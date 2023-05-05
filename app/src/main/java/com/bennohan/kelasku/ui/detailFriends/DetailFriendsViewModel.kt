package com.bennohan.kelasku.ui.detailFriends

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.User
import com.crocodic.core.api.ApiCode
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.extension.toObject
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class DetailFriendsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : BaseViewModel() {

    private var _friends = MutableSharedFlow<User?>()
    var friends = _friends.asSharedFlow()

    fun getFriends(id: Int) = viewModelScope.launch {
        ApiObserver({ apiService.getUserId(id) },
            false, object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<User>(gson)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                    _friends.emit(data)
                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


    fun getNotificationColek( userId: Int?) = viewModelScope.launch {
        ApiObserver({ apiService.getNotification(userId) },
            false, object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess())

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun like (userId: Int) = viewModelScope.launch {
        ApiObserver({ apiService.likeUser(userId) },
            false, object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("liked"))

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }

    fun dislike (userId: Int?) = viewModelScope.launch {
        ApiObserver({ apiService.dislikeUser(userId) },
            false, object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess("disliked"))
                }
                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())
                }
            })
    }


}