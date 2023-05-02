package com.bennohan.kelasku.ui.profile

import android.os.Bundle
import android.view.View
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.databinding.ActivityProfileBinding
import com.bennohan.kelasku.ui.editProfile.EditProfileActivity
import com.crocodic.core.extension.createIntent
import com.crocodic.core.helper.ImagePreviewHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity :
    BaseActivity<ActivityProfileBinding, ProfileViewModel>(R.layout.activity_profile) {

    @Inject
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUser()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        binding.btnBack.setOnClickListener {
            finish()
        }

//        binding.btnEditProfile.setOnClickListener {
//            openActivity<EditProfileActivity>()
//        }

        binding.ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(binding.ivProfile, binding.user?.foto)
        }

        binding.btnEditProfile.setOnClickListener {
            activityLauncher.launch(createIntent<EditProfileActivity>()) {
                if (it.resultCode == 1234) {
                    getUser()
                }
            }
        }


    }

    private fun getUser() {
        val user = session.getUser()
        binding.user = user
    }

}