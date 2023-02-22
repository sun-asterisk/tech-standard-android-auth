package com.sun.auth.sample.google.firebase

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.core.InvalidCredentialsException
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivityGoogleFirebaseAuthBinding

class GoogleFirebaseAuthActivity : AppCompatActivity() {

    private val googleAuthViewModel: GoogleFirebaseAuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())
            .get(GoogleFirebaseAuthViewModel::class.java)
    }
    private lateinit var binding: ActivityGoogleFirebaseAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleFirebaseAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        setupViews()
        observes()
    }

    private fun setupViews() {
        switchUi()
        binding.signIn.setOnClickListener {
            googleAuthViewModel.signIn()
        }
        binding.signOut.setOnClickListener {
            googleAuthViewModel.signOut()
        }
        binding.linkAccount.setOnClickListener {
            googleAuthViewModel.signIn()
        }
    }

    private fun initData() {
        googleAuthViewModel.initGoogleSignIn(this)
    }

    private fun observes() {
        googleAuthViewModel.apply {
            signInState.observe(this@GoogleFirebaseAuthActivity) {
                if (it.exception != null) {
                    if (it.exception is InvalidCredentialsException) {
                        displayMessage("Your datetime or given info is incorrect, please correct!")
                    } else {
                        displayMessage("SignIn error ${it.exception.message.orEmpty()}")
                    }
                } else {
                    displayMessage("SignIn success")
                }
                switchUi()
            }
            signOutState.observe(this@GoogleFirebaseAuthActivity) {
                if (it != null) {
                    displayMessage("SignOut error ${it.message}")
                } else {
                    displayMessage("SignOut success")
                }
                switchUi()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun switchUi() {
        if (googleAuthViewModel.isLoggedIn()) {
            binding.tvId.text = "Welcome: ${googleAuthViewModel.getUserInfo()?.email}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }
}
