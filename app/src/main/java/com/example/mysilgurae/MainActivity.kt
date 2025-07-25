// MainActivity.kt
package com.example.mysilgurae

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mysilgurae.databinding.ActivityMainBinding
import com.example.mysilgurae.view.ListFragment
import com.example.mysilgurae.view.MapFragment
import com.example.mysilgurae.viewmodel.ApartmentViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.mysilgurae.BuildConfig


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ApartmentViewModel by viewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    // TODO: 발급받은 공공데이터포털 API 키를 입력하세요.
    private val API_KEY = BuildConfig.REAL_ESTATE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        checkLocationPermission()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_map
        }

        // [디버깅 코드 추가] ViewModel에서 발생하는 오류를 관찰하고 토스트 메시지로 표시
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, "오류: $errorMessage", Toast.LENGTH_LONG).show()
            Log.e("ApartmentAppError", errorMessage)
        }

        // [디버깅 코드 추가] 데이터 로딩 상태를 관찰하고 토스트 메시지로 표시
        viewModel.deals.observe(this) { deals ->
            if (deals.isNullOrEmpty()) {
                Toast.makeText(this, "거래 내역이 없거나 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${deals.size}건의 거래 내역을 불러왔습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // [추가!] cameraTarget LiveData를 관찰
        viewModel.cameraTarget.observe(this) { latLng ->
            if (latLng != null) {
                // 지도 이동 요청이 들어오면, 현재 탭이 지도가 아닐 경우 지도 탭으로 전환
                if (binding.bottomNavigation.selectedItemId != R.id.nav_map) {
                    binding.bottomNavigation.selectedItemId = R.id.nav_map
                }
            }
        }

    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MapFragment())
                        .commit()
                    true
                }
                R.id.nav_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ListFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    fun fetchDataForArea(lawdCd: String) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val dealYmd = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time)

        Log.d("ApartmentApp", "지도 이동으로 API 호출: lawdCd=$lawdCd")
        viewModel.fetchApartmentDeals(API_KEY, lawdCd, dealYmd)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchDataForCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchDataForCurrentLocation()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다. 기본 지역 데이터로 조회합니다.", Toast.LENGTH_SHORT).show()
                fetchDefaultData()
            }
        }
    }

    private fun fetchDataForCurrentLocation() {
        // TODO: 실제 위치 정보를 가져와 지역코드(LAWD_CD)로 변환하는 로직이 필요합니다.
        Toast.makeText(this, "현재 위치 기반 데이터 로딩 기능은 구현이 필요합니다. 강남구 데이터로 조회합니다.", Toast.LENGTH_LONG).show()
        fetchDefaultData()
    }

    private fun fetchDefaultData() {
        val lawdCd = "11110" // 예시: 서울시 강남구
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val dealYmd = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time)
        // ViewModel을 통해 API 호출

        Log.d("ApartmentApp", "API 호출: key=$API_KEY, lawdCd=$lawdCd, dealYmd=$dealYmd")
        viewModel.fetchApartmentDeals(API_KEY, lawdCd, dealYmd)
    }
}
