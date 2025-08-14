// view/MapFragment.kt
package com.example.mysilgurae.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mysilgurae.MainActivity
import com.example.mysilgurae.R
import com.example.mysilgurae.data.ApartmentDeal
import com.example.mysilgurae.data.LawdCdRepository
import com.example.mysilgurae.viewmodel.ApartmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnInfoWindowClickListener {

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

        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnInfoWindowClickListener(this)
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))

        viewModel.deals.observe(viewLifecycleOwner) { deals ->
            googleMap.clear()
            deals.groupBy { it.displayName + it.fullAddress }
                .forEach { (_, dealsForApartment) ->
                    val latestDeal = dealsForApartment.maxByOrNull { it.dealYear!! * 10000 + it.dealMonth!! * 100 + it.dealDay!! } ?: return@forEach
                    if (latestDeal.latitude != null && latestDeal.longitude != null) {
                        createCustomMarker(latestDeal)
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

    override fun onCameraIdle() {
        if (googleMap.cameraPosition.zoom < 12) return

        val center = googleMap.cameraPosition.target
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(center.latitude, center.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val fullAddress = addresses[0].getAddressLine(0) // e.g., "대한민국 서울특별시 종로구..."

                // [수정!] 전체 주소에서 직접 코드를 찾는 방식으로 변경
                val lawdCd = LawdCdRepository.findCdFromAddress(fullAddress)

                if (lawdCd != null) {
                    (activity as? MainActivity)?.fetchDataForArea(lawdCd)
                }
            }
        } catch (e: Exception) {
            Log.e("MapFragment", "Geocoder 오류 발생", e)
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val deal = marker.tag as? ApartmentDeal ?: return
        val lawdCd = viewModel.currentLawdCd ?: return

        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra("APARTMENT_NAME", deal.displayName)
            putExtra("LAWD_CD", lawdCd)
        }
        startActivity(intent)
    }

    private fun createCustomMarker(deal: ApartmentDeal) {
        val markerView = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.marker_layout, null)

        val nameTextView = markerView.findViewById<TextView>(R.id.marker_apartment_name)
        val priceTextView = markerView.findViewById<TextView>(R.id.marker_price)

        nameTextView.text = deal.displayName
        priceTextView.text = deal.formattedPrice

        val markerBitmap = createBitmapFromView(markerView)

        val location = LatLng(deal.latitude!!, deal.longitude!!)
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .anchor(0.5f, 1.0f)
        )
        marker?.tag = deal
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}