package com.gdsc.medimedi.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.databinding.FragmentAlarmDemoBinding
import java.util.*

class AlarmDemoFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentAlarmDemoBinding? = null
    private val binding get() = _binding!!
    private val args: AlarmDemoFragmentArgs by navArgs()
    private lateinit var tts: TextToSpeech
    private val enMealList = arrayOf("breakfast ", "lunch ", "dinner ")
    private val checkList = booleanArrayOf(false, false, false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tts = TextToSpeech(this.context, this)

        binding.btnNewAlarm.setOnClickListener {
            addAlarm()
        }
    }

    private fun addAlarm(){
        checkList.fill(false) // 항목 초기화

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Please choose when to take the medicine.")

        // 마지막 인자는 OnMultiChoiceClickListener이므로 밖으로 빼낸다.
        builder.setMultiChoiceItems(enMealList, checkList) { dialog, which, isChecked ->
            checkList[which] = isChecked
        }

        builder.setPositiveButton("Save") { _, _ ->
//            val toastMessage = getCheckedItemText()
//            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()

            // 하나라도 체크된 게 있으면 항목 업데이트
            if(checkList.contains(true)){
                binding.tvAlarmGuide.visibility = View.GONE

                binding.tvAlarmItem.visibility = View.VISIBLE
                binding.tvAlarmItem.text = getCheckedItemText()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun getCheckedItemText(): String {
        var checkedItemText = ""
        for(i in 0 until checkList.size){
            val checked = checkList[i]
            if(checked){ // item is checked
                checkedItemText += enMealList[i]
            }
        }

        return "The alarm rings every ${checkedItemText}"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)
            speakOut(args.tts.toString()) // 약 알림 받기
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
