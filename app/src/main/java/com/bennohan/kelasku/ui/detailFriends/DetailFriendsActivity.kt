package com.bennohan.kelasku.ui.detailFriends

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.databinding.ActivityDetailFriendsBinding
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.snacked
import com.crocodic.core.extension.tos
import com.crocodic.core.helper.ImagePreviewHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailFriendsActivity :
    BaseActivity<ActivityDetailFriendsBinding, DetailFriendsViewModel>(R.layout.activity_detail_friends) {

    @Inject
    lateinit var session: Session
    private var phone: String? = null
    private var userId: Int? = null
    private var userLike: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        observe()
        getFriends()

        binding.btnWhatsapp.setOnClickListener {
            onChatButtonClick()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnColek.setOnClickListener {
            tos("clicked")
            colek()
        }

        binding.btnLike.setOnClickListener {
            likeDislike()
        }

        binding.ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(binding.ivProfile, binding.user?.foto)
        }

    }

    private fun getFriends() {
        val data = intent.getIntExtra(Const.FRIENDS.ID, 0)
        viewModel.getFriends(data)

    }

    private fun colek() {
        viewModel.getNotificationColek(userId)
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                if (it.message == "liked"){
                                    userId?.let { it1 -> viewModel.getFriends(it1) }
                                    binding.root.snacked("Friends Liked")
                                }
                                if (it.message == "disliked"){
                                    userId?.let { it1 -> viewModel.getFriends(it1) }
                                    binding.root.snacked("Friends Disliked")
                                }
                                loadingDialog.dismiss()
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
                    viewModel.friends.collect { friends ->
                        binding.user = friends
                        phone = friends?.nomorTelepon
                        userId = friends?.userId
                        userLike = friends?.likeByYou

                    }
                }
            }
        }
    }

    private fun onChatButtonClick() {
        val phoneNumber = "+62 + $phone"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun likeDislike() {
        if (userLike != true) {
            userId?.let { viewModel.like(it) }
            setResult(6100)
        } else {
            viewModel.dislike(userId)
            setResult(6100)

        }

    }

}


