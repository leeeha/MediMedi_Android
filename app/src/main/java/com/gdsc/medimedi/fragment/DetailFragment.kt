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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.medimedi.adapter.ResultAdapter
import com.gdsc.medimedi.databinding.FragmentDetailBinding
import com.gdsc.medimedi.model.Result
import com.gdsc.medimedi.retrofit.DetailResponse
import com.gdsc.medimedi.retrofit.RESTApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DetailFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var tts: TextToSpeech

    private var resultAdapter = ResultAdapter()
    private val dataSet = mutableListOf<Result>()
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)
    private lateinit var mediInfo: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)
        binding.tvTitle.text = args.title // 약 이름

        initRecyclerView()

        // 화면 길게 누르면 음성 다시 재생
        binding.rvResult.setOnLongClickListener{
            speakOut(mediInfo)
            return@setOnLongClickListener true
        }
    }

    private fun initRecyclerView() {
        val recyclerView = binding.rvResult
        recyclerView.adapter = resultAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // id 값에 따라 약 정보 보여주기
        mRESTApi.getHistoryDetail(args.id).enqueue(object : Callback<DetailResponse>{
            override fun onResponse(
                call: Call<DetailResponse>,
                response: Response<DetailResponse>
            ) {
                val name = response.body()?.name
                val entp = response.body()?.entp
                val effect = response.body()?.effect
                val usingMethod = response.body()?.usingMethod
                val caution = response.body()?.caution
                val notice = response.body()?.notice
                val interact = response.body()?.interact
                val sideEffect = response.body()?.sideEffect
                val storageMethod = response.body()?.storageMethod

                // 리사이클러뷰 초기화
                with(dataSet){
                    add(Result("제품명", name))
                    add(Result("회사명", entp))
                    add(Result("효능∙효과", effect))
                    add(Result("사용법", usingMethod))
                    add(Result("주의사항", caution))
                    add(Result("경고", notice))
                    add(Result("상호작용", interact))
                    add(Result("부작용", sideEffect))
                    add(Result("보관 방법", storageMethod))
                }
                resultAdapter.dataSet = dataSet

                // 제품명, 효능효과, 사용법은 음성으로 읽어주기
                mediInfo = "$name $effect $usingMethod"
                speakOut(mediInfo)
            }

            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                 Log.e("Retrofit", "Connection Error!")
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)
        } else {
            Log.e("TTS", "TTS Initialization Failed!")
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