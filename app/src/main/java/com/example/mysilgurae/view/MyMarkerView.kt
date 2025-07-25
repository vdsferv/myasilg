package com.example.mysilgurae.view

import android.content.Context
import android.widget.TextView
import com.example.mysilgurae.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.NumberFormat
import java.util.*

class MyMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val dateTextView: TextView = findViewById(R.id.text_marker_date)
    private val priceTextView: TextView = findViewById(R.id.text_marker_price)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val year = it.x.toInt() / 100
            val month = it.x.toInt() % 100
            dateTextView.text = "${year}년 ${month}월"

            val priceInTenThousand = it.y.toLong()
            val priceInWon = priceInTenThousand * 10000
            val formattedPrice = if (priceInWon >= 100000000) {
                String.format(Locale.KOREA, "%.1f억원", priceInWon / 100000000.0)
            } else {
                String.format(Locale.KOREA, "%s만원", NumberFormat.getNumberInstance(Locale.KOREA).format(priceInTenThousand))
            }
            priceTextView.text = "평균 ${formattedPrice}"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}