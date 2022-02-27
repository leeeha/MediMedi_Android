package com.gdsc.medimedi.retrofit

import retrofit2.Callback

// 로그인(회원가입) 클래스
data class LoginRequest(
    val token: String, val name: String, val email: String
)
data class LoginResponse(
    val success: Boolean, val data: Unit
) {
    fun enqueue(callback: Callback<LoginResponse>) {

    }
    override fun toString(): String {
        return "LoginResponse{" +
                "success=" + success +
                ", data" + data
        '}'
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