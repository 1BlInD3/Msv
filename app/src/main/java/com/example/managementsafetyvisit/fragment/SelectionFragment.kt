package com.example.managementsafetyvisit.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.managementsafetyvisit.MainActivity
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.adapters.PersonSelectionAdapter
import com.example.managementsafetyvisit.data.Data
import java.lang.RuntimeException

class SelectionFragment : Fragment(), PersonSelectionAdapter.OnPersonClickListener {

    interface PersonSelectionConnector {
        fun onPersonSelected(selectedData: Data)
    }

    private lateinit var personSelectionConnector: PersonSelectionConnector

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selection, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.person_recycler)

        val adapter = PersonSelectionAdapter(MainActivity.dataArray, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    override fun onPersonClick(data: Data) {
        personSelectionConnector.onPersonSelected(data)
    }

    override fun onResume() {
        super.onResume()
        MainActivity.signed = false
        MainActivity.signing = false
        MainActivity.closingTime = false
        MainActivity.adminScanning = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        personSelectionConnector = if (context is PersonSelectionConnector) {
            context
        } else {
            throw RuntimeException("$context must implement PersonSelectionConnector")
        }
    }
}