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
import com.gdsc.medimedi.databinding.FragmentResultBinding
import com.gdsc.medimedi.model.Result
import com.gdsc.medimedi.retrofit.RESTApi
import java.util.*
import com.gdsc.medimedi.retrofit.SearchRequest
import com.gdsc.medimedi.retrofit.SearchResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.*
import retrofit2.HttpException

// 결과 화면
class ResultFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val args: ResultFragmentArgs by navArgs()
    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    // 리사이클러뷰
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)
    private var resultAdapter = ResultAdapter()
    private val cateList = listOf("제품명", "회사명", "효능∙효과", "사용법", "주의사항", "경고", "상호작용", "부작용", "보관법")
    //private lateinit var descList: List<String>
    private val dataSet = mutableListOf<Result>()

    // 음성으로 제공할 약 정보 (제품명, 효능효과, 사용법)
    private lateinit var ttsGuide: String

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

        val recyclerView = binding.rvResult
        recyclerView.adapter = resultAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        doRetrofitWithCoroutine() // todo: 레트로핏 데이터로 초기화

        // 이전 화면으로 돌아가서 다시 촬영하기
        binding.btnCamera.setOnClickListener {
            navController.popBackStack()
        }

        // 검색 기록 조회 버튼
        binding.btnHistory.setOnClickListener{
            val action = ResultFragmentDirections.actionResultFragmentToHistoryFragment("검색 기록 조회하기")
            navController.navigate(action)
        }

        // 리사이클러뷰 길게 누르면 약 설명 다시 재생
        binding.rvResult.setOnLongClickListener{
            speakOut(ttsGuide)
            return@setOnLongClickListener true
        }
    }

    private fun doRetrofitWithCoroutine() {
        CoroutineScope(Dispatchers.IO).launch {
            val account = GoogleSignIn.getLastSignedInAccount(requireActivity())
            val requestBody = SearchRequest(account?.idToken, args.imgUrl)
            Log.e("ResultFragment", "${args.imgUrl}")

            // todo: 서버에서 검색 결과 받아오기
            val response = mRESTApi.getSearchResult(requestBody)
            withContext(Dispatchers.Main) {
                try {
                    if (response.isSuccessful) {
                        // Do something with response e.g show to the UI. (리사이클러뷰에 결과 보여주기)
                        Log.e("Retrofit", "Success")

                        val body = response.body() ?: throw IllegalArgumentException("body is null")
                        if(body.success){
                            Log.e("검색 성공 후 약 이름: ", body.data.name)
                        }
                        else {
                            Log.e("검색 실패 후 인식한 글자: ", body.data.text)
                        }

                        //initRecyclerView(body)
                    } else {
                        Log.e("Retrofit", "Error: ${response.errorBody()}")
                    }
                } catch (e: HttpException) {
                    Log.e("Retrofit", "Exception: ${e.message}")

                } catch (e: Throwable) {
                    Log.e("Retrofit", "Throwable: ${e.message}")
                }
            }
        }
    }

    private fun initRecyclerView(body: SearchResponse) {
        val data = body.data
        if(body.success){
            Log.e("검색 성공 후 약 이름: ", data.name)
        } else {
            Log.e("검색 실패 후 인식한 글자: ", data.text)
        }

//        body?.let {
//            if (it.success) { // 검색 성공
//                Log.e("검색 성공 후 약 이름: ", it.data.name)
//
//                // 리사이클러뷰 데이터 업데이트
//                with(dataSet) {
//                    add(Result(cateList[0], it.data.name))
//                    add(Result(cateList[1], it.data.entp))
//                    add(Result(cateList[2], it.data.effect))
//                    add(Result(cateList[3], it.data.usingMethod))
//                    add(Result(cateList[4], it.data.caution))
//                    add(Result(cateList[5], it.data.notice))
//                    add(Result(cateList[6], it.data.interact))
//                    add(Result(cateList[7], it.data.sideEffect))
//                    add(Result(cateList[8], it.data.storageMethod))
//                }
//                resultAdapter.dataSet = dataSet
//
//                // 제품명, 효능효과, 사용법은 음성으로 읽어주기
//                ttsGuide = "${it.data.name} ${it.data.effect} ${it.data.usingMethod}"
//                speakOut(ttsGuide)
//
//            } else { // 검색 실패
//                ttsGuide = it.data.text
//                Log.e("검색 실패 후 인식한 글자: ", ttsGuide)
//                speakOut("해당 약을 찾지 못해 인식한 글자만 읽어드립니다. $ttsGuide")
//            }
//        }
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