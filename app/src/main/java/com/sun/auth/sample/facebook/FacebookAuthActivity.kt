package com.sun.auth.sample.facebook

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivityFacebookAuthBinding

class FacebookAuthActivity : AppCompatActivity() {
    private val facebookAuthViewModel: FacebookAuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())
            .get(FacebookAuthViewModel::class.java)
    }
    private lateinit var binding: ActivityFacebookAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacebookAuthBinding.inflate(layoutInflater)
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
            facebookAuthViewModel.logout()
        }
    }

    private fun initData() {
        facebookAuthViewModel.initFacebookSignIn(this)
    }

    private fun observes() {
        facebookAuthViewModel.apply {
            signInState.observe(this@FacebookAuthActivity) {
                if (it.exception != null) {
                    displayMessage("SignIn error ${it.exception.message.orEmpty()}")
                } else {
                    displayMessage("SignIn success")
                }
                switchUi()
            }
            signOutState.observe(this@FacebookAuthActivity) {
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
        if (facebookAuthViewModel.isLoggedIn()) {
            binding.tvId.text = "Welcome: ${facebookAuthViewModel.getUser()?.user?.email}"
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }
}