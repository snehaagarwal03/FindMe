package com.example.findme.ui.myreports

import android.content.Intent
import android.os.Bundle
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
import com.example.findme.databinding.FragmentMyReportsBinding
import com.example.findme.model.toItemModel
import com.example.findme.ui.adapter.ItemAdapter
import com.example.findme.ui.detail.ItemDetailActivity
import com.example.findme.ui.viewmodel.FindMeViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MyReportsFragment : Fragment() {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindMeViewModel by activityViewModels()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        observeMyReports()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra(ItemDetailActivity.EXTRA_ITEM_ID, selectedItem.id)
            }
            startActivity(intent)
        }

        binding.rvMyReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun setupTabs() {
        // Sync tab layout with current ViewModel tab state
        val initialTabIndex = if (viewModel.myReportsTab.value == FindMeViewModel.MyReportsTab.PAST) 1 else 0
        binding.tabLayoutReports.getTabAt(initialTabIndex)?.select()

        binding.tabLayoutReports.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedTab = if (tab?.position == 1) {
                    FindMeViewModel.MyReportsTab.PAST
                } else {
                    FindMeViewModel.MyReportsTab.ACTIVE
                }
                viewModel.setMyReportsTab(selectedTab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeMyReports() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.myReports.collect { entities ->
                    val items = entities.map { it.toItemModel() }
                    itemAdapter.submitList(items)

                    val isPastTab = viewModel.myReportsTab.value == FindMeViewModel.MyReportsTab.PAST

                    if (items.isEmpty()) {
                        binding.rvMyReports.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE

                        if (isPastTab) {
                            binding.tvEmptyReportsTitle.text = getString(R.string.empty_my_reports_past_title)
                            binding.tvEmptyReportsSubtitle.text = getString(R.string.empty_my_reports_past_subtitle)
                        } else {
                            binding.tvEmptyReportsTitle.text = getString(R.string.empty_my_reports_active_title)
                            binding.tvEmptyReportsSubtitle.text = getString(R.string.empty_my_reports_active_subtitle)
                        }
                    } else {
                        binding.rvMyReports.visibility = View.VISIBLE
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
        fun newInstance() = MyReportsFragment()
    }
}
