package com.example.findme.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.FragmentHomeBinding
import com.example.findme.model.ItemModel
import com.example.findme.model.ItemType
import com.example.findme.model.SampleData
import com.example.findme.ui.adapter.ItemAdapter
import com.example.findme.ui.detail.ItemDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: ItemAdapter
    private var allItems: List<ItemModel> = emptyList()
    private var currentFilterType: ItemType? = null
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilters()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data in case a new item was reported
        loadData()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra(ItemDetailActivity.EXTRA_ITEM, selectedItem)
            }
            startActivity(intent)
        }

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun setupSearchAndFilters() {
        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter chips listener
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterType = when {
                checkedIds.contains(R.id.chipLost) -> ItemType.LOST
                checkedIds.contains(R.id.chipFound) -> ItemType.FOUND
                else -> null // All items
            }
            applyFilters()
        }
    }

    private fun loadData() {
        allItems = SampleData.getAllItems()
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val matchesType = currentFilterType == null || item.type == currentFilterType
            val matchesQuery = currentSearchQuery.isEmpty() ||
                    item.title.contains(currentSearchQuery, ignoreCase = true) ||
                    item.description.contains(currentSearchQuery, ignoreCase = true) ||
                    item.location.contains(currentSearchQuery, ignoreCase = true) ||
                    item.category.displayName.contains(currentSearchQuery, ignoreCase = true)

            matchesType && matchesQuery
        }

        itemAdapter.submitList(filtered)

        binding.tvItemsCount.text = getString(R.string.filter_all).let {
            "Showing ${filtered.size} item${if (filtered.size == 1) "" else "s"}"
        }

        if (filtered.isEmpty()) {
            binding.rvItems.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvItems.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}

