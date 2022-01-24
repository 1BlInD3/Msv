package com.example.managementsafetyvisit.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SendApi {
    @Multipart
    @POST("uploadFile/{path}")
    fun sendPhoto(
        @Part("path")path: RequestBody,
        @Part xml: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("test")
    fun getTest():Call<UploadResponse>

    @GET("image")
    fun getImage(@Query("name") name: String): Call<ResponseBody>

    companion object{
        operator fun invoke():SendApi{
            return Retrofit.Builder()
                .baseUrl("http://10.0.2.149:8030/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SendApi::class.java)
        }
    }

}