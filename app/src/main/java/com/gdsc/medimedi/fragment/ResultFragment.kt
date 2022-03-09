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
import com.bumptech.glide.Glide
import com.gdsc.medimedi.adapter.ResultAdapter
import com.gdsc.medimedi.databinding.FragmentResultBinding
import com.gdsc.medimedi.model.Result
import com.gdsc.medimedi.retrofit.RESTApi
import java.util.*
import com.gdsc.medimedi.retrofit.SearchRequest
import com.gdsc.medimedi.retrofit.SearchResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
    private lateinit var resultAdapter: ResultAdapter
    private val dataSet = mutableListOf<Result>()
    private lateinit var result: String

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

        // 약 상자 이미지 보여주기
        if(args.imgUri != null){
            Glide.with(requireActivity()).load(args.imgUri).into(binding.ivMedicineBox)
        }

        // 리사이클러뷰 초기화
        initRecyclerView()

        // 이전 화면으로 돌아가서 다시 촬영하기
        binding.btnCamera.setOnClickListener {
            navController.popBackStack()
        }

        // 검색 기록 조회 버튼
        binding.btnHistory.setOnClickListener{
            val action = ResultFragmentDirections.actionResultFragmentToHistoryFragment("검색 기록 조회하기")
            findNavController().navigate(action)
        }

        // 화면 가운데를 길게 누르면, 약 설명 다시 재생
        binding.btnReplay.setOnLongClickListener{
            speakOut(result)
            return@setOnLongClickListener true
        }
    }

    private fun initRecyclerView() {
        resultAdapter = ResultAdapter() // 전역변수 초기화 필수

        // 어댑터와 레이아웃 매니저
        val recyclerView = binding.rvResult
        recyclerView.adapter = resultAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 아이템 구분선 추가
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // restApi에서 데이터 받아와서 리사이클러뷰 초기화
        val account: GoogleSignInAccount? = null
        val requestBody = SearchRequest(account?.idToken, args.imgUri)
        mRESTApi.getSearchResult(requestBody).enqueue(object: Callback<SearchResponse>{
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                // 약 검색 성공
                if(response.body()?.success == true){
                    val name = response.body()!!.data.name
                    val entp = response.body()!!.data.entp
                    val effect = response.body()!!.data.effect
                    val usingMethod = response.body()!!.data.usingMethod
                    val caution = response.body()!!.data.caution
                    val notice = response.body()!!.data.notice
                    val interact = response.body()!!.data.interact
                    val sideEffect = response.body()!!.data.sideEffect
                    val storageMethod = response.body()!!.data.storageMethod

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

                    // 제품명, 효능효과, 사용법 음성으로 읽어주기
                    result = "$name $effect $usingMethod"
                    speakOut(result)
                }else{
                    // 약 검색 실패 시, 인식된 텍스트만 읽어주기
                    result = response.body()?.data?.text.toString()
                    speakOut("해당 약을 찾지 못해 인식한 글자만 읽어드립니다. $result")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
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