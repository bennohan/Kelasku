package com.bennohan.kelasku.ui.login

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.constant.Const
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val session: Session,
) : BaseViewModel() {

    //Login Function
    fun login(
        phone: String,
        password: String,
        deviceToken: String
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.login(phone, password, deviceToken) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val token = response.getJSONObject("data").getJSONObject("token").getString("access_token")
                    session.setValue(Const.TOKEN.API_TOKEN,token)
                    _apiResponse.emit(ApiResponse().responseSuccess())

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())

                }
            })
    }

}