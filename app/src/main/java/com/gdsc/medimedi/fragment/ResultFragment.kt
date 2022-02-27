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
import java.util.*
import com.gdsc.medimedi.model.SearchResult

// 결과 화면
class ResultFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val args: ResultFragmentArgs by navArgs()
    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    // 검색 결과는 리사이클러뷰
    private lateinit var resultAdapter: ResultAdapter
    private val dataSet = mutableListOf<SearchResult>()

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

        // 데이터 초기화
        // todo: 제품명, 효능효과, 사용법 3가지는 음성으로 읽어주기
        with(dataSet){
            add(SearchResult("제품명", "설명"))
            add(SearchResult("회사명", "설명"))
            add(SearchResult("효능∙효과", "설명"))
            add(SearchResult("사용법", "설명"))
            add(SearchResult("주의사항", "설명"))
            add(SearchResult("경고", "설명"))
            add(SearchResult("상호 작용", "설명"))
            add(SearchResult("부작용", "설명"))
            add(SearchResult("보관 방법", "설명"))
        }
        resultAdapter.dataSet = dataSet
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)
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