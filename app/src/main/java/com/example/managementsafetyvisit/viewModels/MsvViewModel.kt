package com.example.managementsafetyvisit.viewModels

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.managementsafetyvisit.interfaces.MsvListener
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import com.example.managementsafetyvisit.retrofit.SendApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    var firstName: String = ""
    var middleName: String = ""
    var middleMiddleName: String = ""
    var familyName: String = ""
    var tsz: String = ""
    var firstNameCommissar: String = ""
    var middleCommissarName: String = ""
    var middleMiddleCommissarName: String = ""
    var familyNameCommissar: String = ""
    var firstNameChastnik: String = ""
    var middleChastinkName: String = ""
    var middleMiddleChastnikName: String = ""
    var familyNameChastnik: String = ""
    var datum: String = ""
    var location = ""
    var msvNumber = ""

    fun getPhoto(name: String){
        msvListener?.setProgressOn()
        SendApi().getImage(name).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                    Log.d(TAG, "onResponse: ${bmp::class.simpleName}")
                    msvListener?.sendImageBitmap(bmp)
                    CoroutineScope(Main).launch {
                        msvListener?.setProgressOff()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }

        })
    }
}