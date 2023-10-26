package com.example.petside.network
import com.example.petside.data.FavouriteItem
import com.example.petside.data.SignUpRequestModel
import com.example.petside.data.SignUpResponseModel
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://api.thecatapi.com/v1/"

interface AppApi {
    @POST("user/passwordlesssignup")
    fun signUp(@Body body: SignUpRequestModel): Observable<SignUpResponseModel>
    @GET("favourites")
    fun getFavourites(@Query("api_key") apiKey: String): Observable<Array<FavouriteItem>>
}

class AppClient {
    companion object {
        private var instance: AppApi? = null

        @Synchronized
        fun getInstance(): AppApi {
            if (instance == null) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)

                    .build()

                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                    .create(AppApi::class.java)
            }

            return instance!!
        }
    }
}
