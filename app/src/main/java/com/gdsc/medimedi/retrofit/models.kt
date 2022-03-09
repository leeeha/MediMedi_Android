package com.gdsc.medimedi.retrofit

import com.gdsc.medimedi.model.History
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val token: String,
    val name: String,
    val email: String
)

data class LoginResponse(
    val success: Boolean,
    val data: Unit
) {
    override fun toString(): String {
        return "LoginResponse{" +
                "success=" + success +
                ", data=" + data + "}"
    }
}

data class UserResponse(
    val id: Int,
    val email: String,
    val name: String
)

data class IdBody(
    val id: Int
)

data class SearchRequest(
    val token: String?, // nullable
    @SerializedName("image_url")
    val imageUrl: String?
)

data class SearchResponse(
    val success: Boolean,
    val data: MedicineInfo
)

data class MedicineInfo(
    val name: String, // 제품명
    val entp: String, // 제약 회사명
    val effect: String, // 효능효과
    // 서버와 클라이언트에서 정의한 변수명이 서로 대응될 수 있도록
    // cf) 서버는 snake 표기법, 클라이언트는 camel 표기법
    @SerializedName("using_method")
    val usingMethod: String, // 사용법
    val caution: String, // 주의사항
    val notice: String, // 경고
    val interact: String, // 다른 약과의 상호작용
    @SerializedName("side_effect")
    val sideEffect: String, // 부작용
    @SerializedName("storage_method")
    val storageMethod: String, // 보관방법
    val text: String // 약 검색 결과가 없는 경우, 인식한 텍스트만 읽어주기
)

data class HistoryResponse(
    val success: Boolean,
    val data: List<History>
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
