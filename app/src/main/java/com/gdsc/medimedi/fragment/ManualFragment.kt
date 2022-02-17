package com.gdsc.medimedi.fragment

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.SUCCESS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentManualBinding
import java.util.*

class ManualFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!
    private lateinit var tts: TextToSpeech
    lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    // onCreateView()의 리턴값이 onViewCreated()의 매개변수로 전달됨.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        tts = TextToSpeech(this.context, this)

        // 한번 클릭하면 텍스트 모드, 더블 클릭하면 음성 모드
        binding.frameLayout.setOnClickListener(object : DoubleClickListener() {
            override fun onSingleClick() {
                // TODO: TTS 음성 나오기 전에 이미 화면 전환이 돼버리는 문제 해결하기!!
                speakOut("텍스트 모드")
                navController.navigate(R.id.action_manualFragment_to_loginFragment)
            }

            override fun onDoubleClick() {
                // TODO: TTS 음성 나오기 전에 이미 화면 전환이 돼버리는 문제 해결하기!!
                speakOut("음성 모드")
                navController.navigate(R.id.action_manualFragment_to_loginFragment)
            }
        })

        // 길게 누르면 안내음 다시 재생
        binding.frameLayout.setOnLongClickListener {
            speakOut(getString(R.string.app_manual))
            return@setOnLongClickListener true
        }
    }

    override fun onInit(status: Int) {
        // TTS 객체가 정상적으로 초기화 되면
        if (status == SUCCESS) {
            // TTS 언어를 한국어로 설정
            val result = tts.setLanguage(Locale.KOREA)

            // 지원되는 언어가 아니거나, 언어 데이터가 누락된 경우 에러 문구 출력
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("TTS", "This Language is not supported")
            } else {
                tts.setPitch(0.6F) // 음성 톤 높이 지정
                tts.setSpeechRate(1.0F) // 음성 속도 지정

                // onInit에 음성 출력할 텍스트를 넣어줌
                speakOut(getString(R.string.app_manual))
            }
        } else { // 초기화 실패
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        // 첫 번째 매개변수: 음성 출력을 할 텍스트
        // 두 번째 매개변수: 1. TextToSpeech.QUEUE_FLUSH - 진행 중인 음성 출력을 끊고 이번 TTS의 음성 출력
        //                 2. TextToSpeech.QUEUE_ADD - 진행 중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    abstract class DoubleClickListener : View.OnClickListener {
        abstract fun onDoubleClick()
        abstract fun onSingleClick()

        companion object {
            private const val DEFAULT_QUALIFICATION_SPAN: Long = 300
        }

        private var isSingleEvent = false
        private val doubleClickSpanInMillis: Long = DEFAULT_QUALIFICATION_SPAN
        private var timestampLastClick: Long
        private val handler: Handler
        private val runnable: Runnable

        init { // 생성자
            timestampLastClick = 0
            handler = Handler()
            runnable = Runnable {
                if (isSingleEvent) {
                    onSingleClick()
                }
            }
        }

        override fun onClick(v: View) {
            // 더블 클릭
            if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickSpanInMillis) {
                isSingleEvent = false
                handler.removeCallbacks(runnable)
                onDoubleClick()
                return
            }else{ // 싱글 클릭
                isSingleEvent = true
                handler.postDelayed(runnable, DEFAULT_QUALIFICATION_SPAN)
                timestampLastClick = SystemClock.elapsedRealtime()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 사용한 TTS 객체 제거
        tts.stop()
        tts.shutdown()

        _binding = null
    }
}
