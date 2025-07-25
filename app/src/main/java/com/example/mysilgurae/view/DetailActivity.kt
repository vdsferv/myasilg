package com.example.mysilgurae.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysilgurae.R
import com.example.mysilgurae.databinding.ActivityDetailBinding
import com.example.mysilgurae.viewmodel.ApartmentViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.Chip
import com.example.mysilgurae.BuildConfig

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: ApartmentViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    private val API_KEY = BuildConfig.REAL_ESTATE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apartmentName = intent.getStringExtra("APARTMENT_NAME")
        val lawdCd = intent.getStringExtra("LAWD_CD")

        if (apartmentName == null || lawdCd == null) {
            finish()
            return
        }

        binding.textApartmentTitle.text = apartmentName
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchHistoricalDeals(API_KEY, lawdCd, apartmentName)
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredHistoricalDeals.observe(this) { deals ->
            historyAdapter.updateData(deals)
            binding.textDealCount.text = "총 ${deals.size}건 거래"
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.areaFilters.observe(this) { areas ->
            setupFilterChips(areas)
        }

        viewModel.chartData.observe(this) { lineData ->
            setupChart(lineData)
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList())
        binding.recyclerHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@DetailActivity)
        }
    }

    private fun setupFilterChips(areas: List<Int>) {
        binding.chipGroupArea.removeAllViews()

        val allChip = Chip(this).apply {
            text = "전체"
            isCheckable = true
            isChecked = true
            id = 0
        }
        binding.chipGroupArea.addView(allChip)

        areas.forEach { pyeong ->
            val chip = Chip(this).apply {
                text = "$pyeong 평"
                isCheckable = true
                id = pyeong
            }
            binding.chipGroupArea.addView(chip)
        }

        binding.chipGroupArea.setOnCheckedChangeListener { group, checkedId ->
            viewModel.filterDealsByArea(checkedId)
        }
    }

    private fun setupChart(data: LineData) {
        if (data.dataSetCount > 0) {
            val iDataSet = data.getDataSetByIndex(0)
            if (iDataSet is LineDataSet) {
                val fillDrawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient)
                iDataSet.fillDrawable = fillDrawable
            }
        }

        binding.lineChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)

            setMarker(MyMarkerView(this@DetailActivity, R.layout.custom_marker_view))

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = Color.GRAY
            xAxis.axisLineColor = Color.LTGRAY
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val year = value.toInt() / 100
                    val month = value.toInt() % 100
                    return "${year % 100}.${String.format("%02d", month)}"
                }
            }

            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#e0e0e0")
            axisLeft.textColor = Color.GRAY
            axisLeft.setDrawAxisLine(false)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false

            invalidate()
        }
    }
}

private fun setupCharttt(data: LineData) {

}

//commit