package com.example.findme.ui.report

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.findme.R
import com.example.findme.databinding.FragmentReportBinding
import com.example.findme.model.ItemCategory
import com.example.findme.model.ItemModel
import com.example.findme.model.ItemType
import com.example.findme.model.SampleData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register Android Photo Picker contract
        pickMediaLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.layoutPhotoPlaceholder.visibility = View.GONE
                binding.ivSelectedPhoto.visibility = View.VISIBLE
                binding.ivSelectedPhoto.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()
        setupDefaultDate()
        setupPhotoPicker()
        setupSubmitButton()
    }

    private fun setupCategoryDropdown() {
        val categories = ItemCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupDefaultDate() {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(Date()))
    }

    private fun setupPhotoPicker() {
        binding.cardSelectPhoto.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitReport()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val location = binding.etLocation.text?.toString()?.trim().orEmpty()
        val contact = binding.etContact.text?.toString()?.trim().orEmpty()

        if (title.isEmpty()) {
            binding.tilTitle.error = "Please enter an item name"
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        if (location.isEmpty()) {
            binding.tilLocation.error = "Please specify where the item was lost or found"
            isValid = false
        } else {
            binding.tilLocation.error = null
        }

        if (contact.isEmpty()) {
            binding.tilContact.error = "Please provide contact info (e.g. campus email)"
            isValid = false
        } else {
            binding.tilContact.error = null
        }

        return isValid
    }

    private fun submitReport() {
        val itemType = if (binding.toggleType.checkedButtonId == R.id.btnTypeFound) {
            ItemType.FOUND
        } else {
            ItemType.LOST
        }

        val categoryName = binding.actvCategory.text.toString()
        val category = ItemCategory.values().firstOrNull { it.displayName == categoryName }
            ?: ItemCategory.OTHER

        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val location = binding.etLocation.text?.toString()?.trim().orEmpty()
        val date = binding.etDate.text?.toString()?.trim().orEmpty()
        val description = binding.etDescription.text?.toString()?.trim().orEmpty()
        val contact = binding.etContact.text?.toString()?.trim().orEmpty()

        val newItem = ItemModel(
            id = UUID.randomUUID().toString(),
            title = title,
            type = itemType,
            category = category,
            location = location,
            date = date,
            description = description.ifEmpty { "No additional details provided." },
            contactInfo = contact,
            isMyReport = true
        )

        SampleData.addItem(newItem)

        Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_LONG).show()

        // Clear form
        resetForm()
    }

    private fun resetForm() {
        binding.etTitle.text?.clear()
        binding.etLocation.text?.clear()
        binding.etDescription.text?.clear()
        binding.etContact.text?.clear()
        binding.ivSelectedPhoto.setImageURI(null)
        binding.ivSelectedPhoto.visibility = View.GONE
        binding.layoutPhotoPlaceholder.visibility = View.VISIBLE
        selectedImageUri = null
        setupDefaultDate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReportFragment()
    }
}

