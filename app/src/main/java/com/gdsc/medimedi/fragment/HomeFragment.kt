package com.gdsc.medimedi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentHomeBinding

// 로그인 정보 받아온 상태에서 홈 화면 진입
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.btnCamera.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_searchFragment)
        }

        binding.btnAlarm.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_alarmFragment)
        }

        binding.btnHistory.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.ibSettings.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}