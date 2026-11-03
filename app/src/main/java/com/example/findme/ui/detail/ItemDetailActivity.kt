package com.example.findme.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.findme.R
import com.example.findme.databinding.ActivityItemDetailBinding
import com.example.findme.model.ItemModel
import com.example.findme.model.ItemType

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private var item: ItemModel? = null

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

        item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_ITEM, ItemModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_ITEM) as? ItemModel
        }

        setupToolbar()
        populateDetails()
        setupActions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.topToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun populateDetails() {
        val currentItem = item ?: run {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvDetailTitle.text = currentItem.title
        binding.tvDetailCategory.text = currentItem.displayCategory
        binding.tvDetailLocation.text = currentItem.location
        binding.tvDetailDate.text = currentItem.date
        binding.tvDetailDescription.text = currentItem.description
        binding.tvDetailContact.text = currentItem.contactInfo

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

    private fun setupActions() {
        binding.btnContactAction.setOnClickListener {
            item?.let { currentItem ->
                val contact = currentItem.contactInfo
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

        binding.btnShareBottom.setOnClickListener {
            shareItem()
        }
    }

    private fun shareItem() {
        val currentItem = item ?: return
        val typeLabel = if (currentItem.type == ItemType.LOST) "LOST" else "FOUND"
        val shareText = """
            [FindMe - Campus Lost & Found]
            Item: ${currentItem.title} ($typeLabel)
            Category: ${currentItem.displayCategory}
            Location: ${currentItem.location}
            Date: ${currentItem.date}
            Description: ${currentItem.description}
            Contact: ${currentItem.contactInfo}
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
        const val EXTRA_ITEM = "extra_item"
    }
}

