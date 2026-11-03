package com.example.findme.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.findme.R
import com.example.findme.databinding.ItemCardPostBinding
import com.example.findme.model.ItemModel
import com.example.findme.model.ItemType

class ItemAdapter(
    private val onItemClick: (ItemModel) -> Unit
) : ListAdapter<ItemModel, ItemAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemCardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemCardPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemModel) {
            val context = binding.root.context

            binding.tvTitle.text = item.title
            binding.tvCategory.text = item.category.displayName
            binding.tvLocation.text = item.location
            binding.tvDate.text = item.date
            binding.tvDescription.text = item.description

            if (item.type == ItemType.LOST) {
                binding.tvTypeBadge.text = "LOST"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost)
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.lost_badge_text))
            } else {
                binding.tvTypeBadge.text = "FOUND"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_found)
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.found_badge_text))
            }

            binding.tvMyReportTag.visibility = if (item.isMyReport) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ItemModel>() {
            override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}

