package com.example.managementsafetyvisit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.managementsafetyvisit.camera.CaptureAct
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.fragment.LoginFragment
import com.example.managementsafetyvisit.fragment.MsvFragment
import com.example.managementsafetyvisit.fragment.PerceptionFragment
import com.example.managementsafetyvisit.utils.Sql
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.annotation.Nullable
@AndroidEntryPoint
class MainActivity : AppCompatActivity(),MsvFragment.MainActivityConnector,PerceptionFragment.MainActivityInteract, LoginFragment.LoginScan {

    private val TAG = "MainActivity"

    companion object{
        val observationArray: ArrayList<ObservationData> = ArrayList()
        val dataArray: ArrayList<Data> = ArrayList()
        const val read_connect ="jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
        var felelos: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        getLoginFragment()
        dataArray.clear()
    }
    private fun getMsvFragment(){
        val msvFragment = MsvFragment()
        supportFragmentManager.beginTransaction().replace(R.id.id_container,msvFragment,"MSVFRAG").commit()
    }

    private fun getLoginFragment(){
        val login = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.id_container,login,"LOGIN").commit()
    }

    override fun loadPerceptionPanel() {
        val perceptionFragment = PerceptionFragment()
        supportFragmentManager.beginTransaction().replace(R.id.panel_container,perceptionFragment,"PERCEPTION").commit()
    }


    override fun loadPanelWithValues(
        perception: String,
        type: String,
        response: String,
        measure: String,
        urgent: Boolean,
        corrector: String?,
        date: String?
    ) {
        val perceptionFragment = PerceptionFragment.newInstance(perception,response,measure,urgent,type,corrector,date)
        supportFragmentManager.beginTransaction().replace(R.id.panel_container,perceptionFragment,"PERCEPTION").commit()
    }

    override fun getCameraToScan() {
        scanCode()
    }


    override fun closeFragment() {
        val myFragment = supportFragmentManager.findFragmentByTag("PERCEPTION")
        if (myFragment != null) {
            supportFragmentManager.beginTransaction().remove(myFragment).commit()
        }
    }

    override fun refreshList() {
        val myFragment = supportFragmentManager.findFragmentByTag("MSVFRAG")
        if (myFragment != null) {
            (myFragment as MsvFragment).refreshList()
        }
    }

    private fun scanCode() {
        val integrator = IntentIntegrator(this)
        integrator.captureActivity = CaptureAct::class.java
        integrator.setOrientationLocked(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Beolvasás folyamatban...")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                CoroutineScope(IO).launch {
                    val sql = Sql()
                    sql.getDataByName(result.contents.trim())
                    CoroutineScope(Main).launch {
                        Log.d(TAG, "onActivityResult: $dataArray")
                        val msvFragment = MsvFragment.newInstance(
                            dataArray[0].id,
                            dataArray[0].name,
                            dataArray[0].tsz,
                            dataArray[0].fsz,
                            dataArray[0].ftsz,
                            dataArray[0].resztvevo,
                            dataArray[0].rtsz,
                            dataArray[0].location,
                            dataArray[0].date,
                            dataArray[0].status
                        )
                        supportFragmentManager.beginTransaction().replace(R.id.id_container,msvFragment,"MSVFRAG").commit()
                    }
                }
            } else {
                Log.d(TAG, "onActivityResult: no result")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun openCamera() {
        scanCode()
    }
}