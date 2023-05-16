package com.bennohan.kelasku.ui.register

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.School
import com.bennohan.kelasku.databinding.ActivityRegisterBinding
import com.bennohan.kelasku.ui.login.LoginActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity :
    BaseActivity<ActivityRegisterBinding, RegisterViewModel>(R.layout.activity_register) {

    private var dataSchool = ArrayList<School?>()
    private var schoolId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)

        getListSchool()
        autocompleteSpinner()
//        hintColor()
        observe()
        validateRegister()


        binding.btnBack.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            register()
        }


    }

    private fun hintColor() {
        val hintColor = ContextCompat.getColor(this, R.color.my_hint_color)
        binding.textInputName.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputPhone.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputSchool.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputConfirmPassword.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputPassword.defaultHintTextColor = ColorStateList.valueOf(hintColor)
    }


    private fun getListSchool() {
        viewModel.schoolList()
    }

    private fun isValidPasswordLength(password: String): Boolean {
        return password.length >= 6
    }

    private fun validatePassword(){
        if (!isValidPasswordLength(binding.etPassword.textOf())){
            binding.tvPasswordLength.visibility = View.VISIBLE
            return
        }else{
            binding.tvPasswordLength.visibility = View.GONE
        }
        if (binding.etPassword.textOf() != binding.etConfirmPass.textOf()){
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            return
        }else{
            binding.tvPasswordNotMatch.visibility = View.GONE
        }
    }

    private fun validateRegister() {


        binding.etPassword.doAfterTextChanged {
           validatePassword()
        }

        binding.etConfirmPass.doAfterTextChanged {
            validatePassword()
            if (binding.etPassword.textOf().isEmpty()){
                binding.etPassword.error = "Password Tidak Boleh Kosong"
                binding.tvPasswordNotMatch.visibility = View.GONE
            }
        }

    }


    private fun register() {
        val name = binding.etName.textOf()
        val phone = binding.etPhone.textOf()
        val password = binding.etPassword.textOf()


        if (binding.etName.isEmptyRequired(R.string.mustFill) || binding.etPhone.isEmptyRequired(
                R.string.mustFill
            ) || binding.autoCompleteSpinner.isEmptyRequired(R.string.mustFill)
            || binding.etPassword.isEmptyRequired(R.string.mustFillPassword) || binding.etConfirmPass.isEmptyRequired(
                R.string.mustFillConfirmPassword
            )
        ) {
            return
        }

        fun isValidIndonesianPhoneNumber(phoneNumber: String): Boolean {
            val regex = Regex("^\\+62\\d{9,15}$|^0\\d{9,11}$")
            return regex.matches(phoneNumber)
        }
        if (!isValidIndonesianPhoneNumber(phone)) {
            // Nomor Telephone valid
            binding.etPhone.error = "Nomor Telephone Tidak Valid"
            return
        }
        if (binding.etPassword.textOf() == binding.etConfirmPass.textOf()) {
            // Password is valid
            binding.tvPasswordLength.visibility = View.GONE
            binding.tvPasswordNotMatch.visibility = View.GONE
        } else {
            // Password is too short
            binding.tvPasswordLength.visibility = View.GONE
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            return
        }

        viewModel.register(name,phone, schoolId, password)
    }


    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        //when(it.message){}
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("Please Wait...")
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                tos("Register Success")
                                openActivity<LoginActivity>()
                                finish()
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                binding.root.snacked("Register Failed")
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
                launch {
                    viewModel.dataSchool.collect { school ->
                        dataSchool.addAll(school)
//                        val items = apiResponse.items.toList()
                        // Call a function to set the spinner items with the retrieved data
                    }
                }
            }
        }
    }

    private fun autocompleteSpinner() {

        val autoCompleteSpinner = findViewById<AutoCompleteTextView>(R.id.autoCompleteSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataSchool)
        autoCompleteSpinner.setAdapter(adapter)
        binding.autoCompleteSpinner.setTextColor(ContextCompat.getColor(this,R.color.black))


        // Show the dropdown list when the AutoCompleteTextView is clicked
        autoCompleteSpinner.setOnClickListener {
            autoCompleteSpinner.showDropDown()
            autoCompleteSpinner.dropDownVerticalOffset = -autoCompleteSpinner.height

        }

        autoCompleteSpinner.setOnItemClickListener { _, _, position, _ ->
            // Handle item selection here
            val selectedItem = dataSchool[position]
            schoolId = selectedItem?.sekolahId


        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.etName.textOf().isNotEmpty() || binding.etPhone.textOf()
                .isNotEmpty() || binding.etPassword.textOf()
                .isNotEmpty() || binding.etConfirmPass.textOf().isNotEmpty()
        ) {
            unsavedAlert()
            return
        }
        if (schoolId != null) {
            unsavedAlert()
            return
        }
        finish()
    }

    private fun unsavedAlert() {
        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle("Unsaved Changes")
        builder.setMessage("You have unsaved changes. Are you sure you want to Dismiss changes?.")
            .setPositiveButton("Dismiss") { _, _ ->
                this@RegisterActivity.finish()
            }
            .setNegativeButton("Keep Editing") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()

        // Set the color of the positive button text
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.my_hint_color))
        }
        dialog.show()

    }





}