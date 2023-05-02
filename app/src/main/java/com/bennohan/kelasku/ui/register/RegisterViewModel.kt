package com.bennohan.kelasku.ui.register

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.School
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
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
) : BaseViewModel() {

    private var _dataSchool = MutableSharedFlow<List<School?>>()
    var dataSchool = _dataSchool.asSharedFlow()


    fun register(
        name: String,
        phone: String,
        schoolId: Int?,
        password: String?
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.register(name, phone, schoolId, password) },
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

    fun schoolList() = viewModelScope.launch {
        ApiObserver({ apiService.getListSchool() }, false, object : ApiObserver.ResponseListener {
            override suspend fun onSuccess(response: JSONObject) {
                val status = response.getInt(ApiCode.STATUS)
                if (status == ApiCode.SUCCESS) {
                    val data = response.getJSONArray(ApiCode.DATA).toList<School>(gson)
                    _dataSchool.emit(data)

                }
            }
        })
    }


}
