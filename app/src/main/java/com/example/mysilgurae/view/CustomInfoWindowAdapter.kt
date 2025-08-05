// view/CustomInfoWindowAdapter.kt
package com.example.mysilgurae.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.mysilgurae.R
import com.example.mysilgurae.data.ApartmentDeal
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        // 기본 창 프레임은 그대로 사용하고 내용만 바꿀 것이므로 null 반환
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val deal = marker.tag as? ApartmentDeal ?: return view

        val nameTextView = view.findViewById<TextView>(R.id.info_apartment_name)
        val dealInfoTextView = view.findViewById<TextView>(R.id.info_deal_info)
        val detailTextView = view.findViewById<TextView>(R.id.info_detail)

        nameTextView.text = deal.displayName
        // [오류 수정!] dealYear 등이 null일 수 있으므로 안전하게 처리합니다.
        val year = (deal.dealYear ?: 0) % 100
        val month = deal.dealMonth ?: 0
        val day = deal.dealDay ?: 0
        val dealDate = "$year.$month.$day"
        dealInfoTextView.text = "$dealDate / ${deal.formattedPrice}만원"
        detailTextView.text = "전용 ${deal.area ?: "-"}㎡, ${deal.floor ?: "-"}층"

        return view
    }
}