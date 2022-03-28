package com.gdsc.medimedi.retrofit

import com.gdsc.medimedi.model.Check
import com.gdsc.medimedi.model.History
import com.gdsc.medimedi.model.MedicineInfo
import com.google.gson.annotations.SerializedName

// 로그인
data class LoginRequest(
    val token: String?,
    val name: String?,
    val email: String?
)
data class LoginResponse(
    val success: Boolean,
    val data: Unit
)

data class CheckResponse(
    val success: Boolean,
    val data: Check
)


// 검색
data class SearchRequest(
    val token: String?,
    @SerializedName("image_url")
    val imageUrl: String?
)
data class SearchResponse(
    val success: Boolean,
    val data: MedicineInfo
)

// 기록 조회
data class HistoryResponse(
    val success: Boolean,
    val data: MutableList<History>
)

// 기록 상세 조회
data class DetailResponse(
    val success: Boolean,
    val data: MedicineInfo
)