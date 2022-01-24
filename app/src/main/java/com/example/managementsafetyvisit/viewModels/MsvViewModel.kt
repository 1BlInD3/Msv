package com.example.managementsafetyvisit.viewModels

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.managementsafetyvisit.interfaces.MsvListener
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import com.example.managementsafetyvisit.retrofit.SendApi
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "MsvViewModel"

@HiltViewModel
class MsvViewModel
    @Inject
    constructor(
        private val retro: RetrofitFunctions
        ): ViewModel() {

    var msvListener: MsvListener? = null

    fun getPhoto(name: String){
        SendApi().getImage(name).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                    Log.d(TAG, "onResponse: ${bmp::class.simpleName}")
                    msvListener?.sendImageBitmap(bmp)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }

        })
    }
}