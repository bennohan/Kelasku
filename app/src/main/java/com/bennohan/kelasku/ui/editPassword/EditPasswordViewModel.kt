package com.bennohan.kelasku.ui.editPassword

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class EditPasswordViewModel @Inject constructor(
    private val apiService: ApiService,
) : BaseViewModel() {

    fun editPassword(
        currentPassword: String?,
        newPassword: String?,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.editPassword(currentPassword, newPassword) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    _apiResponse.emit(ApiResponse().responseSuccess())

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())

                }
            })
    }


}