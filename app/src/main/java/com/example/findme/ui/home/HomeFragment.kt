package com.example.findme.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.data.preferences.FindMePreferences
import com.example.findme.databinding.FragmentHomeBinding
import com.example.findme.model.ItemType
import com.example.findme.model.toItemModel
import com.example.findme.ui.adapter.ItemAdapter
import com.example.findme.ui.detail.ItemDetailActivity
import com.example.findme.ui.viewmodel.FindMeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindMeViewModel by activityViewModels()
    private lateinit var itemAdapter: ItemAdapter

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
        restoreSavedFilter()
        observeItems()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra(ItemDetailActivity.EXTRA_ITEM_ID, selectedItem.id)
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
                viewModel.searchQuery.value = s?.toString()?.trim().orEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter chips listener
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(R.id.chipLost) -> FindMePreferences.FILTER_LOST
                checkedIds.contains(R.id.chipFound) -> FindMePreferences.FILTER_FOUND
                else -> FindMePreferences.FILTER_ALL
            }
            viewModel.setFilter(filter)
        }
    }

    private fun restoreSavedFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentFilter.collect { filter ->
                // Select the chip matching the saved filter without re-triggering listener
                val chipId = when (filter) {
                    FindMePreferences.FILTER_LOST -> R.id.chipLost
                    FindMePreferences.FILTER_FOUND -> R.id.chipFound
                    else -> R.id.chipAll
                }
                if (binding.chipGroupFilter.checkedChipId != chipId) {
                    binding.chipGroupFilter.check(chipId)
                }
            }
        }
        // Also load the persisted preference on first launch
        viewModel.loadSavedFilter()
    }

    private fun observeItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeItems.collect { entities ->
                    // Apply in-memory type filter when searching
                    val currentQuery = viewModel.searchQuery.value
                    val currentFilter = viewModel.currentFilter.value

                    val filtered = if (currentQuery.isNotBlank() && currentFilter != FindMePreferences.FILTER_ALL) {
                        val filterType = when (currentFilter) {
                            FindMePreferences.FILTER_LOST -> ItemType.LOST
                            FindMePreferences.FILTER_FOUND -> ItemType.FOUND
                            else -> null
                        }
                        if (filterType != null) {
                            entities.filter { it.type == filterType }
                        } else {
                            entities
                        }
                    } else {
                        entities
                    }

                    val items = filtered.map { it.toItemModel() }
                    itemAdapter.submitList(items)

                    binding.tvItemsCount.text =
                        "Showing ${items.size} item${if (items.size == 1) "" else "s"}"

                    if (items.isEmpty()) {
                        binding.rvItems.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvItems.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
            }
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
