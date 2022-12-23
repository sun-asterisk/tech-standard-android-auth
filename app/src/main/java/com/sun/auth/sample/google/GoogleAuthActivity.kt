package com.sun.auth.sample.google

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.databinding.ActivityGoogleAuthBinding
import com.sun.auth.social.google.ModifiedDateTimeException

class GoogleAuthActivity : AppCompatActivity() {

    private val googleAuthViewModel: GoogleAuthViewModel by lazy {
        ViewModelProvider(this, GoogleAuthViewModelFactory())
            .get(GoogleAuthViewModel::class.java)
    }
    private lateinit var binding: ActivityGoogleAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleAuthBinding.inflate(layoutInflater)
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
            googleAuthViewModel.logout()
        }
    }

    private fun initData() {
        googleAuthViewModel.initGoogleSignIn(this)
    }

    private fun observes() {
        googleAuthViewModel.apply {
            signInState.observe(this@GoogleAuthActivity) {
                if (it.exception != null) {
                    if (it.exception is ModifiedDateTimeException) {
                        displayMessage("Your datetime is changed, please correct!")
                    } else {
                        displayMessage("SignIn error ${it.exception.message ?: "Cancelled"}")
                    }
                } else {
                    displayMessage("SignIn success")
                }
                switchUi()
            }
            signOutState.observe(this@GoogleAuthActivity) {
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
            binding.tvId.text = "Welcome: ${googleAuthViewModel.getUser()?.user?.email}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }
}
