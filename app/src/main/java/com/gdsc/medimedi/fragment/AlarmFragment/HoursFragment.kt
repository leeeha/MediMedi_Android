package com.gdsc.medimedi.fragment.AlarmFragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentHoursBinding
import com.google.android.gms.common.config.GservicesValue.value
import java.util.*

class HoursFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {
    private var _binding: FragmentHoursBinding? = null
    private val binding get() = _binding!!
    private val args: HoursFragmentArgs by navArgs()

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        binding.btnMake.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_make -> {
                val action = HoursFragmentDirections.actionHoursFragmentToAlarmFragment(
                    binding.numHours.value,0, "알람 화면 입니다.")
                findNavController().navigate(action)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initNumberPicker()
        numberPickerListener()
    }

    // 넘버 픽커 초기화
    private fun initNumberPicker(){
        val data: Array<String> = Array(24){
                i -> i.toString()
        }

        binding.numHours.minValue = 0
        binding.numHours.maxValue = data.size-1
        binding.numHours.displayedValues = data
        binding.numHours.wrapSelectorWheel = false
        binding.numHours.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    }

    // 넘버 픽커 리스너
    private fun numberPickerListener(){
        binding.numHours.setOnValueChangedListener { picker, oldVal, newVal ->
            Log.d("test", "oldVal : ${oldVal}, newVal : $newVal")
            Log.d("test", "picker.displayedValues ${picker.displayedValues[picker.value]}")
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