package com.gdsc.medimedi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentManualBinding
import com.gdsc.medimedi.databinding.FragmentSearchBinding

// 약 검색 화면
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 카메라 촬영 시작
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}