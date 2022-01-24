package com.example.managementsafetyvisit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.fragment.MsvFragment
import com.example.managementsafetyvisit.fragment.PerceptionFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),MsvFragment.MainActivityConnector,PerceptionFragment.MainActivityInteract {

    companion object{
        val observationArray: ArrayList<ObservationData> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        getMsvFragment()
    }
    private fun getMsvFragment(){
        val msvFragment = MsvFragment()
        supportFragmentManager.beginTransaction().replace(R.id.id_container,msvFragment,"MSVFRAG").commit()
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

}