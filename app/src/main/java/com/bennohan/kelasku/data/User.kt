package com.bennohan.kelasku.data


import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @Expose
    @SerializedName("api_token")
    val apiToken: String?,
    @Expose
    @SerializedName("created_at")
    val createdAt: String?,
    @Expose
    @SerializedName("foto")
    val foto: String?,
    @Expose
    @SerializedName("nama")
    val nama: String?,
    @Expose
    @SerializedName("nomor_telepon")
    val nomorTelepon: String?,
    @Expose
    @SerializedName("sekolah_id")
    val sekolahId: Int?,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String?,
    @Expose
    @SerializedName("user_id")
    val userId: Int?,
    @Expose
    @SerializedName("nama_sekolah")
    val namaSekolah: String?,
    @Expose
    @SerializedName("device_token")
    var deviceToken: String?,
    @Expose
    @SerializedName("like_by_you")
    var likeByYou : Boolean?
) : Parcelable