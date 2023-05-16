package com.bennohan.kelasku.api

import okhttp3.MultipartBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    //login
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("nomor_telepon") phone: String?,
        @Field("password") password: String?,
        @Field("device_token") deviceToken : String,
    ): String

    @POST("api/data_user")
    suspend fun getUser(): String

    @FormUrlEncoded
    @POST("api/register")
    suspend fun register(
        @Field("nama") name: String?,
        @Field("nomor_telepon") phone: String?,
        @Field("sekolah_id") schoolId: Int?,
        @Field("password") password: String?
        ): String

    @GET("api/listsekolah")
    suspend fun getListSchool(): String

    @GET("api/myfriend")
    suspend fun getListUser(
//        @Path("like_by_you") like_by_you : Boolean?
    ): String

    @GET("api/user/{user_id}")
    suspend fun getUserId(
        @Path("user_id")user_id : Int?
    ): String

    @FormUrlEncoded
    @POST("api/editprofile")
    suspend fun editProfile(
        @Field("nama") name: String?,
        @Field("sekolah_id")schoolId: String?,
    ) : String

    @Multipart
    @POST("api/editprofile")
    suspend fun editProfilePhoto(
        @Part("nama") name: String?,
            @Part("sekolah_id")schoolId: String?,
        @Part photo : MultipartBody.Part?
    ) : String

    @POST("api/logout")
    suspend fun logout(): String

    @FormUrlEncoded
    @POST("api/editpassword")
    suspend fun editPassword(
        @Field("current_password") currentPassword: String?,
        @Field("new_password") newPassword: String?,
    ): String

    @POST("api/ben/like/{user_id}")
    suspend fun likeUser(
        @Path("user_id") userId : Int?
    ) : String

    @POST("api/unlike/{user_id}")
    suspend fun dislikeUser(
        @Path("user_id") userId : Int?
    ) : String

    @POST("api/bennotif/{user_id}")
    suspend fun getNotification(
        @Path("user_id")user_id : Int?
    ): String

}