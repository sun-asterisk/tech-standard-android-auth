package com.sun.auth.sample.google.standard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.SignInCredential
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivityGoogleAuthBinding
import com.sun.auth.sample.handleSocialAuthError

class GoogleAuthActivity : AppCompatActivity() {

    private val googleAuthViewModel: GoogleAuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())
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
            googleAuthViewModel.signOut()
        }
    }

    private fun initData() {
        googleAuthViewModel.initGoogleSignIn(this)
        if (!googleAuthViewModel.isSignedIn()) {
            googleAuthViewModel.showOneTapSignIn()
        }
    }

    private fun observes() {
        googleAuthViewModel.apply {
            signInState.observe(this@GoogleAuthActivity) {
                if (it.data != null) {
                    displayMessage("SignIn Success")
                } else if (it.error != null) {
                    handleSocialAuthError(it.error)
                }

                if (it.data is SignInCredential) {
                    switchUiOneTap(it.data)
                } else {
                    switchUi()
                }
            }
            signOutState.observe(this@GoogleAuthActivity) {
                if (it != null) {
                    handleSocialAuthError(it)
                } else {
                    switchUiOneTap(null)
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
        if (googleAuthViewModel.isSignedIn()) {
            binding.tvId.text = "Welcome: ${googleAuthViewModel.getUser()?.email}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }

    private fun switchUiOneTap(credential: SignInCredential?) {
        if (credential != null) {
            binding.tvId.text = "Welcome: ${credential.displayName}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }
}
