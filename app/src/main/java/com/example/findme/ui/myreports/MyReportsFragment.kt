package com.example.findme.ui.myreports

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.databinding.FragmentMyReportsBinding
import com.example.findme.model.SampleData
import com.example.findme.ui.adapter.ItemAdapter
import com.example.findme.ui.detail.ItemDetailActivity

class MyReportsFragment : Fragment() {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!

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
        loadMyReports()
    }

    override fun onResume() {
        super.onResume()
        loadMyReports()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra(ItemDetailActivity.EXTRA_ITEM, selectedItem)
            }
            startActivity(intent)
        }

        binding.rvMyReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun loadMyReports() {
        val myReports = SampleData.getMyReports()
        itemAdapter.submitList(myReports)

        binding.tvMyReportsCount.text = "${myReports.size} local submission${if (myReports.size == 1) "" else "s"}"

        if (myReports.isEmpty()) {
            binding.rvMyReports.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvMyReports.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
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

