package com.gdsc.medimedi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentHomeBinding

// 로그인 정보 받아온 상태에서 홈 화면 진입
class HomeFragment : Fragment(), View.OnClickListener {
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

        binding.btnCamera.setOnClickListener(this)
        binding.btnAlarm.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_camera -> {
                val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment("약 검색하기")
                findNavController().navigate(action)
            }
            R.id.btn_alarm -> {
                val action = HomeFragmentDirections.actionHomeFragmentToAlarmFragment("약 알림받기")
                findNavController().navigate(action)
            }
            R.id.btn_history -> {
                val action = HomeFragmentDirections.actionHomeFragmentToHistoryFragment("검색 기록 조회하기")
                findNavController().navigate(action)
            }
            R.id.btn_settings -> {
                val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment("설정 화면")
                findNavController().navigate(action)
            }

            // 홈 화면에서 뒤로가기 누르면, 앱을 종료하시겠습니까? 다이얼로그 띄우기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}