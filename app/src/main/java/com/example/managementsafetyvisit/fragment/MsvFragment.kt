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

@AndroidEntryPoint
class MsvFragment : Fragment(),MsvListener,ObservationDataAdapter.CurrentSelection {

    private val viewModel: MsvViewModel by viewModels()
    private lateinit var binding: FragmentMsvBinding
    interface MainActivityConnector{
        fun loadPerceptionPanel()
        fun loadPanelWithValues(perception: String, type: String, response: String, measure: String, urgent: Boolean, corrector: String?,date: String?)
    }
    private lateinit var mainActivityConnector: MainActivityConnector

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_msv, container, false)
        binding.viewModel = viewModel
        binding.familyName.text = "Bálind"
        binding.firstName.text = "Attila"
        binding.idCode.text = "#1557"
        viewModel.msvListener = this

        binding.observationRecycler?.adapter =  ObservationDataAdapter(observationArray,this)
        binding.observationRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding.observationRecycler?.setHasFixedSize(true)


        binding.observationRecycler?.adapter?.notifyDataSetChanged()

        binding.newResponse?.setOnClickListener {
            mainActivityConnector.loadPerceptionPanel()
        }
        binding.imageView.setOnClickListener {
            viewModel.getPhoto("BálindAttila1557.jpg")
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
        viewModel.getPhoto("BálindAttila1557.jpg")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivityConnector = if(context is MainActivityConnector){
            context
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun onCurrentClick(position: Int) {
        mainActivityConnector.loadPanelWithValues(observationArray[position].perception,
            observationArray[position].type, observationArray[position].response, observationArray[position].measure,
            observationArray[position].now, observationArray[position].corrector, observationArray[position].date)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun refreshList(){
        binding.observationRecycler?.adapter?.notifyDataSetChanged()
    }
}