package com.bennohan.kelasku.ui.favourite

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.User
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.databinding.ActivityFavouriteBinding
import com.bennohan.kelasku.databinding.ItemFriendsBinding
import com.bennohan.kelasku.ui.detailFriends.DetailFriendsActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.adapter.ReactiveListAdapter
import com.crocodic.core.extension.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouriteActivity : BaseActivity<ActivityFavouriteBinding,FavouriteViewModel>(R.layout.activity_favourite) {

    private val adapterUser by lazy {
        object : ReactiveListAdapter<ItemFriendsBinding, User>(R.layout.item_friends) {
            override fun onBindViewHolder(
                holder: ItemViewHolder<ItemFriendsBinding, User>, position: Int
            ) {

                val item = getItem(position)

                item?.let { itm ->
                    holder.binding.data = itm
                    holder.bind(itm)


                    holder.binding.btnViewProfile.setOnClickListener {
                        openActivity<DetailFriendsActivity> {
                            putExtra(Const.FRIENDS.ID, item)
                            putExtra(Const.FRIENDS.FRIENDS_DEVICE_TOKEN,item)
                            android.util.Log.d("cekToken","cekToken : $item")
                        }
                    }
                }
                holder.binding.cardView.cardElevation = 0f
                holder.binding.cardView.setCardBackgroundColor(Color.TRANSPARENT)



                super.onBindViewHolder(holder, position)
            }


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rvUser.adapter = adapterUser

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        observe()
        getListUser()

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun getListUser() {
        viewModel.getListUser()
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
                    viewModel.listUser.collect { listUser ->
                        val filterUser = listUser.filter { it?.likeByYou == true
                        }
                        adapterUser.submitList(filterUser)

                    }
                }
            }
        }

    }

}