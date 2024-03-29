package com.bennohan.kelasku.ui.profile

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.Session
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
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val session: Session,
    ) : BaseViewModel() {

    private var _user = MutableSharedFlow<User?>()
    var user = _user.asSharedFlow()


    //getUser
    fun getUser () = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.getUser() },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<User>(gson)
                    session.saveUser(data)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                    _user.emit(data)

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())

                }
            })


    }


}