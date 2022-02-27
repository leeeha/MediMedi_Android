package com.gdsc.medimedi

import com.gdsc.medimedi.retrofit.*
import com.google.android.gms.auth.api.credentials.IdToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface RESTApi {

    @POST("user")
    suspend fun googleLogin(@Body loginRequest: LoginRequest): LoginResponse?

//    @POST("api/auth/check") //todo list : 백엔드 check 한번
//    fun checkLoggedIn( @Body token: String?): BasicResponse?



//
    companion object {
        val gson: Gson = GsonBuilder().setLenient().create()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://3.38.255.253:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }
}