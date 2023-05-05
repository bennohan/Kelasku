package com.bennohan.kelasku.ui.editPassword

import android.os.Bundle
import android.view.View
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditPasswordActivity : BaseActivity<ActivityEditPasswordBinding,EditPasswordViewModel>(R.layout.activity_edit_password) {

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        getUser()
        observe()

        binding.btnEditPassword.setOnClickListener {
            editPassword()
        }
        binding.btnBack.setOnClickListener {
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
                        //when(it.message){}
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("Updating")
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                loadingDialog.setResponse(it.message ?: return@collect)
                                binding.root.snacked("Updating Success")
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                binding.root.snacked("Updating Failed")
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


}