package com.example.mysilgurae.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysilgurae.R
import com.example.mysilgurae.data.ApartmentDeal

class HistoryAdapter(private var deals: List<ApartmentDeal>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_deal, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(deals[position])
    }

    override fun getItemCount(): Int = deals.size

    fun updateData(newDeals: List<ApartmentDeal>) {
        // 최신순으로 정렬
        this.deals = newDeals.sortedWith(compareByDescending<ApartmentDeal> { it.dealYear }.thenByDescending { it.dealMonth }.thenByDescending { it.dealDay })
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.text_deal_date)
        private val priceTextView: TextView = itemView.findViewById(R.id.text_deal_price)
        private val infoTextView: TextView = itemView.findViewById(R.id.text_deal_detail_info)

        fun bind(deal: ApartmentDeal) {
            dateTextView.text = "${deal.dealYear}.${deal.dealMonth}.${deal.dealDay}"
            priceTextView.text = "${deal.formattedPrice}만원"
            infoTextView.text = "전용 ${deal.area ?: "-"}㎡, ${deal.floor}층"
        }
    }
}