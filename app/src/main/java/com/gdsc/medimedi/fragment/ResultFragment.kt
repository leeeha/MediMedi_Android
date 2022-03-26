package com.gdsc.medimedi.fragment

import android.app.ProgressDialog
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
import com.gdsc.medimedi.model.MedicineInfo
import com.gdsc.medimedi.model.Result
import com.gdsc.medimedi.retrofit.RESTApi
import java.util.*
import com.gdsc.medimedi.retrofit.SearchRequest
import com.gdsc.medimedi.retrofit.SearchResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)
    private val cateList = listOf("제품명", "회사명", "효능∙효과", "사용법", "주의사항", "경고", "상호작용", "부작용", "보관법")
    private val dataSet = mutableListOf<Result>()

    // 음성으로 제공할 약 정보 (제품명, 효능효과, 사용법)
    private lateinit var ttsGuide: String
    private lateinit var resultAdapter: ResultAdapter
    private lateinit var progressDialog: ProgressDialog
    private var startTime: Long = 0
    private var endTime: Long = 0

    // 프래그먼트가 뜨자마자 다이얼로그 로딩하다가 결과 나오면 dismiss!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = System.currentTimeMillis()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait a moment.")
        progressDialog.setMessage("Loading...")
        progressDialog.show()
    }

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

        doRetrofit()

        // 이전 화면으로 돌아가서 다시 촬영하기
        binding.btnCamera.setOnClickListener {
            navController.popBackStack()
        }

        // 검색 기록 조회 버튼
        binding.btnHistory.setOnClickListener{
            val action = ResultFragmentDirections.actionResultFragmentToHistoryFragment("검색 기록 조회하기")
            navController.navigate(action)
        }

        // todo: 화면 꾹 누르면 음성 다시 재생하기
    }

    private fun doRetrofit() {
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())
        val requestBody = SearchRequest(account?.idToken, args.imgUrl)
        Log.e("ResultFragment", "${args.imgUrl}")
        mRESTApi.getSearchResult(requestBody).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    endTime = System.currentTimeMillis()
                    Log.e("Retrofit", "Success: ${endTime - startTime} ms")
                    progressDialog.dismiss()
                    progressDialog.cancel()

                    response.body()?.let{
                        if(it.success){ // 검색 성공
                            Log.e("검색 성공 후 약 이름: ", it.data.name)
                            initRecyclerView(it.data)
                        }
                        else {
                            ttsGuide = it.data.text.toString()
                            Log.e("검색 실패 후 인식한 글자: ", ttsGuide)
                            speakOut("해당 약을 찾지 못해 인식한 글자만 읽어드립니다. $ttsGuide")
                        }
                    }
                } else {
                    Log.e("Retrofit", "Error: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("Retrofit", t.message.toString())
            }
        })
    }

    private fun initRecyclerView(data: MedicineInfo) {
        Log.e("제품명", data.name)
        Log.e("회사명", data.entp)
        Log.e("효능 효과", data.effect)
        Log.e("사용법", data.usingMethod)
        Log.e("주의사항", data.caution)
        Log.e("경고", data.notice)
        Log.e("상호작용", data.interact)
        Log.e("부작용", data.sideEffect)
        Log.e("보관 방법", data.storageMethod)

        val recyclerView = binding.rvResult
        resultAdapter = ResultAdapter()
        recyclerView.adapter = resultAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(),
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        dataSet.apply {
            add(Result(cateList[0], data.name))
            add(Result(cateList[1], data.entp))
            add(Result(cateList[2], data.effect))
            add(Result(cateList[3], data.usingMethod))
            add(Result(cateList[4], data.caution))
            add(Result(cateList[5], data.notice))
            add(Result(cateList[6], data.interact))
            add(Result(cateList[7], data.sideEffect))
            add(Result(cateList[8], data.storageMethod))

            resultAdapter.dataSet = dataSet
        }

        readTTSGuide(data)
    }

    // 제품명, 효능효과, 사용법은 음성으로 읽어주기
    private fun readTTSGuide(data: MedicineInfo) {
        ttsGuide = "제품명: ${data.name}, 효능효과: ${data.effect}, 사용법: ${data.usingMethod}"
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
//        _binding = null
    }
}