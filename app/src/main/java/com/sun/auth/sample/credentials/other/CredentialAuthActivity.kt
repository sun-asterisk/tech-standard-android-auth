package com.sun.auth.sample.credentials.other

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
import com.sun.auth.sample.databinding.ActivityCredentialAuthBinding

class CredentialAuthActivity : AppCompatActivity() {

    private val credentialAuthViewModel: CredentialAuthViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory()).get(CredentialAuthViewModel::class.java)
    }
    private lateinit var binding: ActivityCredentialAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCredentialAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observes()
    }

    private fun setupViews() {
        switchUi()
        binding.username.afterTextChanged {
            credentialAuthViewModel.signInDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString(),
            )
        }

        binding.password.apply {
            afterTextChanged {
                credentialAuthViewModel.signInDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> credentialAuthViewModel.signIn(
                        binding.username.text.toString(),
                        binding.password.text.toString(),
                    )
                }
                false
            }

            binding.signIn.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                credentialAuthViewModel.signIn(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }
        }

        binding.signOut.setOnClickListener {
            credentialAuthViewModel.logout {
                binding.mainGroup.visibility = View.GONE
                binding.signInGroup.visibility = View.VISIBLE
            }
        }

        binding.refreshToken.setOnClickListener {
            credentialAuthViewModel.refreshToken()
        }
    }

    private fun observes() {
        credentialAuthViewModel.signInFormState.observe(this@CredentialAuthActivity) {
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

        credentialAuthViewModel.credentialAuthResult.observe(this@CredentialAuthActivity) {
            val signInResult = it ?: return@observe

            binding.loading.visibility = View.GONE
            if (signInResult.error != null) {
                showSignInFailed()
            }
            if (signInResult.success != null) {
                updateUiWithUser(signInResult.success)
            }
        }

        credentialAuthViewModel.refreshTokenResult.observe(this@CredentialAuthActivity) {
            val refreshTokenResult = it ?: return@observe
            updateUiWithUser(refreshTokenResult.success)
        }
    }

    private fun switchUi() {
        binding.tvId.text = "Welcome: ${credentialAuthViewModel.getToken()?.crAccessToken}"
        if (credentialAuthViewModel.isLoggedIn()) {
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }

    private fun updateUiWithUser(model: Token?) {
        val welcome = getString(R.string.welcome)
        val displayName = model?.id
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

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
