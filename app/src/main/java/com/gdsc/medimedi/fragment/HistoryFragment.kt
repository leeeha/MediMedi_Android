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
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.medimedi.adapter.HistoryAdapter
import com.gdsc.medimedi.databinding.FragmentHistoryBinding
import com.gdsc.medimedi.model.History
import com.gdsc.medimedi.retrofit.HistoryResponse
import com.gdsc.medimedi.retrofit.RESTApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var dataSet: MutableList<History>
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

        // 주의: 이 리스트를 위에서 변수 선언할 때 초기화를 해버리면
        // 프래그먼트가 생성될 때마다 리스트에 동일한 아이템이 계속 추가됨.
        dataSet = mutableListOf()

        doRetrofit()
    }

    private fun doRetrofit() {
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())
        mRESTApi.getSearchHistory(account?.idToken).enqueue(object : Callback<HistoryResponse> {
            override fun onResponse(
                call: Call<HistoryResponse>,
                response: Response<HistoryResponse>
            ) {
                if (response.isSuccessful) { // 레트로핏 성공
                    response.body()?.let {
                        if (it.success && it.data.isNotEmpty()) {
                            Log.e("Retrofit", "검색 기록 조회 성공")
                            initRecyclerView(it.data)
                        } else {
                            Log.e("Retrofit", "검색 기록 조회 실패 or 결과 없음")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                Log.e("Retrofit", t.message.toString())
            }

        })

    }

    private fun initRecyclerView(data: MutableList<History>) {
        val recyclerView = binding.rvHistory
        historyAdapter = HistoryAdapter{ pos -> onListItemClick(pos) }
        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        for(i in 0 until data.size){
            val id = data[i].id
            val name = data[i].name
            val date = data[i].date
            Log.e("Retrofit", " ${id} ${name} ${date}")
            dataSet.add(History(id, name, date))
        }
        historyAdapter.dataSet = dataSet
    }

    // id값 넘겨주면서 Detail 화면으로 이동
    private fun onListItemClick(pos: Int) {
        val itemId = dataSet[pos].id
        val itemName = dataSet[pos].name
        val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(itemId, itemName)
        navController.navigate(action)
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