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
import com.estudiante.techscoop.databinding.FragmentProfileBinding
import com.estudiante.techscoop.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var latestPhotoFile: File? = null
    private var currentImageUri: Uri? = null

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
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

        setupObservers()
        setupListeners()
    }

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
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
                binding.etBio.setText(it.bio ?: "")
                binding.etPassword.setText(it.password)

                it.profileImageUri?.let { uriString ->
                    currentImageUri = Uri.parse(uriString)
                    binding.ivAvatar.setImageURI(currentImageUri)
                }

                if (it.status == "inactivo") {
                    Toast.makeText(requireContext(), "Tu cuenta está inactiva", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveChanges.setOnClickListener {
            viewModel.updateProfile(
                name = binding.etName.text.toString(),
                email = binding.etEmail.text.toString(),
                bio = binding.etBio.text.toString(),
                pass = binding.etPassword.text.toString(),
                uri = currentImageUri?.toString()
            )
            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeactivate.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Desactivar Cuenta")
                .setMessage("Tu cuenta quedará inactiva y se eliminará en un mes. ¿Continuar?")
                .setPositiveButton("Desactivar") { _, _ ->
                    viewModel.deactivateAccount()
                    navigateToLogin()
                }
                .setNegativeButton("Cancelar", null)
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
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ -> navigateToLogin() }
                .setNegativeButton("Cancelar", null)
                .show()
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