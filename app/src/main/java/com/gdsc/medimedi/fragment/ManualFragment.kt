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
import androidx.navigation.fragment.findNavController
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentManualBinding
import java.util.*

class ManualFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

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
                val action = ManualFragmentDirections.actionManualFragmentToLoginFragment("텍스트 모드")
                findNavController().navigate(action)
            }

            override fun onDoubleClick() {
                val action = ManualFragmentDirections.actionManualFragmentToLoginFragment("음성 모드")
                findNavController().navigate(action)
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
            tts.language = Locale.KOREA // 언어 설정
            tts.setPitch(0.6F) // 음성 톤 높이 지정
            tts.setSpeechRate(1.0F) // 음성 속도 지정
            speakOut(getString(R.string.app_manual))
        } else { // 초기화 실패
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
        tts.stop()
        tts.shutdown()
        _binding = null
    }
}
