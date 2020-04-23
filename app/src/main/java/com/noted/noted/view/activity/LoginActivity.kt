package com.noted.noted.view.activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import com.noted.noted.MainActivity
import com.noted.noted.databinding.ActivityLoginBinding
import com.noted.noted.utils.Extensions
import org.koin.android.ext.android.inject

class LoginActivity : BaseActivity() {

    private lateinit var binding :ActivityLoginBinding
    private val extensions: Extensions by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginRegisterButton.setOnClickListener { startActivity(Intent(this, IntroActivity::class.java)) }
        setupSpans()

    }

    private fun setupSpans(){
        with(extensions){
            val signupSpannableString  = SpannableString("First time ? Signup")
            signupSpannableString.withClickableSpan("Signup", onClickListener = { binding.loginUsernameExpandable.toggle() })

            binding.loginSignupText.movementMethod = LinkMovementMethod.getInstance()
            binding.loginSignupText.text = signupSpannableString
        }


    }

}