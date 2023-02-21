package com.sun.auth.sample.facebook.firebase

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivityFacebookFirebaseAuthBinding

class FacebookFirebaseAuthActivity : AppCompatActivity() {
    private val facebookAuthViewModel: FacebookFirebaseAuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())
            .get(FacebookFirebaseAuthViewModel::class.java)
    }
    private lateinit var binding: ActivityFacebookFirebaseAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacebookFirebaseAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        setupViews()
        observes()
    }

    private fun setupViews() {
        switchUi()
        binding.signIn.setOnClickListener {
            facebookAuthViewModel.signIn()
        }
        binding.signOut.setOnClickListener {
            facebookAuthViewModel.signOut()
        }
    }

    private fun initData() {
        facebookAuthViewModel.initFacebookSignIn(this)
    }

    private fun observes() {
        facebookAuthViewModel.apply {
            signInState.observe(this@FacebookFirebaseAuthActivity) {
                if (it.exception != null) {
                    displayMessage("SignIn error ${it.exception.message.orEmpty()}")
                } else {
                    displayMessage("SignIn success")
                }
                switchUi()
            }
            signOutState.observe(this@FacebookFirebaseAuthActivity) {
                if (it != null) {
                    displayMessage("SignOut error ${it.message.orEmpty()}")
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
        if (facebookAuthViewModel.isSignedIn()) {
            binding.tvId.text = "Welcome: ${facebookAuthViewModel.getLinkedAccounts()?.email}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }
}
