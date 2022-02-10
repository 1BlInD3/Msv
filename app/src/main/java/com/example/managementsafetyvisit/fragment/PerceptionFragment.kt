package com.example.managementsafetyvisit.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.managementsafetyvisit.MainActivity.Companion.managerArray
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.adapters.ManagerAdapter
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.databinding.FragmentPerceptionBinding
import com.example.managementsafetyvisit.viewModels.PerceptionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "param7"
private const val ARG_PARAM8 = "param8"

@AndroidEntryPoint
class PerceptionFragment : Fragment() {

    interface MainActivityInteract {
        fun closeFragment()
        fun refreshList()
        fun saveNewPerception(
            perception: String?,
            answer: String?,
            measure: String?,
            type: String?,
            urgent: Boolean,
            corrector: String?,
            date: String?,
            id: Int,
            statusz: Int
        )
        fun updateExistingPerception(
            perception: String?,
            answer: String?,
            measure: String?,
            type: String?,
            urgent: Boolean,
            corrector: String?,
            date: String?,
            id: Int,
            statusz: Int
        )
        fun deleteById(id: Int)
    }

    private lateinit var mainActivityInteract: MainActivityInteract
    private val viewModel: PerceptionViewModel by viewModels()
    private lateinit var binding: FragmentPerceptionBinding
    private var id = ""
    private var p1 = ""
    private var p2 = ""
    private var p3 = ""
    private var p4 = false
    private var p5 = ""
    private var p6 = ""
    private var p7 = ""
    private var p8 = ""
    var update = false
    private lateinit var managerAdapter: ManagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            p1 = it.getString(ARG_PARAM1).toString()
            p2 = it.getString(ARG_PARAM2).toString()
            p3 = it.getString(ARG_PARAM3).toString()
            p4 = it.getBoolean(ARG_PARAM4)
            p5 = it.getString(ARG_PARAM5).toString()
            p6 = it.getString(ARG_PARAM6).toString()
            p7 = it.getString(ARG_PARAM7).toString()
            p8 = it.getString(ARG_PARAM8).toString()
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_perception, container, false)
        binding.viewModel = viewModel

        binding.cancelButton.setOnClickListener {
            mainActivityInteract.closeFragment()
           // Toast.makeText(requireContext(), viewModel.msvId, Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.msvTypes,
            android.R.layout.simple_spinner_item
        )
            .also { adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        managerAdapter = ManagerAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,managerArray)
        binding.managerSpinner?.adapter = managerAdapter
        binding.typeSpinner.adapter = adapter
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val myMonth = month + 1
            val myFormattedMonth: String = if (myMonth < 10) {
                "0$myMonth"
            } else {
                "$myMonth"
            }
            val myFormattedDay: String = if (day < 10) {
                "0$day"
            } else {
                "$day"
            }
            viewModel.myDate = "$year-$myFormattedMonth-$myFormattedDay"
            //Toast.makeText(requireContext(), viewModel.myDate, Toast.LENGTH_SHORT).show()
        }

        binding.urgentBox.setOnClickListener {
            viewModel.urgent = binding.urgentBox.isChecked
            // Toast.makeText(requireContext(), "${binding.urgentBox.isChecked}", Toast.LENGTH_SHORT).show()
        }

        binding.deleteButton?.setOnClickListener {
            if(update){
                mainActivityInteract.deleteById(viewModel.msvId.toInt())
            }else{
                Toast.makeText(
                    requireContext(),
                    "Csak mentett észrevételt lehet törölni",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.okButton.setOnClickListener {
            if(!update){
                viewModel.typeValue = binding.typeSpinner.selectedItem.toString()
                when {
                    binding.perceptionEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Az észrevétel mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.answerEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "A válasz mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.measureEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Az intézkedések mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        viewModel.response = binding.perceptionEdit.text.toString().trim()
                        viewModel.answer = binding.answerEdit.text.toString().trim()
                        viewModel.measure = binding.measureEdit.text.toString().trim()
                        if (viewModel.myDate.isEmpty()) {
                            val simpleDate = SimpleDateFormat("yyyy-MM-dd")
                            viewModel.myDate = simpleDate.format(Date(binding.calendarView.date))
                        }
                        viewModel.urgent = binding.urgentBox.isChecked
                        viewModel.typeValue =
                            binding.typeSpinner.selectedItem.toString().substring(0, 2)
                        viewModel.corrector = binding.correctorEdit.text.toString().trim()
                        if(viewModel.urgent && viewModel.measure.isNotEmpty()){
                            val omg: String? = arguments?.getString("MYSTRING")
                            viewModel.corrector = omg!!
                            mainActivityInteract.saveNewPerception(
                                viewModel.response,
                                viewModel.answer,
                                viewModel.measure,
                                viewModel.typeValue,
                                viewModel.urgent,
                                viewModel.corrector,
                                viewModel.myDate,
                                viewModel.msvId.toInt(),
                                3
                            )
                        }else{
                            mainActivityInteract.saveNewPerception(
                                viewModel.response,
                                viewModel.answer,
                                viewModel.measure,
                                viewModel.typeValue,
                                viewModel.urgent,
                                viewModel.corrector,
                                viewModel.myDate,
                                viewModel.msvId.toInt(),
                                1
                            )
                        }
                        viewModel.response = ""
                        viewModel.answer = ""
                        viewModel.measure = ""
                        viewModel.typeValue = ""
                        viewModel.urgent = false
                        viewModel.corrector = ""
                        viewModel.myDate = ""
                        viewModel.msvId = ""
                        mainActivityInteract.closeFragment()
                    }
                }
            }else{
                update = false
                viewModel.typeValue = binding.typeSpinner.selectedItem.toString()
                when {
                    binding.perceptionEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Az észrevétel mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.answerEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "A válasz mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.measureEdit.text.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Az intézkedések mező nem lehet üres!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        viewModel.response = binding.perceptionEdit.text.toString().trim()
                        viewModel.answer = binding.answerEdit.text.toString().trim()
                        viewModel.measure = binding.measureEdit.text.toString().trim()
                        if (viewModel.myDate.isEmpty()) {
                            val simpleDate = SimpleDateFormat("yyyy-MM-dd")
                            viewModel.myDate = simpleDate.format(Date(binding.calendarView.date))
                        }
                        viewModel.urgent = binding.urgentBox.isChecked
                        viewModel.typeValue =
                            binding.typeSpinner.selectedItem.toString().substring(0, 2)
                        viewModel.corrector = binding.correctorEdit.text.toString().trim()
                        if(viewModel.urgent && viewModel.measure.isNotEmpty()){
                            mainActivityInteract.updateExistingPerception(
                                viewModel.response,
                                viewModel.answer,
                                viewModel.measure,
                                viewModel.typeValue,
                                viewModel.urgent,
                                viewModel.corrector,
                                viewModel.myDate,
                                viewModel.msvId.toInt(),
                                3
                            )
                        }else{
                            mainActivityInteract.updateExistingPerception(
                                viewModel.response,
                                viewModel.answer,
                                viewModel.measure,
                                viewModel.typeValue,
                                viewModel.urgent,
                                viewModel.corrector,
                                viewModel.myDate,
                                viewModel.msvId.toInt(),
                                1
                            )
                        }
                        viewModel.response = ""
                        viewModel.answer = ""
                        viewModel.measure = ""
                        viewModel.typeValue = ""
                        viewModel.urgent = false
                        viewModel.corrector = ""
                        viewModel.myDate = ""
                        viewModel.msvId = ""
                        mainActivityInteract.closeFragment()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.perceptionEdit.requestFocus()
            loadData()
            viewModel.msvId = id
            update = false
        } catch (e: Exception) {
            update = true
            loadModify()
            viewModel.msvId = id
            //Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
            viewModel.response = p1
            //binding.perceptionEdit.requestFocus()
            viewModel.answer = p2
            viewModel.measure = p3
            viewModel.urgent = p4
            when (p5) {
                "PP" -> viewModel.typeList = 0
                "UA" -> viewModel.typeList = 1
                "UC" -> viewModel.typeList = 2
            }
            viewModel.corrector = p6
            if (p7 != "") {
                val date = p7
                val year = date.substring(0, 4).toInt()
                val month: Int = if (date.substring(5, 6) == ("0")) {
                    date.substring(6, 7).toInt()
                } else {
                    date.substring(5, 7).toInt()
                }
                val day: Int = if (date.substring(8, 9) == "0") {
                    date.substring(9).toInt()
                } else {
                    date.substring(8).toInt()
                }
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val milli = calendar.timeInMillis
                binding.calendarView.setDate(milli, true, true)
                viewModel.msvId = p8
               // Toast.makeText(requireContext(), viewModel.msvId, Toast.LENGTH_SHORT).show()
            }
            try {
                binding.perceptionEdit.isFocusableInTouchMode = true
                binding.perceptionEdit.setSelection(binding.perceptionEdit.text.length)

            }catch (e: Exception){
                Log.d("FOKUSZ", "onResume: $e")
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivityInteract = if (context is MainActivityInteract) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    private fun loadData() {
        val myList: ArrayList<ObservationData> =
            arguments?.getSerializable("EMPTYARRAY") as ArrayList<ObservationData>
        id = myList[0].id
    }

    private fun loadModify() {
        val myList: ArrayList<ObservationData> =
            arguments?.getSerializable("LOADING") as ArrayList<ObservationData>
        p1 = myList[0].perception.toString()
        p2 = myList[0].response.toString()
        p3 = myList[0].measure.toString()
        p4 = myList[0].now
        p5 = myList[0].type.toString()
        p6 = myList[0].corrector.toString()
        p7 = myList[0].date.toString()
        p8 = myList[0].id
    }
}