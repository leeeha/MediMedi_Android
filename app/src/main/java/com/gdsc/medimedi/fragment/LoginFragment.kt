package com.gdsc.medimedi.fragment

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentLoginBinding
import com.gdsc.medimedi.retrofit.LoginRequest
import com.gdsc.medimedi.retrofit.RESTApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class LoginFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val args: LoginFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private lateinit var tts: TextToSpeech

    // 로그인
    private val RC_SIGN_IN = 10
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var account: GoogleSignInAccount? = null
    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var mRESTApi: RESTApi? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // 로그인
        preferences = context?.getSharedPreferences("data", MODE_PRIVATE)
        editor = preferences!!.edit()
        mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        mGoogleSignInClient!!.silentSignIn().addOnCompleteListener(this,
            OnCompleteListener<GoogleSignInAccount?> { task -> handleSignInResult(task) })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        // 이전 화면으로 돌아가기
        binding.btnPrev.setOnClickListener {
            navController.popBackStack()
        }

        // 로그인 버튼
        binding.signInButton.setOnClickListener {
            signIn()
            Log.d("signinbutton", "signIn")
        }

        // 자동 로그인
        val gsa = GoogleSignIn.getLastSignedInAccount(requireActivity())

        if (gsa!= null){
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
            Log.d("loginId", "${gsa.email}")
            Log.d("autologin", "navigate to home")
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        Log.d("signIN", "success")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            Log.d("onActivityResult", "requestCode is same")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            Log.d("LoginFragment", "idToken = $idToken")
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult: failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account == null) {
            binding.signInButton.setVisibility(View.VISIBLE)
        } else {
            doRetrofit(account)
        }
    }

    private fun doRetrofit(account: GoogleSignInAccount?) {
        val test = LoginRequest("${account?.idToken}",
                "${account?.displayName}",
                "${account?.email}")
        GlobalScope.launch {
            try {
                val response = mRESTApi?.googleLogin(test)
                Log.d("logintogoogle", response.toString())
                //checkLoggedIn()
            } catch (throwable: Throwable) {
                Log.d("LoginFragment", throwable.message!!)
            }
        }
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

    private fun <TResult> Task<TResult>.addOnCompleteListener(
        loginFragment: LoginFragment,
        onCompleteListener: OnCompleteListener<TResult?>) {

    }
}



