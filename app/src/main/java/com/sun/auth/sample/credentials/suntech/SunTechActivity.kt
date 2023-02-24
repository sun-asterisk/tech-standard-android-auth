package com.sun.auth.sample.credentials.suntech

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.R
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivitySuntechAuthBinding

class SunTechActivity : AppCompatActivity() {

    private val sunTechViewModel: SunTechViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory()).get(SunTechViewModel::class.java)
    }
    private lateinit var binding: ActivitySuntechAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySuntechAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observes()
    }

    private fun setupViews() {
        switchUi()
        binding.username.afterTextChanged {
            sunTechViewModel.signInDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString(),
            )
        }

        binding.password.apply {
            afterTextChanged {
                sunTechViewModel.signInDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> sunTechViewModel.signIn(
                        binding.username.text.toString(),
                        binding.password.text.toString(),
                    )
                }
                false
            }

            binding.signIn.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                sunTechViewModel.signIn(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }
        }

        binding.signOut.setOnClickListener {
            sunTechViewModel.signOut {
                binding.mainGroup.visibility = View.GONE
                binding.signInGroup.visibility = View.VISIBLE
            }
        }

        binding.refreshToken.setOnClickListener {
            sunTechViewModel.refreshToken()
        }
    }

    private fun observes() {
        sunTechViewModel.signInFormState.observe(this@SunTechActivity) {
            val signInState = it ?: return@observe

            // disable singIn button unless both username / password is valid
            binding.signIn.isEnabled = true

            if (signInState.usernameError != null) {
                binding.username.error = getString(signInState.usernameError)
            }
            if (signInState.passwordError != null) {
                binding.password.error = getString(signInState.passwordError)
            }
        }

        sunTechViewModel.credentialAuthResult.observe(this@SunTechActivity) {
            val signInResult = it ?: return@observe

            binding.loading.visibility = View.GONE
            if (signInResult.error != null) {
                showSignInFailed()
            }
            if (signInResult.success != null) {
                updateUiWithUser(signInResult.success)
            }
        }

        sunTechViewModel.refreshTokenResult.observe(this@SunTechActivity) {
            val refreshTokenResult = it ?: return@observe
            updateUiWithUser(refreshTokenResult.success)
        }
    }

    private fun switchUi() {
        binding.tvId.text = "Welcome: ${sunTechViewModel.getToken()?.crAccessToken}"
        if (sunTechViewModel.isSignedIn()) {
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }

    private fun updateUiWithUser(model: SunToken?) {
        val welcome = getString(R.string.welcome)
        val displayName = model?.accessToken
        Toast.makeText(applicationContext, "$welcome $displayName", Toast.LENGTH_LONG).show()
        switchUi()
    }

    private fun showSignInFailed() {
        Toast.makeText(applicationContext, getString(R.string.signin_failed), Toast.LENGTH_SHORT)
            .show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // Do nothing
        }
    })
}
