package com.bennohan.kelasku.ui.editProfile

import androidx.lifecycle.viewModelScope
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.base.BaseViewModel
import com.bennohan.kelasku.data.School
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.User
import com.crocodic.core.api.ApiCode
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.extension.toList
import com.crocodic.core.extension.toObject
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel
@Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val session: Session,
) : BaseViewModel() {

    private var _dataSchool = MutableSharedFlow<List<School?>>()
    var dataSchool = _dataSchool.asSharedFlow()


    fun editProfile(
        name: String?,
        schoolId: String?,
    ) = viewModelScope.launch {
        _apiResponse.emit(ApiResponse().responseLoading())
        ApiObserver({ apiService.editProfile(name,schoolId) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val data = response.getJSONObject(ApiCode.DATA).toObject<User>(gson)
                    _apiResponse.emit(ApiResponse().responseSuccess())
                    session.saveUser(data)

                }

                override suspend fun onError(response: ApiResponse) {
                    super.onError(response)
                    _apiResponse.emit(ApiResponse().responseError())

                }
            })
    }

    //Function Update Profile Photo
    fun updateUserWithPhoto(name: String,schoolId: String?, photo: File) =
        viewModelScope.launch {
            val fileBody = photo.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("foto", photo.name, fileBody)
            ApiObserver({ apiService.editProfilePhoto(name, schoolId, filePart) },
                false, object : ApiObserver.ResponseListener {
                    override suspend fun onSuccess(response: JSONObject) {
                        val data = response.getJSONObject(ApiCode.DATA).toObject<User>(gson)
                        _apiResponse.emit(ApiResponse().responseSuccess())
                        session.saveUser(data)
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