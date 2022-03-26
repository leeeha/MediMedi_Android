package com.gdsc.medimedi.fragment.AlarmFragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentAlarmBinding
import com.gdsc.medimedi.databinding.FragmentAlarmTypeBinding
import java.util.*

class AlarmTypeFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {

    private var _binding: FragmentAlarmTypeBinding? = null
    private val binding get() = _binding!!
    private val args: AlarmTypeFragmentArgs by navArgs()

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        binding.btnForHours.setOnClickListener(this)
        binding.btnMeals.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_for_hours -> {
                val action = AlarmTypeFragmentDirections.actionAlarmTypeFragmentToHoursFragment("알람이 반복될 시간을 골라주세요.")
                navController.navigate(action)
            }
            R.id.btn_meals -> {
                val action = AlarmTypeFragmentDirections.actionAlarmTypeFragmentToMealsFragment("1일 복용 횟수를 골라주세요.")
                navController.navigate(action)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)
            speakOut(args.tts.toString())
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