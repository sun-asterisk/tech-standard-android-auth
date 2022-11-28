package com.sun.sample.credentials

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sun.sample.R
import com.sun.sample.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val logout = binding.btnLogout
        val loginGroup = binding.loginGroup
        val mainGroup = binding.mainGroup
        val btnRefresh = binding.btnRefreshToken

        username.setText("linh@framgia.com")
        password.setText("abcd1234")

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        switchUi()

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = true

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.authenResult.observe(this@LoginActivity) {
            val loginResult = it ?: return@observe

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed()
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
        }

        loginViewModel.refreshTokenResult.observe(this@LoginActivity) {
            val refreshTokenResult = it ?: return@observe
            updateUiWithUser(refreshTokenResult.success)
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

        }

        logout.setOnClickListener {
            loginViewModel.logout {
                mainGroup.visibility = View.GONE
                loginGroup.visibility = View.VISIBLE
            }
        }

        btnRefresh.setOnClickListener {
            loginViewModel.refreshToken()
        }
    }

    private fun switchUi() {
        binding.tvId.text = "Welcome: ${loginViewModel.getToken()?.crAccessToken}"
        if (loginViewModel.isLoggedIn()) {
            binding.loginGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.loginGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
    }

    private fun updateUiWithUser(model: Token?) {
        val welcome = getString(R.string.welcome)
        val displayName = model?.id
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
        switchUi()
    }

    private fun showLoginFailed() {
        Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
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