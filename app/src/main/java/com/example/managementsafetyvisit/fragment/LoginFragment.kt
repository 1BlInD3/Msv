package com.example.managementsafetyvisit.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.managementsafetyvisit.MainActivity.Companion.closingTime
import com.example.managementsafetyvisit.MainActivity.Companion.commissar
import com.example.managementsafetyvisit.MainActivity.Companion.signed
import com.example.managementsafetyvisit.MainActivity.Companion.signing
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.databinding.FragmentLoginBinding
import com.example.managementsafetyvisit.viewModels.LoginViewModel
import java.lang.RuntimeException


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : Fragment() {

    interface LoginScan{
        fun openCamera()
    }
    private lateinit var loginScan: LoginScan
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        binding.loginButton.setOnClickListener {
            loginScan.openCamera()
        }
        Log.d("QQQQ", "onCreateView: ")
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("QQQQ", "onResume: ")
        signed = false
        signing = false
        commissar = false
        closingTime = false
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
         loginScan = if(context is LoginScan){
             context
         }else{
             throw RuntimeException(context.toString() + "must implement")
         }
    }
}