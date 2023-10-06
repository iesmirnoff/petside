package com.example.petside.network
import com.example.petside.data.SignUpModel
import retrofit2.Call
import retrofit2.http.POST

interface ApiInterface {
    @POST("signup")
    fun signUp(): Call<List<SignUpModel>>
}