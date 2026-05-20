package com.estudiante.techscoop.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.data.PreferencesManager
import com.estudiante.techscoop.data.SessionManager
import com.estudiante.techscoop.databinding.FragmentProfileBinding
// Integración con cierre de sesión Firebase/Google (añadido al flujo de perfil).
import com.estudiante.techscoop.repository.AuthRepository
import com.estudiante.techscoop.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private val categoriesDisplay = arrayOf("Technology", "Business", "Sports", "Entertainment", "General", "Science", "Health")
    private val categoriesApi = arrayOf("technology", "business", "sports", "entertainment", "general", "science", "health")

    private val languagesDisplay = arrayOf("English", "Spanish")
    private val languagesApi = arrayOf("en", "es")

    private val sortDisplay = arrayOf("By Date", "Relevance", "Popularity")
    private val sortApi = arrayOf("publishedAt", "relevancy", "popularity")

    private var latestPhotoFile: File? = null
    private var currentImageUri: Uri? = null

    private var isEditing = false

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentImageUri != null) {
            binding.ivAvatar.setImageURI(currentImageUri)
        }
    }

    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.ivAvatar.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPreferencesUI()
        setupObservers()
        setupListeners()
        setEditMode(false) // start in read-only mode
    }

    // ─────────────────────────── Edit Mode Toggle ───────────────────────────

    private fun setEditMode(editing: Boolean) {
        isEditing = editing

        if (editing) {
            // Populate EditTexts from the current read-only values
            binding.etName.setText(binding.tvName.text)
            binding.etEmail.setText(binding.tvEmail.text)
            binding.etBio.setText(binding.tvBio.text)
            binding.etPassword.setText(binding.tvPassword.tag as? String ?: "")

            binding.layoutReadOnly.visibility = View.GONE
            binding.layoutEditable.visibility = View.VISIBLE
            binding.layoutPhotoButtons.visibility = View.VISIBLE
            binding.btnSaveChanges.visibility = View.VISIBLE
            binding.btnEditToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        } else {
            binding.layoutReadOnly.visibility = View.VISIBLE
            binding.layoutEditable.visibility = View.GONE
            binding.layoutPhotoButtons.visibility = View.GONE
            binding.btnSaveChanges.visibility = View.GONE
            binding.btnEditToggle.setImageResource(android.R.drawable.ic_menu_edit)
        }
    }

    // ─────────────────────────── Preferences Spinners ───────────────────────

    private fun setupPreferencesUI() {
        val catAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoriesDisplay)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = catAdapter

        val langAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languagesDisplay)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = langAdapter

        val sortAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortDisplay)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSortBy.adapter = sortAdapter

        binding.spinnerCategory.setSelection(categoriesApi.indexOf(PreferencesManager.getCategory()).takeIf { it >= 0 } ?: 0)
        binding.spinnerLanguage.setSelection(languagesApi.indexOf(PreferencesManager.getLanguage()).takeIf { it >= 0 } ?: 0)
        binding.spinnerSortBy.setSelection(sortApi.indexOf(PreferencesManager.getSortBy()).takeIf { it >= 0 } ?: 0)

        // AUTOGUARDADO DE PREFERENCIAS
        // Creamos un Listener que se activará cada vez que el usuario
        // toque cualquiera de los tres menús desplegables
        val spinnerListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Obtenemos qué posición (0, 1, 2...) está seleccionada en cada menú en este instante.
                val selectedCatIndex = binding.spinnerCategory.selectedItemPosition
                val selectedLangIndex = binding.spinnerLanguage.selectedItemPosition
                val selectedSortIndex = binding.spinnerSortBy.selectedItemPosition

                // Guardamos directamente en PreferencesManager las palabras clave exactas que necesita la API
                // (ej. "en" en vez de "English"). Si algo falla, asignamos un valor por defecto.
                PreferencesManager.savePreferences(
                    language = languagesApi.getOrElse(selectedLangIndex) { "en" },
                    category = categoriesApi.getOrElse(selectedCatIndex) { "technology" },
                    sortBy = sortApi.getOrElse(selectedSortIndex) { "publishedAt" }
                )
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Conectamos nuestro listener a los tres menús para que estén sincronizados.
        binding.spinnerCategory.onItemSelectedListener = spinnerListener
        binding.spinnerLanguage.onItemSelectedListener = spinnerListener
        binding.spinnerSortBy.onItemSelectedListener = spinnerListener
    }

    // ─────────────────────────── Observers ───────────────────────────────────

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveChanges.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Populate read-only TextViews
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                binding.tvBio.text = it.bio ?: ""
                // Show masked password in read-only view, store real value in tag
                binding.tvPassword.text = "••••••••"
                binding.tvPassword.tag = it.password

                it.profileImageUri?.let { uriString ->
                    currentImageUri = Uri.parse(uriString)
                    binding.ivAvatar.setImageURI(currentImageUri)
                }

                if (it.status == "inactivo") {
                    Toast.makeText(requireContext(), "Your account is inactive", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ─────────────────────────── Click Listeners ────────────────────────────

    private fun setupListeners() {
        binding.btnEditToggle.setOnClickListener {
            if (isEditing) {
                // Cancel editing — revert to read-only without saving
                setEditMode(false)
            } else {
                setEditMode(true)
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.updateProfile(
                name = binding.etName.text.toString(),
                email = binding.etEmail.text.toString(),
                bio = binding.etBio.text.toString(),
                pass = binding.etPassword.text.toString(),
                uri = currentImageUri?.toString()
            )
            // Preferences are now saved automatically via the OnItemSelectedListener

            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()

            // Switch back to read-only mode after saving
            setEditMode(false)
        }

        binding.btnDeactivate.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Deactivate Account")
                .setMessage("Your account will become inactive and be deleted in one month. Continue?")
                .setPositiveButton("Deactivate") { _, _ ->
                    viewModel.deactivateAccount()
                    signOutAndGoToLogin()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnCamera.setOnClickListener {
            if (hasCameraPermission()) {
                launchCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnGallery.setOnClickListener {
            pickGalleryLauncher.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ -> signOutAndGoToLogin() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Cierra Firebase y Google, limpia datos locales y regresa a LoginActivity.
    private fun signOutAndGoToLogin() {
        viewLifecycleOwner.lifecycleScope.launch {
            AuthRepository.signOut(requireContext().applicationContext)
            navigateToLogin()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        latestPhotoFile = photoFile
        currentImageUri = photoUri
        takePictureLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir("ProfilePhotos")
        return File.createTempFile("PROFILE_$timeStamp", ".jpg", storageDir)
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

