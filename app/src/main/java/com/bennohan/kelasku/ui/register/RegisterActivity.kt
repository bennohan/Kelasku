package com.bennohan.kelasku.ui.register

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
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

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        getListSchool()
        autocompleteSpinner()
        hintColor()
        observe()


        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun hintColor(){
        val hintColor = ContextCompat.getColor(this, R.color.my_hint_color)
        binding.textInputName.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputPhone.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputSchool.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputConfirmPassword.defaultHintTextColor = ColorStateList.valueOf(hintColor)
        binding.textInputPassword.defaultHintTextColor = ColorStateList.valueOf(hintColor)

    }


    private fun getListSchool(){
        viewModel.schoolList()
    }

    private fun register() {
        val name = binding.etName.textOf()
        val phone = binding.etPhone.textOf()
        val password = binding.etPassword.textOf()
        val confirm = binding.etConfirmPass.textOf()


        if (binding.etName.isEmptyRequired(R.string.mustFill) || binding.etPhone.isEmptyRequired(R.string.mustFill)||binding.autoCompleteSpinner.isEmptyRequired(R.string.mustFill)
            || binding.etPassword.isEmptyRequired(R.string.mustFillNewPassword) || binding.etConfirmPass.isEmptyRequired(R.string.mustFillConfirmPassword))
        {
            return
        }

        if (password != confirm){
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            binding.tvPasswordLength.visibility = View.GONE
        } else{
            binding.tvPasswordNotMatch.visibility = View.GONE
            binding.tvPasswordLength.visibility = View.GONE
            if (password.length >=6){
                binding.tvPasswordLength.visibility = View.GONE
                binding.tvPasswordNotMatch.visibility = View.GONE
                viewModel.register(name, phone, schoolId, password)
            } else {
                //password does not meet minimum
                binding.tvPasswordNotMatch.visibility = View.GONE
                binding.tvPasswordLength.visibility = View.VISIBLE
            }
        }


    }



    private fun observe(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        //when(it.message){}
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("Register....in")
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                openActivity<LoginActivity>()
                                finish()
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                binding.root.snacked("Register Failed")

//                                tos(it.message ?: return@collect)
                                loadingDialog.setResponse(it.message ?: return@collect)
//                                loadingDialog.dismiss()

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
                launch {
                    viewModel.dataSchool.collect{ school->
                        dataSchool.addAll(school)
//                        val items = apiResponse.items.toList()
                        // Call a function to set the spinner items with the retrieved data
                    }
                }
            }
        }
    }

    private fun autocompleteSpinner(){

        val autoCompleteSpinner = findViewById<AutoCompleteTextView>(R.id.autoCompleteSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataSchool)
        autoCompleteSpinner.setAdapter(adapter)
        binding.autoCompleteSpinner.setTextColor(resources.getColor(R.color.black))


        // Show the dropdown list when the AutoCompleteTextView is clicked
        autoCompleteSpinner.setOnClickListener {
            autoCompleteSpinner.showDropDown()
            autoCompleteSpinner.setDropDownVerticalOffset(-autoCompleteSpinner.height)

        }

        autoCompleteSpinner.setOnItemClickListener { parent, view, position, id ->
            // Handle item selection here
            val selectedItem = dataSchool[position]
             schoolId = selectedItem?.sekolahId


        }

    }

}