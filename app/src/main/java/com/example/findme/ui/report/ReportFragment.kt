package com.example.findme.ui.report

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.findme.R
import com.example.findme.data.db.ItemEntity
import com.example.findme.databinding.FragmentReportBinding
import com.example.findme.model.ItemCategory
import com.example.findme.model.ItemType
import com.example.findme.ui.viewmodel.FindMeViewModel
import com.example.findme.utils.FileUtils
import com.example.findme.utils.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindMeViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private var tempCameraFile: File? = null
    private var tempCameraUri: Uri? = null

    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    // Track pending submission so we can fire notification after permission grant
    private var pendingNotificationItem: ItemEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Native Photo Picker launcher (Gallery)
        pickMediaLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                displaySelectedPhoto(uri)
            }
            // If null, user cancelled picker — preserve existing selection
        }

        // Native Camera Capture launcher
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
                displaySelectedPhoto(tempCameraUri!!)
            } else {
                // Cancelled or failed — clean up temp file if it wasn't the current selection
                if (selectedImageUri != tempCameraUri) {
                    tempCameraFile?.delete()
                }
            }
        }

        // Notification permission launcher (Android 13+)
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            // Permission result doesn't block submission —
            // notification was already attempted or will be attempted
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
        setupPhotoSelectionControls()
        setupSubmitButton()
        observeSubmitResult()
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.categories_array)
        binding.actvCategory.setSimpleItems(categories)
        binding.actvCategory.setText(ItemCategory.ELECTRONICS.displayName, false)

        binding.actvCategory.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            handleCategorySelection(selected)
        }
    }

    private fun handleCategorySelection(categoryName: String) {
        if (categoryName.equals(ItemCategory.OTHER.displayName, ignoreCase = true)) {
            binding.tilCustomCategory.visibility = View.VISIBLE
        } else {
            binding.tilCustomCategory.visibility = View.GONE
            binding.etCustomCategory.text?.clear()
            binding.tilCustomCategory.error = null
        }
    }

    private fun setupDefaultDate() {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(Date()))
    }

    private fun setupPhotoSelectionControls() {
        // Tapping on the photo card triggers source selection dialog
        binding.cardSelectPhoto.setOnClickListener {
            showPhotoSourceDialog()
        }
    }

    private fun showPhotoSourceDialog() {
        val title = if (selectedImageUri == null) {
            getString(R.string.photo_dialog_title)
        } else {
            getString(R.string.photo_dialog_change_title)
        }

        val options = arrayOf(
            getString(R.string.option_camera),
            getString(R.string.option_gallery)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> launchGallery()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun launchCamera() {
        try {
            val (file, uri) = FileUtils.createCameraTempUri(requireContext())
            tempCameraFile = file
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open camera: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun launchGallery() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun displaySelectedPhoto(uri: Uri) {
        binding.layoutPhotoPlaceholder.visibility = View.GONE
        binding.ivSelectedPhoto.visibility = View.VISIBLE
        binding.layoutChangePhotoBadge.visibility = View.VISIBLE

        binding.ivSelectedPhoto.setImageURI(null)
        binding.ivSelectedPhoto.setImageURI(uri)

        // Clear photo validation error
        binding.tvPhotoError.visibility = View.GONE
        binding.cardSelectPhoto.strokeColor = ContextCompat.getColor(
            requireContext(),
            R.color.border_color
        )
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitReport()
            }
        }
    }

    private fun observeSubmitResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.submitResult.collect { result ->
                        when (result) {
                            is FindMeViewModel.SubmitResult.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Report submitted successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                resetForm()

                                // Request notification permission if needed (Android 13+)
                                requestNotificationPermissionIfNeeded()
                            }
                            is FindMeViewModel.SubmitResult.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    result.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.isSubmitting.collect { submitting ->
                        binding.btnSubmit.isEnabled = !submitting
                        binding.btnSubmit.text = if (submitting) {
                            "Submitting…"
                        } else {
                            getString(R.string.btn_submit_report)
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(requireContext())) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val location = binding.etLocation.text?.toString()?.trim().orEmpty()
        val categoryName = binding.actvCategory.text.toString()

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

        // Validate custom category if 'Other' is selected
        if (categoryName.equals(ItemCategory.OTHER.displayName, ignoreCase = true)) {
            val customCat = binding.etCustomCategory.text?.toString()?.trim().orEmpty()
            if (customCat.isEmpty()) {
                binding.tilCustomCategory.error = "Please specify the item category"
                isValid = false
            } else {
                binding.tilCustomCategory.error = null
            }
        } else {
            binding.tilCustomCategory.error = null
        }

        // Compulsory Photo validation
        if (selectedImageUri == null) {
            binding.tvPhotoError.text = getString(R.string.error_photo_required)
            binding.tvPhotoError.visibility = View.VISIBLE
            binding.cardSelectPhoto.strokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.lost_badge_text
            )
            isValid = false
        } else {
            binding.tvPhotoError.visibility = View.GONE
            binding.cardSelectPhoto.strokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.border_color
            )
        }

        if (!isValid) {
            Toast.makeText(
                requireContext(),
                "Please fill in all required fields and add a photo",
                Toast.LENGTH_SHORT
            ).show()
        }

        return isValid
    }

    private fun submitReport() {
        val photoUri = selectedImageUri ?: return

        val itemType = if (binding.toggleType.checkedButtonId == R.id.btnTypeFound) {
            ItemType.FOUND
        } else {
            ItemType.LOST
        }

        val categoryName = binding.actvCategory.text.toString()
        val category = ItemCategory.values().firstOrNull {
            it.displayName.equals(categoryName, ignoreCase = true)
        } ?: ItemCategory.OTHER

        val customCategory = if (category == ItemCategory.OTHER) {
            binding.etCustomCategory.text?.toString()?.trim()
        } else {
            null
        }

        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val location = binding.etLocation.text?.toString()?.trim().orEmpty()
        val date = binding.etDate.text?.toString()?.trim().orEmpty()
        val description = binding.etDescription.text?.toString()?.trim().orEmpty()
        val contact = binding.etContact.text?.toString()?.trim()

        val itemId = UUID.randomUUID().toString()

        // Copy picked/captured image to FindMe's app-specific internal storage
        val internalImagePath = FileUtils.saveImageToInternalStorage(
            requireContext(),
            photoUri,
            itemId
        )

        if (internalImagePath == null) {
            Toast.makeText(
                requireContext(),
                "Failed to save the photo. Please try selecting another image.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // If the photo was a temporary camera capture, delete the temp file from cache
        if (photoUri == tempCameraUri) {
            tempCameraFile?.delete()
            tempCameraFile = null
            tempCameraUri = null
        }

        val newItem = ItemEntity(
            id = itemId,
            title = title,
            type = itemType,
            category = category,
            customCategory = customCategory,
            location = location,
            date = date,
            description = description.ifEmpty { "No additional details provided." },
            contactInfo = contact?.ifEmpty { null },
            isMyReport = true,
            imagePath = internalImagePath,
            createdAt = System.currentTimeMillis()
        )

        viewModel.submitReport(newItem)
    }

    private fun resetForm() {
        binding.etTitle.text?.clear()
        binding.etLocation.text?.clear()
        binding.etDescription.text?.clear()
        binding.etContact.text?.clear()
        binding.etCustomCategory.text?.clear()
        binding.tilCustomCategory.visibility = View.GONE
        binding.tilCustomCategory.error = null

        // Reset photo selection state and views
        selectedImageUri = null
        tempCameraFile?.delete()
        tempCameraFile = null
        tempCameraUri = null

        binding.ivSelectedPhoto.setImageDrawable(null)
        binding.ivSelectedPhoto.visibility = View.GONE
        binding.layoutChangePhotoBadge.visibility = View.GONE
        binding.layoutPhotoPlaceholder.visibility = View.VISIBLE
        binding.tvPhotoError.visibility = View.GONE
        binding.cardSelectPhoto.strokeColor = ContextCompat.getColor(
            requireContext(),
            R.color.border_color
        )

        setupDefaultDate()
        binding.actvCategory.setText(ItemCategory.ELECTRONICS.displayName, false)
        binding.tilTitle.error = null
        binding.tilLocation.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReportFragment()
    }
}
