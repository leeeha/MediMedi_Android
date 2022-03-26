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

        // 이전 화면에서 클릭한 항목에 따라 약 정보 보여주기
        mRESTApi.getHistoryDetail(args.id).enqueue(object : Callback<DetailResponse>{
            override fun onResponse(
                call: Call<DetailResponse>,
                response: Response<DetailResponse>
            ) {
                if(response.isSuccessful){
                    Log.e("Retrofit", "Success!!!")
                }else{
                    Log.e("Retrofit", response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                 Log.e("Retrofit", t.message.toString())
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