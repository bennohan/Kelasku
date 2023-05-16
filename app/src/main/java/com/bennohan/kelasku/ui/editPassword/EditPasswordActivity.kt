package com.bennohan.kelasku.ui.editPassword

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.databinding.ActivityEditPasswordBinding
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.isEmptyRequired
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditPasswordActivity : BaseActivity<ActivityEditPasswordBinding,EditPasswordViewModel>(R.layout.activity_edit_password) {

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)


        getUser()
        observe()

        binding.btnBack.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        binding.btnEditPassword.setOnClickListener {
            editPassword()
        }
        binding.btnBack.setOnClickListener {
            if(binding.etCurrentPassword.textOf().isNotEmpty() || binding.etPasswordNew.textOf().isNotEmpty() || binding.etConfirmPass.textOf().isNotEmpty()){
                unsavedAlert()
                return@setOnClickListener
            }
            finish()
        }

    }

    private fun getUser() {
        val user = session.getUser()
        binding.user = user
    }

    private fun observe(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("Updating")
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                tos("Updating Success")
                                finish()
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                binding.root.snacked(it.message ?: return@collect)
                                loadingDialog.setResponse(it.message ?: return@collect)
                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
            }
        }
    }


    private fun editPassword () {

        val currentPassword = binding.etCurrentPassword.textOf()
        val newPassword = binding.etPasswordNew.textOf()
        val confirmPassword = binding.etConfirmPass.textOf()

        if ( binding.etCurrentPassword.isEmptyRequired(R.string.mustFillCurrentPassword) || binding.etPasswordNew.isEmptyRequired(R.string.mustFillNewPassword)||binding.etConfirmPass.isEmptyRequired(R.string.mustFillConfirmPassword)){
            return
        }

        if (newPassword != confirmPassword){
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            binding.tvPasswordLength.visibility = View.GONE
//            binding.textInputConfirmPassword.error = "Password Not Match"
        } else{
            binding.tvPasswordNotMatch.visibility = View.GONE
            binding.tvPasswordLength.visibility = View.GONE
            if (newPassword.length >=6){
                binding.tvPasswordNotMatch.visibility = View.GONE
                binding.tvPasswordLength.visibility = View.GONE
                viewModel.editPassword(currentPassword, newPassword)
            } else {
                //password does not meet minimum
                binding.tvPasswordNotMatch.visibility = View.GONE
                binding.tvPasswordLength.visibility = View.VISIBLE
            }
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(binding.etCurrentPassword.textOf().isNotEmpty() || binding.etPasswordNew.textOf().isNotEmpty() || binding.etConfirmPass.textOf().isNotEmpty()){
            unsavedAlert()
            return
        }
        finish()
    }


    private fun unsavedAlert(){
        val builder = AlertDialog.Builder(this@EditPasswordActivity)
        builder.setTitle("Unsaved Changes")
        builder.setMessage("You have unsaved changes. Are you sure you want to Dismiss changes?.")
            .setPositiveButton("Dismiss") { _, _ ->
                this@EditPasswordActivity.finish()
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