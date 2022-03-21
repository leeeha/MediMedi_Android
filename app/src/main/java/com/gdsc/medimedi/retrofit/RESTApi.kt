package com.gdsc.medimedi.retrofit

import com.gdsc.medimedi.retrofit.*
import com.google.android.gms.auth.api.credentials.IdToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface RESTApi {

    @POST("user")
    suspend fun googleLogin(@Body loginRequest: LoginRequest): LoginResponse?

    @DELETE("user")
    suspend fun signOut(@Body token: IdToken): LoginResponse?


    companion object {
        var gson = GsonBuilder().setLenient().create()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://3.38.255.253:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

//    @POST("user")
//    fun googleLogin(@Body loginRequest: LoginRequest): Call<LoginResponse>

//    @POST("api/auth/check") // todo: 백엔드 check 한번
//    fun checkLoggedIn( @Body token: String?): BasicResponse?

    @POST("search")
    fun getSearchResult(@Body searchRequest: SearchRequest): Call<SearchResponse>

    @GET("history/list/{token}")
    fun getSearchHistory(@Path("token") token: String?): Call<HistoryResponse>

    @GET("history/detail/{id}")
    fun getHistoryDetail(@Path("id") id: Int): Call<DetailResponse>
}