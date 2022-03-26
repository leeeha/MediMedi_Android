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
import com.gdsc.medimedi.adapter.DetailAdapter
import com.gdsc.medimedi.databinding.FragmentDetailBinding
import com.gdsc.medimedi.model.Detail
import com.gdsc.medimedi.model.MedicineInfo
import com.gdsc.medimedi.retrofit.DetailResponse
import com.gdsc.medimedi.retrofit.RESTApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.util.*

class DetailFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var tts: TextToSpeech

    private lateinit var detailAdapter: DetailAdapter
    private val dataSet = mutableListOf<Detail>()
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)
    private val cateList = listOf("제품명", "회사명", "효능∙효과", "사용법", "주의사항", "경고", "상호작용", "부작용", "보관법")

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
        binding.tvTitle.text = args.title

        doRetrofit()

        // todo: 화면 꾹 누르면 음성 다시 재생하기
    }

    private fun doRetrofit() {
        // id 값에 따라 다른 결과 보여주기
        mRESTApi.getHistoryDetail(args.id).enqueue(object : Callback<DetailResponse>{
            override fun onResponse(
                call: Call<DetailResponse>,
                response: Response<DetailResponse>
            ) {
                if(response.isSuccessful){
                    // body가 널이면 에러
                    response.body()?.let {
                        if(it.success){
                            Log.e("Retrofit", "기록 상세 조회 성공")
                            initRecyclerView(it.data)
                        }
                    }
                }else{
                    Log.e("Retrofit", "기록 상세 조회 실패")
                }
            }
            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                 Log.e("Retrofit", t.message.toString())
            }
        })
    }

    private fun initRecyclerView(data: MedicineInfo) {
        val recyclerView = binding.rvDetail
        detailAdapter = DetailAdapter()
        recyclerView.adapter = detailAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(),
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        Log.e("Retrofit", "최근 검색일: ${data.date}")

        dataSet.apply {
            add(Detail(cateList[0], data.name))
            add(Detail(cateList[1], data.entp))
            add(Detail(cateList[2], data.effect))
            add(Detail(cateList[3], data.usingMethod))
            add(Detail(cateList[4], data.caution))
            add(Detail(cateList[5], data.notice))
            add(Detail(cateList[6], data.interact))
            add(Detail(cateList[7], data.sideEffect))
            add(Detail(cateList[8], data.storageMethod))
            detailAdapter.dataSet = dataSet
        }

        readTTSGuide(data)
    }

    // 제품명, 효능효과, 사용법은 음성으로 읽어주기
    private fun readTTSGuide(body: MedicineInfo) {
        val ttsGuide = "제품명: ${body.name}, 효능효과: ${body.effect}, 사용법: ${body.usingMethod}"
        speakOut(ttsGuide)
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