package com.gdsc.medimedi.fragment.AlarmFragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.broadcastReceiver.Data.Companion.NOTIFICATION_ID
import com.gdsc.medimedi.broadcastReceiver.Data.Companion.curAlarm
import com.gdsc.medimedi.broadcastReceiver.MyReceiver
import com.gdsc.medimedi.databinding.FragmentAlarmBinding
import java.util.*

class AlarmFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!
    private val args: AlarmFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private lateinit var tts: TextToSpeech

    private lateinit var alarmmaintext: String
    private lateinit var alarmManager: AlarmManager
    private lateinit var intent: Intent
    private lateinit var pendingIntent: PendingIntent

    private lateinit var callback: OnBackPressedCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager

        intent = Intent(activity, MyReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            activity, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_MUTABLE
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        binding.btnNewAlarm.setOnClickListener(this)
        binding.btnCancelAlarm.setOnClickListener(this)

        settingAlarm()
        binding.alarmtext.text = alarmmaintext


    }

    //뒤로가기 누르면 홈 버튼으로!
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val action =
                    AlarmFragmentDirections.actionAlarmFragmentToHomeFragment()
                navController.navigate(action)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_new_alarm -> {
                val action =
                    AlarmFragmentDirections.actionAlarmFragmentToAlarmTypeFragment("새로운 알람")
                navController.navigate(action)
            }
            R.id.btn_cancel_alarm -> {
                // 알람 취소
                removeAlarm()
                alarmmaintext = "None"
                binding.alarmtext.text = alarmmaintext
                Log.d("AlarmFragment", "refresh!")
            }
        }
    }

    private fun settingAlarm() { //이미 알람이 설정 되어있을 때 취소해줘야 함
        if(curAlarm != "None") {
            removeAlarm()
            Log.d("settingAlarm", "After removeAlarm, curAlarm is ${curAlarm}")
        }
        when (args.hours) {
            0 -> {
                alarmmaintext = "None"
            }
            25 -> {
                mealsAlarm()
            }
            else -> {
                hoursAlarm()
            }
        }
        Log.d("settingAlarm", "curAlarm is ${curAlarm}")
    }

    private fun removeAlarm(){
        when (curAlarm) {
            "2 meals" -> {
                cancelAlarm(2)
                cancelAlarm(3)
            }
            "3 meals" -> {
                cancelAlarm(2)
                cancelAlarm(3)
                cancelAlarm(4)

            }
            "hours" -> { // 1~24?
                cancelAlarm(1)
            }
        }
        curAlarm = "None"
        Log.d("RemoveAlarm", "cancel!")
    }

    private fun cancelAlarm(id: Int) {
        // 기존에 있던 알람을 삭제한다.
        NOTIFICATION_ID = id

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            NOTIFICATION_ID,
            Intent(activity, MyReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        ) // 있으면 가져오고 없으면 안만든다. (null)

        pendingIntent?.cancel() // 기존 알람 삭제
        Log.d("CANCEL", "cancel $id")
    }

    private fun hoursAlarm() {
        alarmmaintext = "The alarm rings every ${args.hours} hours."
        // 알람 시간마다 울리게 하기

        NOTIFICATION_ID = 1

        intent = Intent(activity, MyReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            activity, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_MUTABLE
        )
        val repeatInterval: Long = args.hours * 3600 * 1000L
        val triggerTime = (SystemClock.elapsedRealtime()
                + repeatInterval)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime, repeatInterval,
            pendingIntent
        )

        val toastMessage = "${repeatInterval / (3600 * 1000)}시간마다 알림이 발생합니다."
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()

        // 현재 알람 상태
        curAlarm = "hours"
    }

    private fun mealsAlarm() {
        when (args.meals) {
            2 -> {
                alarmmaintext = "The alarm rings twice a day."
                // 아침 7:00 저녁 6:00 울리기
                updateAlarm(2, 7) // 2번 7시
                updateAlarm(4, 18) // 4번 18시

                val toastMessage = "아침, 저녁에 알람이 울립니다."

                Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                curAlarm = "2 meals"

            }

            3 -> {
                alarmmaintext = "The alarm rings three times a day."
                // 아침 7:00 점심 12:00 저녁 6:00 울리기
                // 아침 7:00 저녁 6:00 울리기
                updateAlarm(2, 7) //2번 7시 알람
                updateAlarm(3, 12) //3번 12시 알람
                updateAlarm(4, 18) //4번 18시 알람

                val toastMessage = "아침, 점심, 저녁에 알람이 울립니다."
                Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                curAlarm = "3 meals"
            }

        }
    }

    // 알람 만들기
    private fun updateAlarm(id: Int, time: Int) {
        NOTIFICATION_ID = id // 아침 : 2 , 점심 : 3 , 저녁 : 4
        val repeatInterval: Long = 0 //ALARM_TIMER * 1000L
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
            set(Calendar.MINUTE, 0)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            repeatInterval,
            pendingIntent
        )
    }

    private fun refreshFragment() {
        val ft = requireFragmentManager().beginTransaction()
        ft.detach(this).attach(this).commit()
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