package com.sun.auth.sample.biometric

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.biometricauth.BiometricHelper
import com.sun.auth.biometricauth.BiometricMode
import com.sun.auth.biometricauth.BiometricPromptUtils
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.databinding.ActivityLoginBiometricBinding
import com.sun.auth.sample.model.Token
import javax.crypto.Cipher

class BiometricLoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory()).get(LoginViewModel::class.java)
    }

    private lateinit var binding: ActivityLoginBiometricBinding
    private val biometricHelper by lazy { BiometricHelper.getInstance(this) }
    private val cipherToken
        get() = biometricHelper.getAuthenticationDataCipherFromSharedPrefs() // default support
    // get() = biometricHelper.getCiphertextWrapperFromSharedPrefs(KEY_CIPHER_TOKEN) // your custom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBiometricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observes()
    }

    override fun onResume() {
        super.onResume()
        updateButtonSignInBiometricVisibility()
    }

    private fun getBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPromptUtils.createPromptInfo(
            title = "Biometric Authentication Sample",
            subtitle = "Please complete biometric for authentication",
            description = "Complete Biometric for authentication",
            confirmationRequired = false,
            negativeTextButton = "cancel",
        )
    }

    private fun showBiometricPrompt(mode: BiometricMode) {
        biometricHelper.processBiometric(
            activity = this,
            mode = mode,
            cipherTextWrapper = cipherToken,
            promptInfo = getBiometricPrompt(),
            authenticators = BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            secretKey = "This is error",
            onError = ::doOnBiometricError,
            onSuccess = {
                doOnBiometricSuccess(it, mode)
            },
        )
    }

    private fun doOnBiometricError(errCode: Int?, errString: String?) {
        binding.swBiometric.isChecked = cipherToken != null
        if (errCode == null || errString.isNullOrBlank()) {
            showProcessError()
            return
        }
        val message = when (errCode) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "the hardware is unavailable. Try again later."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "no biometric or device credential is enrolled."
            // TODO: Handle other error if needed
            else -> errString
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun doOnBiometricSuccess(
        authResult: BiometricPrompt.AuthenticationResult,
        mode: BiometricMode,
    ) {
        authResult.cryptoObject?.cipher?.let {
            when (mode) {
                BiometricMode.DECRYPT -> decryptServerTokenFromStorage(it)
                BiometricMode.ON -> encryptAndStoreServerToken(it)
                else -> {
                    // do nothing, just update swBiometric checked value.
                    // You can add your logic when biometric setting is off
                    // From now all encrypted data are removed
                }
            }
        }
        binding.swBiometric.isChecked = cipherToken != null
    }

    private fun decryptServerTokenFromStorage(cipher: Cipher) {
        /*
        val token = biometricHelper.decryptData(
            cipher = cipherToken.cipherText,
            prefKey = KEY_CIPHER_TOKEN,
            clazz = Token::class.java
        )
         */

        // default support
        val token = biometricHelper.decryptSavedAuthenticationData(
            cipher = cipher,
            clazz = Token::class.java,
        )
        loginViewModel.updateToken(token)
    }

    private fun encryptAndStoreServerToken(cipher: Cipher) {
        loginViewModel.getToken()?.let { token ->
            /*val encryptedServerToken = biometricHelper.encryptData(token, cipher)
            biometricHelper.persistCiphertextWrapperToSharedPrefs(
                KEY_CIPHER_TOKEN,
                encryptedServerToken
            )*/

            // default support
            biometricHelper.encryptAndPersistAuthenticationData(
                data = token,
                cipher = cipher,
            )
        }
    }

    private fun setupViews() {
        binding.username.doAfterTextChanged {
            loginViewModel.signInDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString(),
            )
        }
        binding.password.apply {
            doAfterTextChanged {
                loginViewModel.signInDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> loginViewModel.signIn(
                        binding.username.text.toString(),
                        binding.password.text.toString(),
                    )
                }
                false
            }
            binding.signIn.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                loginViewModel.signIn(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }
        }
        binding.signOut.setOnClickListener {
            loginViewModel.signOut {
                binding.mainGroup.visibility = View.GONE
                binding.signInGroup.visibility = View.VISIBLE
            }
        }
        binding.refreshToken.setOnClickListener {
            loginViewModel.refreshToken()
        }
        binding.signInWithBiometric.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                showBiometricPrompt(BiometricMode.DECRYPT)
            }
        }
        binding.swBiometric.apply {
            isChecked = cipherToken != null
            setOnClickListener {
                if (isChecked) {
                    showBiometricPrompt(BiometricMode.ON)
                } else {
                    showBiometricPrompt(BiometricMode.OFF)
                }
            }
        }
    }

    private fun observes() {
        loginViewModel.signInFormState.observe(this@BiometricLoginActivity) {
            val signInState = it ?: return@observe
            if (signInState.usernameError != null) {
                binding.username.error = getString(signInState.usernameError)
            }
            if (signInState.passwordError != null) {
                binding.password.error = getString(signInState.passwordError)
            }
        }

        loginViewModel.credentialsAuthResult.observe(this@BiometricLoginActivity) {
            val signInResult = it ?: return@observe
            binding.loading.visibility = View.GONE
            if (signInResult.error != null) {
                showProcessError()
            }
        }

        loginViewModel.refreshTokenResult.observe(this@BiometricLoginActivity) {
            it ?: return@observe
            if (it.error != null) {
                showProcessError()
            } else {
                binding.tvId.text = "Updated: ${it.token?.accessToken}"
            }
        }
        loginViewModel.userToken.observe(this) {
            switchUi()
        }
    }

    private fun switchUi() {
        binding.tvId.text = "Welcome: ${loginViewModel.getToken()?.crAccessToken}"
        if (loginViewModel.isSignedIn()) {
            binding.signInGroup.visibility = View.GONE
            binding.mainGroup.visibility = View.VISIBLE
        } else {
            binding.signInGroup.visibility = View.VISIBLE
            binding.mainGroup.visibility = View.GONE
        }
        updateButtonSignInBiometricVisibility()
    }

    private fun updateButtonSignInBiometricVisibility() {
        if (cipherToken == null) {
            binding.signInWithBiometric.visibility = View.GONE
        } else {
            // this app already setup biometric authentication
            binding.signInWithBiometric.visibility = View.VISIBLE
        }
    }

    private fun showProcessError() {
        Toast.makeText(applicationContext, "Unexpected Error occurs", Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private const val KEY_CIPHER_TOKEN = "KEY_CIPHER_TOKEN"
    }
}
