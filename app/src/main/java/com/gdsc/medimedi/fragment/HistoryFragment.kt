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
import com.gdsc.medimedi.adapter.HistoryAdapter
import com.gdsc.medimedi.databinding.FragmentHistoryBinding
import com.gdsc.medimedi.model.History
import com.gdsc.medimedi.retrofit.HistoryResponse
import com.gdsc.medimedi.retrofit.RESTApi
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class HistoryFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val args: HistoryFragmentArgs by navArgs()

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    private var historyAdapter = HistoryAdapter()
    private val dataSet = mutableListOf<History>()
    private val mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        initRecyclerView()

        historyAdapter.setOnItemClickListener(object : HistoryAdapter.OnItemClickListener{
            override fun onItemClick(v: View, data: History, pos: Int) {
                // 인덱스와 약 이름 전달하기
                val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(pos, dataSet[pos].name)
                findNavController().navigate(action)
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView = binding.rvHistory
        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
            LinearLayoutManager(requireContext()).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val account: GoogleSignInAccount? = null
        mRESTApi.getSearchHistory(account?.idToken).enqueue(object: Callback<HistoryResponse> {
            override fun onResponse(
                call: Call<HistoryResponse>,
                response: Response<HistoryResponse>
            ) {
                if(response.body()?.success == true){
                    val size = response.body()!!.data.size // 검색했던 약의 개수

                    for(i in 0..size){
                        val id = response.body()!!.data.get(i).id
                        val name = response.body()!!.data.get(i).name
                        val date = response.body()!!.data.get(i).date

                        dataSet.add(i, History(id, name, date))
                    }

                    historyAdapter.dataSet = dataSet
                }
            }

            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                Log.e("Retrofit", "Connection Error!")
            }

        })
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