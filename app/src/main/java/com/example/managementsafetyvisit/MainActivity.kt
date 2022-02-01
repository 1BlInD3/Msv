package com.example.managementsafetyvisit

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.managementsafetyvisit.camera.CaptureAct
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.fragment.CameraFragment
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
import java.io.File
import javax.annotation.Nullable

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MsvFragment.MainActivityConnector,
    PerceptionFragment.MainActivityInteract, LoginFragment.LoginScan, Sql.SqlMessage {

    private val TAG = "MainActivity"
    private lateinit var progress: ProgressBar
    private lateinit var progressRound: ProgressBar

    companion object {
        val observationArray: ArrayList<ObservationData> = ArrayList()
        val newPerceptionArray: ArrayList<ObservationData> = ArrayList()

        // val reversedList: ArrayList<ObservationData> = ArrayList()
        val dataArray: ArrayList<Data> = ArrayList()
        const val read_connect =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
        const val write_connect =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=Termelesmonitor;password=TERM123;loginTimeout=10"
        var felelos: String = ""
        val perceptionFragment = PerceptionFragment()
        val msvFragment = MsvFragment()
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progress = findViewById(R.id.progressBar)
        progressRound = findViewById(R.id.progressBar2)
        progress.visibility = View.GONE
        progressRound.visibility = View.GONE
        Log.d(TAG, "onCreate: ")
    }

    override fun onResume() {
        super.onResume()
        getLoginFragment()
        dataArray.clear()
        val myFile : File = getExternalFilesDir(null)!!
    }
    /*private fun getMsvFragment(){
        val msvFragment = MsvFragment()
        supportFragmentManager.beginTransaction().replace(R.id.id_container,msvFragment,"MSVFRAG").commit()
    }*/

    private fun getLoginFragment() {
        val login = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.id_container, login, "LOGIN")
            .commit()
    }

    override fun loadPerceptionPanel(code: String) {
        val sql = Sql(this)
        progressRound.visibility = View.VISIBLE
        CoroutineScope(IO).launch {
            sql.loadPerceptionPanel(code)
            CoroutineScope(Main).launch {
                supportFragmentManager.beginTransaction().replace(
                    R.id.panel_container,
                    perceptionFragment, "PERCEPTION"
                ).commit()
                progressRound.visibility = View.GONE
                /* val perceptionFragment = PerceptionFragment.newInstance(
                     newPerceptionArray[0].perception,
                     newPerceptionArray[0].response,
                     newPerceptionArray[0].measure,
                     newPerceptionArray[0].now,
                     newPerceptionArray[0].type,
                     newPerceptionArray[0].corrector,
                     newPerceptionArray[0].date,
                     newPerceptionArray[0].id.trim()
                 )
                 Toast.makeText(this@MainActivity, newPerceptionArray[0].id.trim(), Toast.LENGTH_LONG).show()
                 supportFragmentManager.beginTransaction().replace(R.id.panel_container,perceptionFragment,"PERCEPTION").commit()*/
            }
        }
    }


    override fun loadPanelWithValues(
        perception: String?,
        type: String?,
        response: String?,
        measure: String?,
        urgent: Boolean,
        corrector: String?,
        date: String?,
        id: String
    ) {
        //val perceptionFragment = PerceptionFragment.newInstance(perception,response,measure,urgent,type,corrector,date,id)
        val observation: ArrayList<ObservationData> = ArrayList()
        observation.add(
            ObservationData(
                perception,
                type,
                response,
                measure,
                urgent,
                corrector,
                date,
                id
            )
        )
        val perceptionFragment = PerceptionFragment()
        val bundle = Bundle()
        bundle.putSerializable("LOADING", observation)
        perceptionFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_container, perceptionFragment, "PERCEPTION").commit()
    }

    override fun getCameraToScan() {
        scanCode()
    }

    override fun getCameraInstance() {
        val cameraFragment = CameraFragment()
        supportFragmentManager.beginTransaction().replace(R.id.panel_container,cameraFragment,"CAMERA").commit()
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

    override fun saveNewPerception(
        perception: String?,
        answer: String?,
        measure: String?,
        type: String?,
        urgent: Boolean,
        corrector: String?,
        date: String?,
        id: Int,
        statusz: Int
    ) {
        val sql = Sql(this)
        CoroutineScope(IO).launch {
            sql.saveNewPerception(perception, answer, measure, type, urgent, corrector, date, id,statusz)
            CoroutineScope(Main).launch {
                val myFrag = supportFragmentManager.findFragmentByTag("MSVFRAG")
                if (myFrag != null) {
                    (myFrag as MsvFragment).refreshList()
                }
            }
        }
    }

    override fun updateExistingPerception(
        perception: String?,
        answer: String?,
        measure: String?,
        type: String?,
        urgent: Boolean,
        corrector: String?,
        date: String?,
        id: Int,
        statusz: Int
    ) {
        val sql = Sql(this)
        CoroutineScope(IO).launch {
            sql.updateExisting(perception, answer, measure, type, urgent, corrector, date, id,statusz)
            CoroutineScope(Main).launch {
                val myFrag = supportFragmentManager.findFragmentByTag("MSVFRAG")
                if (myFrag != null) {
                    (myFrag as MsvFragment).refreshList()
                }
            }
        }
    }

    override fun deleteById(id: Int) {
        val sql = Sql(this)
        CoroutineScope(IO).launch {
            sql.deleteExisting(id)
            CoroutineScope(Main).launch {
                val myFragment = supportFragmentManager.findFragmentByTag("PERCEPTION")
                if (myFragment != null) {
                    supportFragmentManager.beginTransaction().remove(myFragment).commit()
                }
                val myMsvFragment = supportFragmentManager.findFragmentByTag("MSVFRAG")
                if(myMsvFragment != null){
                    (myMsvFragment as MsvFragment).refreshAt(id)
                    (myMsvFragment as MsvFragment).refreshList()
                }
            }
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
                progress.visibility = View.VISIBLE
                CoroutineScope(IO).launch {
                    val sql = Sql(this@MainActivity)
                    if(sql.getDataByName(result.contents.trim())){
                        CoroutineScope(Main).launch {
                            Log.d(TAG, "onActivityResult: $dataArray")
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.id_container, msvFragment, "MSVFRAG").commit()
                            progress.visibility = View.GONE
                        }
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

    override fun onStop() {
        observationArray.clear()
        newPerceptionArray.clear()
        dataArray.clear()
        super.onStop()
    }

    override fun onDestroy() {
        observationArray.clear()
        newPerceptionArray.clear()
        dataArray.clear()
        super.onDestroy()

    }

    override fun sendMessage(message: String) {
        CoroutineScope(Main).launch {
            val dialog = AlertDialog.Builder(this@MainActivity)
            dialog.setTitle("Figyelem")
            dialog.setMessage(message)
            dialog.setPositiveButton("OK") { _, _ ->
                progress.visibility = View.GONE
            }
            dialog.create()
            dialog.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
        }
    }

}