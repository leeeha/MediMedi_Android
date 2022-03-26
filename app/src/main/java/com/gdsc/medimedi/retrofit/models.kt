package com.gdsc.medimedi.retrofit

import com.gdsc.medimedi.model.History
import com.gdsc.medimedi.model.MedicineInfo
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val token: String?,
    val name: String?,
    val email: String?
)

data class LoginResponse(
    val success: Boolean,
    val data: Unit
)

data class SearchRequest(
    val token: String?,
    @SerializedName("image_url")
    val imageUrl: String?
)

data class SearchResponse(
    val success: Boolean,
    val data: MedicineInfo
)

data class HistoryResponse(
    val success: Boolean,
    val data: MutableList<History>
)

data class DetailResponse(
    val success: Boolean,
    val data: MedicineInfo
)