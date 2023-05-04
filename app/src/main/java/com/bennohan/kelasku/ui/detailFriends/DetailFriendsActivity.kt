package com.bennohan.kelasku.ui.detailFriends

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.User
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
    var phone : String? = null
    var userId : Int? = null
    var userLike : Boolean? = null
     var friend : User? = null
//    private var friendDeviceToken : String? = null


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
            if( userLike != true) {
                binding.root.snacked("Friends Liked")
                likeDislike()
                binding.btnLike.setImageResource(R.drawable.ic_baseline_favorite_24)

            } else {
                binding.root.snacked("Friends Disliked")
                likeDislike()
                binding.btnLike.setImageResource(R.drawable.ic_baseline_favorite_border_24)


            }

        }
//
//        binding.btnDislike.setOnClickListener {
//            like()
//        }

        binding.ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(binding.ivProfile, binding.user?.foto)
            }

    }

    private fun getFriends() {
        val data = intent.getIntExtra(Const.FRIENDS.ID,0)
//        if (data?.userId != null){
//            viewModel.getFriends(data.userId)
//            Log.d("cek getFriends","$data")
//        }
        viewModel.getFriends(data ?: return)

    }

    private fun colek(){
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
                                binding.user = friend
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
                    viewModel.friends.collect{ friends ->
                        friend = friends
                        binding.user = friends
                        phone = friends?.nomorTelepon
                        userId = friends?.userId
                        userLike = friends?.likeByYou

                        Log.d("cek nomor","cek nomor : $phone")
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
        if (userLike == true){
            userId?.let { viewModel.like(it) }
            setResult(Const.RELOAD)
        }else{
            viewModel.dislike(userId)
            setResult(Const.RELOAD)
        }

    }

}


