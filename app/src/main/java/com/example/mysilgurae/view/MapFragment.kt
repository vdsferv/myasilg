package com.example.mysilgurae.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mysilgurae.data.ApartmentDeal
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mysilgurae.MainActivity
import com.example.mysilgurae.R
import com.example.mysilgurae.viewmodel.ApartmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private val viewModel: ApartmentViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var geocoder: Geocoder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.KOREA)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val initialPos = LatLng(37.5665, 126.9780)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 12f))

        // [추가!] 지도 이동 및 마커 클릭 리스너 설정
        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnMarkerClickListener(this)

        viewModel.deals.observe(viewLifecycleOwner) { deals ->
            googleMap.clear()
            deals.forEach { deal ->
                if (deal.latitude != null && deal.longitude != null) {
                    val location = LatLng(deal.latitude!!, deal.longitude!!)
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(deal.displayName)
                            .snippet("거래가: ${deal.formattedPrice}만원")
                    )
                    // [추가!] 마커에 아파트 정보를 태그로 저장
                    marker?.tag = deal
                }
            }
        }

        viewModel.cameraTarget.observe(viewLifecycleOwner) { latLng ->
            latLng?.let {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
                viewModel.cameraTarget.postValue(null)
            }
        }
    }

    // [추가!] 지도 이동이 멈췄을 때 호출되는 함수
    override fun onCameraIdle() {
        val center = googleMap.cameraPosition.target
        try {
            // 중심 좌표를 주소로 변환 (API 33 이상에서는 비동기 콜백 방식 권장)
            val addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // 법정동 코드는 '행정표준관리시스템'에서 제공하는 데이터를 앱에 내장하거나
                // 별도 API를 통해 주소->법정동 코드로 변환해야 합니다.
                // 여기서는 예시로 서울시의 지역번호(sggCd)를 임시로 사용합니다.
                // 실제로는 주소(e.g., "서울특별시 종로구")를 기반으로 정확한 LAWD_CD를 찾아야 합니다.
                val tempLawdCd = getTempLawdCd(address.getAddressLine(0))
                if (tempLawdCd != null) {
                    (activity as? MainActivity)?.fetchDataForArea(tempLawdCd)
                }
            }
        } catch (e: IOException) {
            Log.e("MapFragment", "주소 변환 실패", e)
        }
    }

    // [추가!] 마커가 클릭되었을 때 호출되는 함수
    override fun onMarkerClick(marker: Marker): Boolean {
        val deal = marker.tag as? ApartmentDeal ?: return false
        val lawdCd = viewModel.currentLawdCd ?: return false

        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra("APARTMENT_NAME", deal.displayName)
            putExtra("LAWD_CD", lawdCd)
        }
        startActivity(intent)
        return true // true를 반환하면 기본 정보창이 뜨지 않음
    }

    // 임시로 주소에서 지역 코드를 추출하는 함수 (실제 앱에서는 더 정교한 로직 필요)
    private fun getTempLawdCd(address: String): String? {
        return when {
            address.contains("종로구") -> "11110"
            address.contains("강남구") -> "11680"
            address.contains("마포구") -> "11440"
            // ... 다른 지역 추가
            else -> null
        }
    }
}
