package com.gdsc.medimedi.fragment.alarm

import android.app.AlarmManager
import android.app.PendingIntent
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

    private lateinit var alarmManager: AlarmManager
    private lateinit var intent: Intent
    private lateinit var pendingIntent: PendingIntent

    // 뒤로가기 버튼 누르면 홈 화면으로 돌아가도록
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when AlarmFragment is at least started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        AlarmFragmentDirections.actionAlarmFragmentToHomeFragment()
                    navController.navigate(action)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

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

        binding.tvAlarm.text = "None" // 처음 프래그먼트 띄웠을 때
        binding.btnNewAlarm.setOnClickListener(this) // 여기서 처음 알람 등록을 할텐데
        binding.btnCancelAlarm.setOnClickListener(this)

        settingAlarm()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_new_alarm -> {
                val action = AlarmFragmentDirections.actionAlarmFragmentToAlarmTypeFragment("알람 등록하기")
                navController.navigate(action)
            }
            R.id.btn_cancel_alarm -> {
                binding.tvAlarm.text = "None"
                removeAlarm()
            }
        }
    }

    // 새로운 알람 등록하기
    private fun registerAlarm(id: Int, time: Int) {
        NOTIFICATION_ID = id // 아침 : 2 , 점심 : 3 , 저녁 : 4

        val repeatInterval: Long = 0 // ALARM_TIMER * 1000L
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

    private fun settingAlarm() { // 이미 알람이 설정 되어있을 때 취소해줘야 함
        if(curAlarm != "None") {
            removeAlarm()
        }

        when (args.hours) {
            0 -> binding.tvAlarm.text = "None"
            25 -> mealsAlarm()
            else -> hoursAlarm()
        }
    }

    private fun removeAlarm(){
        speakOut("등록된 알람이 취소되었습니다.")

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
    }

    // 등록 취소
    private fun cancelAlarm(id: Int) {
        NOTIFICATION_ID = id

        // 있으면 가져오고 없으면 안 만든다. (null)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            NOTIFICATION_ID,
            Intent(activity, MyReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )

        pendingIntent?.cancel() // 기존 알람 삭제
        Log.d("CANCEL", "cancel $id")
    }

    // 식사 기준으로 알람 등록
    private fun mealsAlarm() {
        when (args.meals) {
            2 -> {
                binding.tvAlarm.text = "The alarm rings twice a day."

                // 아침 7:00 저녁 6:00 울리기
                registerAlarm(2, 7) // 2번 7시
                registerAlarm(4, 18) // 4번 18시

                val toastMessage = "아침, 저녁에 알람이 울립니다."
                Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                curAlarm = "2 meals"
                speakOut(toastMessage)
            }

            3 -> {
                binding.tvAlarm.text = "The alarm rings three times a day."

                // 아침 7:00 점심 12:00 저녁 6:00 울리기
                // 아침 7:00 저녁 6:00 울리기
                registerAlarm(2, 7) //2번 7시 알람
                registerAlarm(3, 12) //3번 12시 알람
                registerAlarm(4, 18) //4번 18시 알람

                val toastMessage = "아침, 점심, 저녁에 알람이 울립니다."
                Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()
                curAlarm = "3 meals"
                speakOut(toastMessage)
            }
        }
    }

    // 시간 기준으로 알림 등록
    private fun hoursAlarm() {
        binding.tvAlarm.text = "The alarm rings every ${args.hours} hours."
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

        val toastMessage = "${args.hours}시간마다 알림이 발생합니다."
        Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT).show()

        // 현재 알람 상태
        curAlarm = "hours"
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