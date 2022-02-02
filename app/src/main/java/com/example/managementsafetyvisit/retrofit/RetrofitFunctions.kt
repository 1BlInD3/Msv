package com.example.managementsafetyvisit.retrofit

import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "RetrofitFunctions"

class RetrofitFunctions() {

   /* interface ResponseImage {
        fun getSavedImageResponse(message: String)
    }*/

    fun retrofitGet(file: File, path: String, number: String) {
        val response = SendApi().getTest().execute()
        val res: String = response.body()!!.message.trim()
        if (res == "OK") {
            // Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + res}")
            uploadPhoto(file, path, number)
        }
    }

    private fun uploadPhoto(file: File, path: String, number: String) {
        val body = UploadRequestBody(file, "file")
        val photoResponse = SendApi().sendPhoto(
            RequestBody.create(MediaType.parse("multipart/form-data"), path),
            MultipartBody.Part.createFormData("file", file.name, body),
            number
        ).execute()
        val xmlRes = photoResponse.body()!!.message.trim()
        if (xmlRes == "success") {
            //response.getSavedImageResponse("A kép sikeresen mentve a szerverre")
            if (file.exists()) {
                file.delete()
                Log.d("MainActivity", "onResponse: delete successful")
            }
        }
    }

    fun getPhoto(name: String){
        SendApi().getImage(name).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                    Log.d(TAG, "onResponse: ${bmp::class.simpleName}")
                    //imageGet.getImage(bmp)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: FAIL")
            }

        })
    }

}