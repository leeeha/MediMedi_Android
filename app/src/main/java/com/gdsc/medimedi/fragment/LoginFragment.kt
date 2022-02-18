package com.gdsc.medimedi.fragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentLoginBinding
import java.util.*

class LoginFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    lateinit var navController : NavController
    private val args: LoginFragmentArgs by navArgs()
    private lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        // 매뉴얼 화면으로 돌아가기
        binding.btnPrev.setOnClickListener{
            navController.popBackStack()
        }

        // 로그인 성공하면, 홈 화면으로 이동
        binding.btnHome.setOnClickListener{
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREA)
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("TTS", "This Language is not supported")
            } else {
                // 인자값에 따라서 텍스트 모드인지 음성 모드인지 알려줌.
                speakOut(args.tts.toString())
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts.setPitch(0.6F) // 음성 톤 높이 지정
        tts.setSpeechRate(1.0F) // 음성 속도 지정
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.stop()
        tts.shutdown()
        _binding = null
    }
}