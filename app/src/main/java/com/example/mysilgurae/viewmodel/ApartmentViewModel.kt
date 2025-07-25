package com.example.mysilgurae.viewmodel

import android.app.Application
import android.graphics.Color
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mysilgurae.data.ApartmentDeal
import com.example.mysilgurae.network.RetrofitClient
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ApartmentViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.instance
    private val geocoder = Geocoder(application, Locale.KOREA)

    private val _deals = MutableLiveData<List<ApartmentDeal>>()
    val deals: LiveData<List<ApartmentDeal>> = _deals

    // [추가!] 10년치 상세 거래 내역을 담을 LiveData
    private val _historicalDeals = MutableLiveData<List<ApartmentDeal>>()
    val historicalDeals: LiveData<List<ApartmentDeal>> = _historicalDeals

    // [오류 수정!] 필터링된 거래 내역을 위한 LiveData 정의
    private val _filteredHistoricalDeals = MutableLiveData<List<ApartmentDeal>>()
    val filteredHistoricalDeals: LiveData<List<ApartmentDeal>> = _filteredHistoricalDeals

    // [추가!] 평수 필터 목록
    private val _areaFilters = MutableLiveData<List<Int>>()
    val areaFilters: LiveData<List<Int>> = _areaFilters

    // [추가!] 차트 데이터
    private val _chartData = MutableLiveData<LineData>()
    val chartData: LiveData<LineData> = _chartData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    val cameraTarget = MutableLiveData<LatLng?>()
    var currentLawdCd: String? = null // 현재 조회된 지역 코드 저장

    fun fetchApartmentDeals(serviceKey: String, lawdCd: String, dealYmd: String) {
        if (currentLawdCd == lawdCd) return // 이미 같은 지역을 조회했다면 다시 호출하지 않음

        _isLoading.value = true
        currentLawdCd = lawdCd // 현재 지역 코드 저장
        viewModelScope.launch {
            try {
                val response = apiService.getApartmentDeals(serviceKey, lawdCd, dealYmd)
                val dealsFromApi = response.body?.items?.itemList ?: emptyList()
                geocodeDealsInBackground(dealsFromApi)
            } catch (e: Exception) {
                _error.postValue("데이터를 불러오는 데 실패했습니다: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    private fun geocodeDealsInBackground(deals: List<ApartmentDeal>) {
        viewModelScope.launch(Dispatchers.IO) {
            val geocodedDeals = deals.mapNotNull { deal ->
                try {
                    val addressList = geocoder.getFromLocationName(deal.fullAddress, 1)
                    if (!addressList.isNullOrEmpty()) {
                        deal.latitude = addressList[0].latitude
                        deal.longitude = addressList[0].longitude
                        deal
                    } else { null }
                } catch (e: IOException) { null }
            }
            _deals.postValue(geocodedDeals)
            _isLoading.postValue(false)
        }
    }

    // [추가!] 10년치(120개월) 데이터를 비동기적으로 조회하는 함수
    fun fetchHistoricalDeals(serviceKey: String, lawdCd: String, apartmentName: String) {
        _isLoading.value = true
        _historicalDeals.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
                val deferreds = (0 until 120).map { i ->
                    val targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }.time
                    val dealYmd = dateFormat.format(targetDate)
                    async {
                        try {
                            val response = apiService.getApartmentDeals(serviceKey, lawdCd, dealYmd, numOfRows = 100)
                            response.body?.items?.itemList?.filter { it.apartmentName == apartmentName } ?: emptyList()
                        } catch (e: Exception) { emptyList<ApartmentDeal>() }
                    }
                }
                val allDeals = deferreds.awaitAll().flatten()
                _historicalDeals.postValue(allDeals)

                processHistoricalData(allDeals)

            } catch (e: Exception) {
                _error.postValue("10년치 데이터를 불러오는 데 실패했습니다: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // [추가!] 10년치 데이터 후처리 (필터 생성, 초기 목록/그래프 설정)
    private fun processHistoricalData(deals: List<ApartmentDeal>) {
        // 평수 필터 목록 생성 (중복 제거 및 정렬)
        val areas = deals.map { it.areaInPyeong }.distinct().sorted()
        _areaFilters.postValue(areas)

        // 초기 데이터는 필터링 없이 전체 목록으로 설정
        _filteredHistoricalDeals.postValue(deals)
        // 전체 목록에 대한 그래프 데이터 생성
        createChartData(deals)
    }

    // [추가!] 평수로 거래 내역 필터링
    fun filterDealsByArea(pyeong: Int) {
        val filtered = if (pyeong == 0) { // '전체' 필터
            _historicalDeals.value
        } else {
            _historicalDeals.value?.filter { it.areaInPyeong == pyeong }
        }
        filtered?.let {
            _filteredHistoricalDeals.postValue(it)
            createChartData(it)
        }
    }

    // [추가!] 차트 데이터 생성
    private fun createChartData(deals: List<ApartmentDeal>) {
        if (deals.isEmpty()) {
            _chartData.postValue(LineData()) // 데이터 없으면 빈 차트
            return
        }

        // 1. 데이터를 '연월'별로 그룹화하고, 각 월의 평균 가격 계산
        val monthlyAvgPrices = deals
            .groupBy { (it.dealYear ?: 0) * 100 + (it.dealMonth ?: 0) } // 202507 형식으로 그룹 키 생성
            .mapValues { (_, monthDeals) ->
                monthDeals.mapNotNull { it.dealAmount?.replace(",", "")?.trim()?.toDoubleOrNull() }.average()
            }
            .toSortedMap() // 날짜순 정렬

        // 2. 차트 라이브러리가 이해할 수 있는 Entry 객체로 변환
        val entries = monthlyAvgPrices.map { (yearMonth, avgPrice) ->
            // yearMonth (202507)를 그래프의 x축 값으로 사용하기 위해 float으로 변환
            Entry(yearMonth.toFloat(), avgPrice.toFloat())
        }

        // 3. 차트 데이터셋 생성 및 스타일링
        // [수정!] 그래프 디자인 개선
        val dataSet = LineDataSet(entries, "월 평균 실거래가 (만원)").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER // 부드러운 곡선
            color = Color.parseColor("#4A89F3") // 세련된 파란색
            lineWidth = 3f // 선 두께를 더 두껍게

            // 데이터 포인트 원 숨기기
            setDrawCircles(false)

            // 그라데이션 채우기 활성화
            setDrawFilled(true)

            // 기타 스타일
            setDrawValues(false) // 포인트에 값 텍스트 표시 안 함
            highLightColor = Color.GRAY // 클릭 시 하이라이트 색상
        }

        _chartData.postValue(LineData(dataSet))
    }

    fun onApartmentClicked(deal: ApartmentDeal) {
        if (deal.latitude != null && deal.longitude != null) {
            cameraTarget.value = LatLng(deal.latitude!!, deal.longitude!!)
        }
    }
}