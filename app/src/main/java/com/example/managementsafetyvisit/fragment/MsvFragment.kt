package com.example.managementsafetyvisit.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.adapters.ObservationDataAdapter
import com.example.managementsafetyvisit.interfaces.MsvListener
import com.example.managementsafetyvisit.databinding.FragmentMsvBinding
import com.example.managementsafetyvisit.viewModels.MsvViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
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

    interface MainActivityConnector {
        fun loadPerceptionPanel()
        fun loadPanelWithValues(
            perception: String,
            type: String,
            response: String,
            measure: String,
            urgent: Boolean,
            corrector: String?,
            date: String?
        )

        fun getCameraToScan()
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

        binding.observationRecycler?.adapter = ObservationDataAdapter(observationArray, this)
        binding.observationRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding.observationRecycler?.setHasFixedSize(true)


        binding.observationRecycler?.adapter?.notifyDataSetChanged()

        binding.newResponse?.setOnClickListener {
            mainActivityConnector.loadPerceptionPanel()
        }
        binding.imageView.setOnClickListener {
            viewModel.getPhoto("${viewModel.familyName}${viewModel.firstName}${viewModel.tsz}.jpg")
            Toast.makeText(requireContext(), "Frissítés", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun sendImageBitmap(image: Bitmap) {
        CoroutineScope(Main).launch {
            binding.imageView.setImageBitmap(image)
        }
    }

    override fun onResume() {
        super.onResume()
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
        } else if (middleMiddleName.isEmpty()){
            if(viewModel.familyName.substring(viewModel.familyName.length-1)=="-"){
                viewModel.getPhoto("${viewModel.familyName.substring(0,viewModel.familyName.length-1)}${viewModel.middleName}${viewModel.firstName}q${viewModel.tsz}.jpg")
            }else{
                viewModel.getPhoto("${viewModel.familyName.substring(0,viewModel.familyName.length)}${viewModel.middleName}${viewModel.firstName}${viewModel.tsz}.jpg")
            }
        } else{
            if(viewModel.familyName.substring(viewModel.familyName.length-1)=="-"){
                viewModel.getPhoto("${viewModel.familyName.substring(0,viewModel.familyName.length-1)}${viewModel.middleName}${viewModel.middleMiddleName.trim()}${viewModel.firstName}q${viewModel.tsz}.jpg")
            }else{
                viewModel.getPhoto("${viewModel.familyName.substring(0,viewModel.familyName.length)}${viewModel.middleName}${viewModel.middleMiddleName.trim()}${viewModel.firstName}${viewModel.tsz}.jpg")
            }
        }
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
            observationArray[position].perception,
            observationArray[position].type,
            observationArray[position].response,
            observationArray[position].measure,
            observationArray[position].now,
            observationArray[position].corrector,
            observationArray[position].date
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshList() {
        binding.observationRecycler?.adapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            param1: Int,
            param2: String,
            param3: Int,
            param4: String,
            param5: Int,
            param6: String,
            param7: Int,
            param8: String,
            param9: String,
            param10: Int
        ) =
            MsvFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putInt(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                    putInt(ARG_PARAM5, param5)
                    putString(ARG_PARAM6, param6)
                    putInt(ARG_PARAM7, param7)
                    putString(ARG_PARAM8, param8)
                    putString(ARG_PARAM9, param9)
                    putInt(ARG_PARAM10, param10)
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

}