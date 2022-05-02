package com.gdsc.medimedi.fragment

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentLoginBinding
import com.gdsc.medimedi.retrofit.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class LoginFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val args: LoginFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var tts: TextToSpeech

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request Code (요청 코드)
    private var mRESTApi = RESTApi.retrofit.create(RESTApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        // Configure sign-in to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signInButton.setOnClickListener {
            signIn()
        }

        binding.btnPrev.setOnClickListener {
            navController.popBackStack()
        }
    }

    // When the button is clicked, the app starts the sign-in intent,
    // which prompts the user to sign in with a Google account.
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // After the successful auth, you get back the results with the onActivityResult method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // The Task (returned from this call) is always completed, no need to attach a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully
            val googleId = account?.id ?: ""
            Log.w("Google ID", googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.w("Google First Name", googleFirstName)

            val googleLastName = account?.familyName ?: ""
            Log.w("Google Last Name", googleLastName)

            val googleEmail = account?.email ?: ""
            Log.w("Google Email", googleEmail)

            val googleProfilePicURL = account?.photoUrl.toString()
            Log.w("Google Profile Pic URL", googleProfilePicURL)

            val googleIdToken = account?.idToken ?: ""
            Log.w("Google ID Token", googleIdToken)

            // Signed in successfully, show authenticated UI.
            updateUI(account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e("failed code=", e.statusCode.toString())
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account == null) { // 로그인 실패 (로그인 버튼 표시)
            Log.e("SignIn", "Fail")
            binding.signInButton.visibility = View.VISIBLE

        } else { // 로그인 성공 (서버에 유저 정보 보낸 뒤 홈화면으로 넘어가기)
            Log.e("SignIn", "Success")

            // 서버에 유저 정보가 이미 등록되어 있는지 확인!
            checkRetrofit(account)
        }
    }

    private fun checkRetrofit(account: GoogleSignInAccount) {
        mRESTApi.checkUserInfo(account.idToken).enqueue(object : Callback<CheckResponse>{
            override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
                if (response.isSuccessful){ // 레트로핏 성공
                    if (response.body()?.success == true){
                        if (response.body()?.data?.is_joined == false){
                            Log.e("Retrofit", "서버에 유저 정보 없음.")
                            doRetrofit(account)
                        } else {
                            // 서버에 이미 등록된 정보가 있을 경우, 유저 정보 다시 전송 X
                            Log.e("Retrofit", "서버에 유저 정보 있음.")
                        }
                    } else {
                        Log.e("Retrofit", "Fail")
                    }
                }
            }

            override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                Log.e("Retrofit", t.message.toString())
            }
        })

    }

    private fun doRetrofit(account: GoogleSignInAccount) {
        val requestBody = LoginRequest(account.idToken, account.givenName, account.email)
        mRESTApi.sendUserInfo(requestBody).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                // 레트로핏 성공
                if(response.isSuccessful){
                    // 서버에 유저 정보 보내기 성공 (홈화면으로 넘어가기)
                    if(response.body()?.success == true){
                        Log.e("Retrofit", "Success")
                        navController.navigate(R.id.action_loginFragment_to_homeFragment)

                    }else{
                        Log.e("Retrofit", "Fail")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Retrofit", t.message.toString())
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
}
