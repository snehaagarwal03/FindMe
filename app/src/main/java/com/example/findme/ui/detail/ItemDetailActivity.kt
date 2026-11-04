package com.example.findme.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.findme.FindMeApplication
import com.example.findme.R
import com.example.findme.data.db.ItemEntity
import com.example.findme.databinding.ActivityItemDetailBinding
import com.example.findme.model.ItemCategory
import com.example.findme.model.ItemType
import com.example.findme.utils.ImageUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private var item: ItemEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        loadItem()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.topToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID)

        if (itemId.isNullOrBlank()) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val app = application as FindMeApplication
        lifecycleScope.launch {
            app.repository.getItemByIdFlow(itemId).collect { entity ->
                if (entity == null) {
                    if (item == null) {
                        Toast.makeText(this@ItemDetailActivity, "Item no longer exists", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@collect
                }
                item = entity
                populateDetails(entity)
                setupActions(entity)
                setupCompletionSection(entity)
            }
        }
    }

    private fun populateDetails(currentItem: ItemEntity) {
        binding.tvDetailTitle.text = currentItem.title

        val displayCategory = if (currentItem.category == ItemCategory.OTHER && !currentItem.customCategory.isNullOrBlank()) {
            currentItem.customCategory
        } else {
            currentItem.category.displayName
        }
        binding.tvDetailCategory.text = displayCategory
        binding.tvDetailLocation.text = currentItem.location
        binding.tvDetailDate.text = currentItem.date
        binding.tvDetailDescription.text = currentItem.description
        binding.tvDetailContact.text = currentItem.contactInfo ?: "Not provided"

        // Display item photo
        if (currentItem.imagePath != null || currentItem.imageResId != null) {
            binding.cardDetailPhoto.visibility = View.VISIBLE
            ImageUtils.loadItemImage(
                binding.ivDetailPhoto,
                currentItem.imagePath,
                currentItem.imageResId,
                targetWidth = 1000,
                targetHeight = 750
            )
        } else {
            binding.cardDetailPhoto.visibility = View.GONE
        }

        if (currentItem.type == ItemType.LOST) {
            binding.tvDetailTypeBadge.text = "LOST"
            binding.tvDetailTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost)
            binding.tvDetailTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.lost_badge_text))
            binding.btnContactAction.text = "Contact Owner / Reporter"
        } else {
            binding.tvDetailTypeBadge.text = "FOUND"
            binding.tvDetailTypeBadge.setBackgroundResource(R.drawable.bg_badge_found)
            binding.tvDetailTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.found_badge_text))
            binding.btnContactAction.text = "Contact Finder"
        }
    }

    private fun setupActions(currentItem: ItemEntity) {
        binding.btnContactAction.setOnClickListener {
            val contact = currentItem.contactInfo
            if (contact.isNullOrBlank()) {
                Toast.makeText(this, "No contact information provided", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = if (contact.contains("@")) {
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$contact")
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding FindMe Post: ${currentItem.title}")
                }
            } else {
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$contact")
                }
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Contact info: $contact", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupCompletionSection(currentItem: ItemEntity) {
        if (currentItem.isCompleted) {
            // Already completed — show non-editable completion status card
            binding.cardCompletionSection.visibility = View.VISIBLE
            binding.layoutCompletionCheckbox.visibility = View.GONE
            binding.layoutCompletionStatus.visibility = View.VISIBLE

            if (currentItem.type == ItemType.LOST) {
                binding.tvCompletionMessage.text = getString(R.string.status_lost_completed_desc)
            } else {
                binding.tvCompletionMessage.text = getString(R.string.status_found_completed_desc)
            }
        } else if (currentItem.isMyReport) {
            // Active report belonging to current user — show completion checkbox
            binding.cardCompletionSection.visibility = View.VISIBLE
            binding.layoutCompletionCheckbox.visibility = View.VISIBLE
            binding.layoutCompletionStatus.visibility = View.GONE

            if (currentItem.type == ItemType.LOST) {
                binding.cbMarkCompleted.text = getString(R.string.checkbox_lost_completed)
            } else {
                binding.cbMarkCompleted.text = getString(R.string.checkbox_found_completed)
            }

            binding.cbMarkCompleted.setOnCheckedChangeListener(null)
            binding.cbMarkCompleted.isChecked = false
            binding.cbMarkCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showCompletionConfirmationDialog(currentItem)
                }
            }
        } else {
            // Not reported by current user and not completed — hide completion section
            binding.cardCompletionSection.visibility = View.GONE
        }
    }

    private fun showCompletionConfirmationDialog(currentItem: ItemEntity) {
        val isLost = currentItem.type == ItemType.LOST
        val title = if (isLost) {
            getString(R.string.dialog_title_lost_completed)
        } else {
            getString(R.string.dialog_title_found_completed)
        }
        val message = if (isLost) {
            getString(R.string.dialog_msg_lost_completed)
        } else {
            getString(R.string.dialog_msg_found_completed)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.action_confirm) { _, _ ->
                markAsCompleted(currentItem.id)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                binding.cbMarkCompleted.isChecked = false
                dialog.dismiss()
            }
            .setOnCancelListener {
                binding.cbMarkCompleted.isChecked = false
            }
            .show()
    }

    private fun markAsCompleted(itemId: String) {
        val app = application as FindMeApplication
        lifecycleScope.launch {
            app.repository.updateCompletionStatus(itemId, true)
            Toast.makeText(this@ItemDetailActivity, "Report marked as completed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareItem() {
        val currentItem = item ?: return
        val typeLabel = if (currentItem.type == ItemType.LOST) "LOST" else "FOUND"
        val displayCategory = if (currentItem.category == ItemCategory.OTHER && !currentItem.customCategory.isNullOrBlank()) {
            currentItem.customCategory
        } else {
            currentItem.category.displayName
        }
        val shareText = """
            [FindMe - Campus Lost & Found]
            Item: ${currentItem.title} ($typeLabel)
            Category: $displayCategory
            Location: ${currentItem.location}
            Date: ${currentItem.date}
            Description: ${currentItem.description}
            Contact: ${currentItem.contactInfo ?: "Not provided"}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share Item Details"))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_share -> {
                shareItem()
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }
}
