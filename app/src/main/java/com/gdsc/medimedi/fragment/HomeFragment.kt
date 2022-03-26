package com.gdsc.medimedi.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentHomeBinding
import java.util.*

// 로그인 정보 받아온 상태에서 홈 화면 진입
class HomeFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val args: HomeFragmentArgs by navArgs()

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when HomeFragment is at least started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    // 다이얼로그 띄우기
                    val alertDialog: AlertDialog = AlertDialog.Builder(activity).create()
                    alertDialog.setTitle(R.string.app_name)
                    alertDialog.setMessage("Are you sure you want to exit?")
                    speakOut("앱을 종료하시겠습니까?")

                    // 앱 종료
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes"){
                            dialog, which -> activity?.finish()
                    }

                    // 다이얼로그 닫기
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No"){
                            dialog, which -> dialog.dismiss()
                    }

                    alertDialog.show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

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
        tts = TextToSpeech(this.context, this)

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
                val action = HomeFragmentDirections.actionHomeFragmentToAlarmFragment(0,0,"약 알림받기")
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
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)

            // Case1: 매뉴얼 화면에서 모드 선택 -> 첫 로그인 (tts) -> 홈 (null)
            // Case2: 매뉴얼 화면에서 모드 선택 -> 자동 로그인 -> 홈 (tts)
            if(args.tts != null){
                speakOut(args.tts.toString())
            }

        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.stop()
        tts.shutdown()
        _binding = null
    }
}