package com.gdsc.medimedi.retrofit

import com.gdsc.medimedi.model.MedicineInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface RESTApi {
    companion object {
        var okHttpClient = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS) // socket timeout
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://3.35.92.107:8080/api/")
            .client(okHttpClient)
            // 요청·응답 시 JSON <-> 자바 객체 간의 형변환을 해주는 gson 컨버터
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @POST("user")
    fun sendUserInfo(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("search")
    fun getSearchResult(@Body searchRequest: SearchRequest): Call<SearchResponse>

    @GET("history/list/{token}")
    fun getSearchHistory(@Path("token") token: String?): Call<HistoryResponse>

    @GET("history/detail/{id}")
    fun getHistoryDetail(@Path("id") id: Int): Call<DetailResponse>
}