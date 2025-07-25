package com.example.mysilgurae.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysilgurae.R
import com.example.mysilgurae.viewmodel.ApartmentViewModel

class ListFragment : Fragment() {

    private val viewModel: ApartmentViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.deals.observe(viewLifecycleOwner) { deals ->
            adapter.updateData(deals)
        }
    }

    private fun setupRecyclerView() {
        // [수정!] 어댑터 생성 시, 클릭된 deal 객체로 viewModel의 함수를 호출하는 람다를 전달
        adapter = ApartmentAdapter(emptyList()) { deal ->
            viewModel.onApartmentClicked(deal)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}
