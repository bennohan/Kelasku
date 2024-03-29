package com.bennohan.kelasku.ui.profile

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
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.databinding.ActivityProfileBinding
import com.bennohan.kelasku.ui.editProfile.EditProfileActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.createIntent
import com.crocodic.core.helper.ImagePreviewHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity :
    BaseActivity<ActivityProfileBinding, ProfileViewModel>(R.layout.activity_profile) {

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUser()
        observe()

        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)


        binding.btnBack.setOnClickListener {
            setResult(Const.RELOAD)
            finish()
        }



        binding.ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(binding.ivProfile, binding.user?.foto)
        }

        binding.btnEditProfile.setOnClickListener {
            activityLauncher.launch(createIntent<EditProfileActivity>()) {
                if (it.resultCode == 6100) {
                    getUser()
                    observe()
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
                                loadingDialog.dismiss()
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)

                        }
                    }
                }

                launch {
                    viewModel.user.collect { dataUser ->
                        binding.user = dataUser

                    }
                }

            }
        }
    }

    private fun getUser() {
        viewModel.getUser()
        setResult(Const.RELOAD)

    }

}