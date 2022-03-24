package com.gdsc.medimedi.fragment

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
import com.bumptech.glide.Glide
import com.gdsc.medimedi.R
import com.gdsc.medimedi.databinding.FragmentSettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.*

class SettingsFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val args: SettingsFragmentArgs by navArgs()
    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        tts = TextToSpeech(this.context, this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        setUserInfo()

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        binding.btnPrev.setOnClickListener{
            navController.popBackStack()
        }
    }

    private fun setUserInfo() {
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())

        Glide.with(this).load(account?.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(binding.ivProfile)

        binding.tvName.text = account?.givenName
        binding.tvEmail.text = account?.email
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(requireActivity()) {
                Log.e("SignOut", "Success")

                // Update your UI here
                navController.navigate(R.id.action_settingsFragment_to_manualFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        tts.stop()
        tts.shutdown()
        _binding = null
    }
}
