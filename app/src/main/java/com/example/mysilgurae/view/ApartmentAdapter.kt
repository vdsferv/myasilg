package com.example.mysilgurae.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysilgurae.R
import com.example.mysilgurae.data.ApartmentDeal

// [수정!] 클릭 이벤트를 처리할 리스너를 인자로 받음
class ApartmentAdapter(
    private var deals: List<ApartmentDeal>,
    private val onItemClick: (ApartmentDeal) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.DealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apartment_deal, parent, false)
        return DealViewHolder(view)
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        // [수정!] 클릭 리스너를 bind 함수에 전달
        holder.bind(deals[position], onItemClick)
    }

    override fun getItemCount(): Int = deals.size

    fun updateData(newDeals: List<ApartmentDeal>) {
        deals = newDeals
        notifyDataSetChanged()
    }

    class DealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_apartment_name)
        private val amountTextView: TextView = itemView.findViewById(R.id.text_deal_amount)
        private val infoTextView: TextView = itemView.findViewById(R.id.text_deal_info)

        // [수정!] 클릭 리스너를 받아 itemView에 설정
        fun bind(deal: ApartmentDeal, onItemClick: (ApartmentDeal) -> Unit) {
            nameTextView.text = deal.displayName
            amountTextView.text = "거래금액: ${deal.formattedPrice}만원"
            val dealDate = "${deal.dealYear}년 ${deal.dealMonth}월 ${deal.dealDay}일"
            val dealInfo = "$dealDate / 전용면적: ${deal.area ?: "정보없음"}㎡ / ${deal.floor}층"
            infoTextView.text = dealInfo

            itemView.setOnClickListener {
                onItemClick(deal)
            }
        }
    }
}