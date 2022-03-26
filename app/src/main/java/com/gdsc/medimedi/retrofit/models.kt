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
    val name: String, // 제품명
    val entp: String, // 제약 회사명
    val effect: String, // 효능효과
    @SerializedName("using_method")
    val usingMethod: String, // 사용법
    val caution: String, // 주의사항
    val notice: String, // 경고
    val interact: String, // 다른 약과의 상호작용
    @SerializedName("side_effect")
    val sideEffect: String, // 부작용
    @SerializedName("storage_method")
    val storageMethod: String // 보관방법
)