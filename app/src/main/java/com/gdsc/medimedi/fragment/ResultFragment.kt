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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.medimedi.adapter.ResultAdapter
import com.gdsc.medimedi.databinding.FragmentResultBinding
import com.gdsc.medimedi.model.Result
import com.gdsc.medimedi.retrofit.RESTApi
import java.util.*
import com.gdsc.medimedi.retrofit.SearchRequest
import com.gdsc.medimedi.retrofit.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 결과 화면
class ResultFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()
    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    // 리사이클러뷰
    private var resultAdapter = ResultAdapter()
    private val cateList = listOf("제품명", "회사명", "효능∙효과", "사용법", "주의사항", "경고", "상호작용", "부작용", "보관법")
    private lateinit var descList: List<String>
    private val dataSet = mutableListOf<Result>()

    // 음성으로 제공할 약 정보 (제품명, 효능효과, 사용법)
    private lateinit var ttsGuide: String

    // 레트로핏 객체 생성
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        // todo: 레트로핏으로 데이터 가져와서 리사이클러뷰 초기화
        loadData()

        // 이전 화면으로 돌아가서 다시 촬영하기
        binding.btnCamera.setOnClickListener {
            navController.popBackStack()
        }

        // 검색 기록 조회 버튼
        binding.btnHistory.setOnClickListener{
            val action = ResultFragmentDirections.actionResultFragmentToHistoryFragment("검색 기록 조회하기")
            findNavController().navigate(action)
        }

        // 리사이클러뷰 길게 누르면 약 설명 다시 재생
        binding.rvResult.setOnLongClickListener{
            speakOut(ttsGuide)
            return@setOnLongClickListener true
        }
    }

    private fun loadData() {
        // todo: 로그인 성공 후 유저 토큰 받아오기
        //val account: GoogleSignInAccount? = null
        val requestBody = SearchRequest("subinToken", args.imgUrl)
        Log.e("ResultFragment", "image url: ${args.imgUrl}")

        // todo: enqueue로 안되면 코루틴 사용하기 (ui 작업 기본적으로 메인 스레드 되고 있나?????)
        mRESTApi.getSearchResult(requestBody).enqueue(object : Callback<SearchResponse>{
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if(response.isSuccessful){ // 레트로핏 성공
                    response.body()?.let {
                        if(it.success){ // 검색 성공
                            Log.e("검색 성공 후 약 이름: ", it.data[0])

                            // 리사이클러뷰 초기화
                            descList = it.data
                            initRecyclerView(descList)

                            // 제품명, 효능효과, 사용법은 음성으로 읽어주기
                            ttsGuide = "${descList[0]} ${descList[2]} ${descList[3]}"
                            speakOut(ttsGuide)

                        }else{ // 검색 실패
                            ttsGuide = it.data[0] // todo: 응답이 String 배열 타입이어야 함.
                            Log.e("검색 실패 후 인식한 글자: ", ttsGuide)
                            speakOut("해당 약을 찾지 못해 인식한 글자만 읽어드립니다. $ttsGuide")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("Retrofit", t.message.toString())
            }
        })
    }


    private fun initRecyclerView(descList: List<String>) {
        // 어댑터와 레이아웃 매니저
        val recyclerView = binding.rvResult
        recyclerView.adapter = resultAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 아이템 구분선 추가
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // todo: 레트로핏으로 받아온 데이터로 초기화
        for(i in 0..8){
            with(dataSet){
                add(Result(cateList[i], descList[i]))
            }
        }

        // 리사이클러뷰 데이터 업데이트
        resultAdapter.dataSet = dataSet
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