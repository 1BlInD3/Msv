package com.example.managementsafetyvisit.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementsafetyvisit.MainActivity.Companion.msvNumber
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.adapters.ObservationDataAdapter
import com.example.managementsafetyvisit.data.Data
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.interfaces.MsvListener
import com.example.managementsafetyvisit.databinding.FragmentMsvBinding
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import com.example.managementsafetyvisit.utils.showToast
import com.example.managementsafetyvisit.viewModels.MsvViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.lang.RuntimeException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "param7"
private const val ARG_PARAM8 = "param8"
private const val ARG_PARAM9 = "param9"
private const val ARG_PARAM10 = "param10"

@AndroidEntryPoint
class MsvFragment : Fragment(), MsvListener, ObservationDataAdapter.CurrentSelection {

    private var p1 = 0
    private var p2 = ""
    private var p3 = 0
    private var p4 = ""
    private var p5 = 0
    private var p6 = ""
    private var p7 = 0
    private var p8 = ""
    private var p9 = ""
    private var p10 = 0


    private var familyName: String = ""
    private var firstName: String = ""
    private var middleName: String = ""
    private var middleMiddleName: String = ""
    private val capitals: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            p1 = it.getInt(ARG_PARAM1)
            p2 = it.getString(ARG_PARAM2).toString()
            p3 = it.getInt(ARG_PARAM3)
            p4 = it.getString(ARG_PARAM4).toString()
            p5 = it.getInt(ARG_PARAM5)
            p6 = it.getString(ARG_PARAM6).toString()
            p7 = it.getInt(ARG_PARAM7)
            p8 = it.getString(ARG_PARAM8).toString()
            p9 = it.getString(ARG_PARAM9).toString()
            p10 = it.getInt(ARG_PARAM10)
        }
    }

    private val viewModel: MsvViewModel by viewModels()
    private lateinit var binding: FragmentMsvBinding
    private val reversedList: ArrayList<ObservationData> = ArrayList()

    interface MainActivityConnector {
        fun loadPerceptionPanel(code: String)
        fun loadPanelWithValues(
            perception: String?,
            type: String?,
            response: String?,
            measure: String?,
            urgent: Boolean,
            corrector: String?,
            date: String?,
            id: String
        )

        fun getCameraToScan()
        fun getCameraInstance()
        fun closeMsv(statusz: Int, id: Int)
    }

    private lateinit var mainActivityConnector: MainActivityConnector

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_msv, container, false)
        binding.viewModel = viewModel
        viewModel.msvListener = this
        binding.observationRecycler?.adapter = ObservationDataAdapter(reversedList, this)
        binding.observationRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding.observationRecycler?.setHasFixedSize(true)
        binding.imageProgress?.visibility = View.GONE
        binding.newResponse?.setOnClickListener {
            mainActivityConnector.loadPerceptionPanel(viewModel.msvNumber.trim())
        }
        binding.cameraButton?.setOnClickListener {
            val retro = RetrofitFunctions()
            CoroutineScope(IO).launch {
                val number = retro.getImageCount("MSV_$msvNumber")
                CoroutineScope(Main).launch {
                    showToast(number,requireContext())
                }
            }
            msvNumber = viewModel.msvNumber.trim()
            mainActivityConnector.getCameraInstance()
        }
        binding.checkButton?.setOnClickListener {
            mainActivityConnector.closeMsv(2,viewModel.msvNumber.trim().toInt())
        }
       /* binding.imageView.setOnClickListener {
            viewModel.getPhoto("${viewModel.familyName}${viewModel.firstName}${viewModel.tsz}.jpg")
            Toast.makeText(requireContext(), "Frissítés", Toast.LENGTH_SHORT).show()
        }*/
        return binding.root
    }

    override fun sendImageBitmap(image: Bitmap) {
        CoroutineScope(Main).launch {
            binding.imageView.setImageBitmap(image)
        }
    }

    override fun setProgressOn() {
        binding.imageProgress?.visibility = View.VISIBLE
    }

    override fun setProgressOff() {
        binding.imageProgress?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadData()
        //viewModel.getPhoto("BálindAttila1557.jpg")
        viewModel.msvNumber = p1.toString().trim()
        nameGenerator(p2)
        if (capitals.size == 2) {
            viewModel.familyName = familyName
            viewModel.firstName = firstName
        } else if (capitals.size == 3) {
            viewModel.familyName = familyName
            viewModel.middleName = middleName
            viewModel.firstName = firstName
        } else if (capitals.size == 4) {
            viewModel.familyName = familyName
            viewModel.middleName = middleName
            viewModel.middleMiddleName = "$middleMiddleName  "
            viewModel.firstName = firstName
        }
        capitals.clear()
        viewModel.tsz = p3.toString().trim()
        nameGenerator(p4)
        if (capitals.size == 2) {
            viewModel.familyNameCommissar = familyName
            viewModel.firstNameCommissar = firstName
        } else if (capitals.size == 3) {
            viewModel.familyNameCommissar = familyName
            viewModel.middleCommissarName = middleName
            viewModel.firstNameCommissar = firstName
        } else if (capitals.size == 4) {
            viewModel.familyNameCommissar = familyName
            viewModel.middleCommissarName = middleName
            viewModel.middleCommissarName = "$middleMiddleName  "
            viewModel.firstNameCommissar = firstName
        }
        capitals.clear()
        nameGenerator(p6)
        if (capitals.size == 2) {
            viewModel.familyNameChastnik = familyName
            viewModel.firstNameChastnik = firstName
        } else if (capitals.size == 3) {
            viewModel.familyNameChastnik = familyName
            viewModel.middleChastinkName = middleName
            viewModel.firstNameChastnik = firstName
        } else if (capitals.size == 4) {
            viewModel.familyNameChastnik = familyName
            viewModel.middleChastinkName = middleName
            viewModel.middleMiddleChastnikName = "$middleMiddleName  "
            viewModel.firstNameChastnik = firstName
        }
        capitals.clear()
        viewModel.location = p8
        viewModel.datum = p9
        if (middleName.isEmpty() && middleMiddleName.isEmpty()) {
            viewModel.getPhoto("${viewModel.familyName}${viewModel.firstName}${viewModel.tsz}.jpg")
        } else if (middleMiddleName.isEmpty()) {
            if (viewModel.familyName.substring(viewModel.familyName.length - 1) == "-") {
                viewModel.getPhoto(
                    "${
                        viewModel.familyName.substring(
                            0,
                            viewModel.familyName.length - 1
                        )
                    }${viewModel.middleName}${viewModel.firstName}q${viewModel.tsz}.jpg"
                )
            } else {
                viewModel.getPhoto(
                    "${
                        viewModel.familyName.substring(
                            0,
                            viewModel.familyName.length
                        )
                    }${viewModel.middleName}${viewModel.firstName}${viewModel.tsz}.jpg"
                )
            }
        } else {
            if (viewModel.familyName.substring(viewModel.familyName.length - 1) == "-") {
                viewModel.getPhoto(
                    "${
                        viewModel.familyName.substring(
                            0,
                            viewModel.familyName.length - 1
                        )
                    }${viewModel.middleName}${viewModel.middleMiddleName.trim()}${viewModel.firstName}q${viewModel.tsz}.jpg"
                )
            } else {
                viewModel.getPhoto(
                    "${
                        viewModel.familyName.substring(
                            0,
                            viewModel.familyName.length
                        )
                    }${viewModel.middleName}${viewModel.middleMiddleName.trim()}${viewModel.firstName}${viewModel.tsz}.jpg"
                )
            }
        }
        refreshList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivityConnector = if (context is MainActivityConnector) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun onCurrentClick(position: Int) {
        mainActivityConnector.loadPanelWithValues(
            reversedList[position].perception,
            reversedList[position].type,
            reversedList[position].response,
            reversedList[position].measure,
            reversedList[position].now,
            reversedList[position].corrector,
            reversedList[position].date,
            reversedList[position].id
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshList() {
        reversedList.clear()
        for (i in observationArray.size - 1 downTo 0) {
            reversedList.add(
                ObservationData(
                    observationArray[i].perception,
                    observationArray[i].type,
                    observationArray[i].response,
                    observationArray[i].measure,
                    observationArray[i].now,
                    observationArray[i].corrector,
                    observationArray[i].date,
                    observationArray[i].id
                )
            )
        }
        binding.observationRecycler?.adapter?.notifyDataSetChanged()
    }

    fun refreshAt(position: Int){
        for(i in observationArray.size - 1 downTo 0){
            if(observationArray[i].id.toInt() == position){
                observationArray.removeAt(i)
            }
        }
    }

    private fun nameGenerator(name: String) {
        for (i in 0 until name.length) {
            if (Character.isUpperCase(name[i])) {
                capitals.add(i)
            }
        }
        if (capitals.size == 2) {
            familyName = name.substring(0, capitals[1]).trim()
            firstName = name.substring(capitals[1]).trim()
        }
        if (capitals.size == 3) {
            if (name.substring(capitals[1] - 1, capitals[1]) == "-") {
                familyName = name.substring(0, capitals[1]).trim()
                middleName = name.substring(capitals[1], capitals[2]).trim()
                firstName = name.substring(capitals[2]).trim()
            } else {
                familyName = name.substring(0, capitals[1]).trim()
                middleName = name.substring(capitals[1], capitals[2]).trim()
                firstName = name.substring(capitals[2]).trim()
            }
        }
        if (capitals.size == 4) {
            if (name.substring(capitals[1] - 1, capitals[1]) == "-") {
                familyName = name.substring(0, capitals[1]).trim()
                middleName = name.substring(capitals[1], capitals[2]).trim()
                middleMiddleName = name.substring(capitals[2], capitals[3]).trim()
                firstName = name.substring(capitals[3]).trim()
            } else {
                familyName = name.substring(0, capitals[1]).trim()
                middleName = name.substring(capitals[1], capitals[2]).trim()
                middleMiddleName = name.substring(capitals[2], capitals[3]).trim()
                firstName = name.substring(capitals[3]).trim()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        val myList = arguments?.getSerializable("EMBER") as ArrayList<Data>
        for (i in 0 until myList.size) {
            p1 = myList[i].id
            p2 = myList[i].name
            p3 = myList[i].tsz
            p4 = myList[i].fsz
            p5 = myList[i].ftsz
            p6 = myList[i].resztvevo
            p7 = myList[i].rtsz
            p8 = myList[i].location
            p9 = myList[i].date
            p10 = myList[i].status
        }
    }

}