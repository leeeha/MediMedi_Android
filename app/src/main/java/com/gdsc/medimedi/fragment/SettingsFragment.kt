package com.gdsc.medimedi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentHomeBinding
import com.gdsc.medimedi.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 유저 정보 설정, 음성 모드 ON/OFF
        //binding.swVoice.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}