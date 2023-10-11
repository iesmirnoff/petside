package com.example.petside.network
import com.example.petside.data.SignUpRequestModel
import com.example.petside.data.SignUpResponseModel
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface AppApi {
    @POST("user/passwordlesssignup")
    fun signUp(@Body body: SignUpRequestModel): Observable<SignUpResponseModel>
}
