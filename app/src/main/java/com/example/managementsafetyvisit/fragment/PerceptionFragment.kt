package com.example.managementsafetyvisit.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.managementsafetyvisit.MainActivity.Companion.observationArray
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.data.ObservationData
import com.example.managementsafetyvisit.databinding.FragmentPerceptionBinding
import com.example.managementsafetyvisit.viewModels.PerceptionViewModel
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"
private const val ARG_PARAM5 = "param5"
private const val ARG_PARAM6 = "param6"
private const val ARG_PARAM7 = "param7"
private const val ARG_PARAM8 = "param8"
class PerceptionFragment : Fragment() {

    interface MainActivityInteract {
        fun closeFragment()
        fun refreshList()
    }

    private lateinit var mainActivityInteract: MainActivityInteract
    private val viewModel: PerceptionViewModel by viewModels()
    private lateinit var binding: FragmentPerceptionBinding

    private var p1 = ""
    private var p2 = ""
    private var p3 = ""
    private var p4 = false
    private var p5 = ""
    private var p6 = ""
    private var p7 = ""
    private var p8 = 0
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
            p8 = it.getInt(ARG_PARAM8)
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
        }
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.msvTypes,
            android.R.layout.simple_spinner_item
        )
            .also { adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.typeSpinner.adapter = adapter
        binding.perceptionEdit.requestFocus()
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val myMonth = month + 1
            val myFormattedMonth: String = if (myMonth < 10) {
                "0$myMonth"
            } else {
                "$myMonth"
            }
            val myFormattedDay: String = if(day < 10){
                "0$day"
            }else{
                "$day"
            }
            viewModel.myDate = "$year-$myFormattedMonth-$myFormattedDay"
            Toast.makeText(requireContext(), viewModel.myDate, Toast.LENGTH_SHORT).show()
        }

        binding.urgentBox.setOnClickListener {
            viewModel.urgent = binding.urgentBox.isChecked
            // Toast.makeText(requireContext(), "${binding.urgentBox.isChecked}", Toast.LENGTH_SHORT).show()
        }
        binding.okButton.setOnClickListener {
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
                    viewModel.response =  binding.perceptionEdit.text.toString().trim()
                    viewModel.answer =  binding.answerEdit.text.toString().trim()
                    viewModel.measure = binding.measureEdit.text.toString().trim()
                    if(viewModel.myDate.isEmpty()){
                        val simpleDate = SimpleDateFormat("yyyy-MM-dd")
                        viewModel.myDate = simpleDate.format(Date(binding.calendarView.date))
                    }
                    viewModel.urgent = binding.urgentBox.isChecked
                    viewModel.typeValue = binding.typeSpinner.selectedItem.toString().substring(0,2)
                    viewModel.corrector = binding.correctorEdit.text.toString().trim()

                    observationArray.add(ObservationData(viewModel.response,viewModel.typeValue,viewModel.answer,viewModel.measure,viewModel.urgent,viewModel.corrector,viewModel.myDate,666))
                    mainActivityInteract.refreshList()
                    mainActivityInteract.closeFragment()
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.response = p1
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
            viewModel.id = p8
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

    companion object {
        @JvmStatic
        fun newInstance(
            param1: String?,
            param2: String?,
            param3: String?,
            param4: Boolean,
            param5: String?,
            param6: String?,
            param7: String?,
            param8: Int
        ) =
            PerceptionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putBoolean(ARG_PARAM4, param4)
                    putString(ARG_PARAM5, param5)
                    putString(ARG_PARAM6, param6)
                    putString(ARG_PARAM7, param7)
                    putInt(ARG_PARAM8, param8)
                }
            }
    }
}