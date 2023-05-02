package com.bennohan.kelasku.ui.favourite

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.User
import com.crocodic.core.api.ApiCode
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.extension.toList
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel
@Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : BaseViewModel() {

    private var _listUser = MutableSharedFlow<List<User?>>()
    var listUser = _listUser.asSharedFlow()


    //getListFriends
    fun getListUser () = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.getListUser() },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONArray(ApiCode.DATA).toList<User>(gson)
                    _listUser.emit(data)
                    _apiResponse.emit(ApiResponse().responseSuccess())

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())

                }
            })


    }

}