package com.bennohan.kelasku.ui.editProfile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowInsetsController
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.School
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.databinding.ActivityEditProfileBinding
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.isEmptyRequired
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import com.crocodic.core.helper.DateTimeHelper
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileActivity :
    BaseActivity<ActivityEditProfileBinding, EditProfileViewModel>(R.layout.activity_edit_profile) {

    @Inject
    lateinit var session: Session
    private var dataSchool = ArrayList<School?>()
    private var schoolId: String? = null

    private var userName: String? = null
    private var school: Int? = null
    private var schoolName: String? = null
    private var filePhoto: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)



        getUser()
        autocompleteSpinner()
        observe()
        getListSchool()

        binding.btnBack.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            if (filePhoto == null) {
                editProfile()
            } else {
                editProfilePhoto()
            }
        }

        binding.btnPhoto.setOnClickListener {
            validateForm()
            if (checkPermissionGallery()) {
                openGallery()
            } else {
                requestPermissionGallery()
            }
        }


    }
    //TODO Must Test
    private fun validateForm() {
            val name = binding.etName.textOf()

            if (name.isEmpty()) {
                binding.etName.isEmptyRequired(R.string.mustFill)
            }

            if (schoolId.isNullOrEmpty()) {
                binding.autoCompleteSpinner.isEmptyRequired(R.string.mustFill)
            }
    }


    private fun getUser() {
        val user = session.getUser()
        school = user?.sekolahId
        userName = user?.nama
        schoolName = user?.namaSekolah
        binding.user = user
        setResult(6100)
    }

    private fun getListSchool() {
        viewModel.schoolList()
    }


    private fun editProfile() {
        val name = binding.etName.textOf()

        if (schoolId.isNullOrEmpty() && name.isEmpty()) {
            binding.root.snacked("data tidak ada perubahan")
            return
        }

        if (name == userName && schoolId.isNullOrEmpty()) {
            binding.root.snacked("data tidak ada perubahan")
            return
        }

        if (schoolId.isNullOrEmpty()) {
            viewModel.editProfile(name, session.getUser()?.sekolahId.toString())
        } else {
            viewModel.editProfile(name, schoolId)
        }

    }

    private fun editProfilePhoto() {
        val name = binding.etName.textOf()

        lifecycleScope.launch {
            val compressesFile = compressFile(filePhoto!!)
            if (compressesFile != null) {
                if (schoolId.isNullOrEmpty()) {
                    viewModel.updateUserWithPhoto(
                        name,
                        session.getUser()?.sekolahId.toString(),
                        compressesFile
                    )
                } else {
                    viewModel.updateUserWithPhoto(name, schoolId, compressesFile)
                }
            }


        }

    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                tos("Profile Updated")
                                loadingDialog.dismiss()
                                setResult(6100)
                                finish()
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                binding.root.snacked("Update Profile failed")
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
                launch {
                    viewModel.dataSchool.collect { school ->
                        dataSchool.addAll(school)
                    }
                }

            }
        }

    }

    private fun autocompleteSpinner() {
        val autoCompleteSpinner = findViewById<AutoCompleteTextView>(R.id.autoCompleteSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataSchool)
        autoCompleteSpinner.setText(schoolName)
        autoCompleteSpinner.setAdapter(adapter)


        // Show the dropdown list when the AutoCompleteTextView is clicked
        autoCompleteSpinner.setOnClickListener {
            autoCompleteSpinner.showDropDown()
//            autoCompleteSpinner.setDropDownVerticalOffset(-autoCompleteSpinner.height)
            autoCompleteSpinner.dropDownVerticalOffset = -autoCompleteSpinner.height

        }

        autoCompleteSpinner.setOnItemClickListener { _, _, position, _ ->
            // Handle item selection here
            val selectedItem = dataSchool[position]
            schoolId = selectedItem?.sekolahId.toString()


        }
    }

    //MultiPart Gallery , photo not Done yet
    private var activityLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let {
                generateFileImage(it)
            }
        }

    //cek Permission Gallery
    private fun checkPermissionGallery(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // function Open Gallery
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityLauncherGallery.launch(galleryIntent)
    }

    // Request Permission Gallery
    private fun requestPermissionGallery() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            110
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openGallery()
            } else {
                Toast.makeText(this, "Ijin gallery ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Generate Image File
    private fun generateFileImage(uri: Uri) {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()

            val orientation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getOrientation2(uri)
            } else {
                getOrientation(uri)
            }
            val file = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                createImageFile()
            } else {
                //File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}" + File.separator + "BurgerBangor", getNewFileName())
                File(externalCacheDir?.absolutePath, getNewFileName())
            }

            val fos = FileOutputStream(file)
            var bitmap = image

            if (orientation != -1 && orientation != 0) {

                val matrix = Matrix()
                when (orientation) {
                    6 -> matrix.postRotate(90f)
                    3 -> matrix.postRotate(180f)
                    8 -> matrix.postRotate(270f)
                    else -> matrix.postRotate(orientation.toFloat())
                }
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            binding.ivProfile.setImageBitmap(bitmap)
            filePhoto = file
        } catch (e: Exception) {
            e.printStackTrace()
            binding.root.snacked("File ini tidak dapat digunakan")
        }
    }

    @SuppressLint("Range")
    private fun getOrientation(shareUri: Uri): Int {
        val orientationColumn = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur = contentResolver.query(
            shareUri,
            orientationColumn,
            null,
            null,
            null
        )
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            if (cur.columnCount > 0) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
            }
            cur.close()
        }
        return orientation
    }

    @SuppressLint("NewApi")
    private fun getOrientation2(shareUri: Uri): Int {
        val inputStream = contentResolver.openInputStream(shareUri)
        return getOrientation3(inputStream)
    }

    @SuppressLint("NewApi")
    private fun getOrientation3(inputStream: InputStream?): Int {
        val exif: ExifInterface
        var orientation = -1
        inputStream?.let {
            try {
                exif = ExifInterface(it)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return orientation
    }

    //function Create Image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = DateTimeHelper().createAtLong().toString()
        val storageDir =
            getAppSpecificAlbumStorageDir(Environment.DIRECTORY_DOCUMENTS, "Attachment")
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    //Get Image File Name
    private fun getNewFileName(isPdf: Boolean = false): String {
        val timeStamp = DateTimeHelper().createAtLong().toString()
        return if (isPdf) "PDF_${timeStamp}_.pdf" else "JPEG_${timeStamp}_.jpg"
    }

    //Get Image Album Storage
    private fun getAppSpecificAlbumStorageDir(albumName: String, subAlbumName: String): File {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(getExternalFilesDir(albumName), subAlbumName)
        if (!file.mkdirs()) {
         //do nothing
        }
        return file
    }

    private suspend fun compressFile(filePhoto: File): File? {
        println("Compress 1")
        return try {
            println("Compress 2")
            Compressor.compress(this, filePhoto) {
                resolution(720, 720)
                quality(50)
                format(Bitmap.CompressFormat.JPEG)
                size(514)
            }
        } catch (e: Exception) {
            println("Compress 3")
            tos("Gagal kompress")
            e.printStackTrace()
            null
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (filePhoto != null ) {
            unsavedAlert()
            return
        }
        if (binding.etName.textOf().isNotEmpty() && binding.etName.textOf() != userName) {
            unsavedAlert()
            return
        }
        if (schoolId != null || binding.autoCompleteSpinner.textOf() != schoolName ){
            unsavedAlert()
            return
        }
        finish()
    }

    private fun unsavedAlert(){
        val builder = AlertDialog.Builder(this@EditProfileActivity)
        builder.setTitle("Unsaved Changes")
        builder.setMessage("You have unsaved changes. Are you sure you want to Dismiss changes?.")
            .setPositiveButton("Dismiss") { _, _ ->
                this@EditProfileActivity.finish()
            }
            .setNegativeButton("Keep Editing") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()

        // Set the color of the positive button text
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.my_hint_color))
        }
        dialog.show()

    }

}