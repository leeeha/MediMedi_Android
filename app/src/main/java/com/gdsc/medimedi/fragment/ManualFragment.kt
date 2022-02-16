package com.gdsc.medimedi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdsc.medimedi.R

class ManualFragment : Fragment() {

    /*
        안녕하세요. 여러분의 눈이 되어줄 메디메디 입니다.
        화면을 한번 클릭하면 텍스트 모드,
        두번 클릭하면 음성 모드로 앱을 사용할 수 있습니다.
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manual, container, false)
    }
}