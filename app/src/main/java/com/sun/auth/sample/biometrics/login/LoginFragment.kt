package com.sun.auth.sample.biometrics.login

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sun.auth.biometricauth.BiometricAuth
import com.sun.auth.biometricauth.BiometricMode
import com.sun.auth.biometricauth.BiometricResult
import com.sun.auth.sample.R
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.biometrics.AlertUtils
import com.sun.auth.sample.biometrics.BaseFragment
import com.sun.auth.sample.databinding.FragmentLoginBinding
import com.sun.auth.sample.model.Token

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {

    override val fragmentLayout: Int get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory()).get(
            LoginViewModel::class.java,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observes()
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        updateBiometricLoginButton()
    }

    private fun goToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun updateBiometricLoginButton() {
        val hasBiometricLoginEnabled = BiometricAuth.getEncryptedAuthenticationData() != null
        val isBiometricAvailable = BiometricAuth.isBiometricAvailable()

        val isVisible = if (hasBiometricLoginEnabled && !isBiometricAvailable) {
            // seem settings biometric was changed
            BiometricAuth.removeEncryptedData()
            false
        } else {
            hasBiometricLoginEnabled
        }
        binding.loginBiometric.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun observes() {
        viewModel.signInFormState.observe(viewLifecycleOwner) { loginFormState ->
            if (loginFormState == null) {
                return@observe
            }
            binding.login.isEnabled = loginFormState.isDataValid
            loginFormState.usernameError?.let {
                binding.username.error = getString(it)
            }
            loginFormState.passwordError?.let {
                binding.password.error = getString(it)
            }
        }

        viewModel.credentialsAuthResult.observe(viewLifecycleOwner) { loginResult ->
            if (loginResult == null || loginResult.error != null) {
                Toast.makeText(context, "Login error", Toast.LENGTH_SHORT).show()
                return@observe
            }
            goToHome()
        }
    }

    private fun setupViews() {
        binding.username.doAfterTextChanged {
            viewModel.loginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString(),
            )
        }

        binding.password.apply {
            doAfterTextChanged {
                viewModel.loginDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString(),
                )
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    callSignIn()
                }
                false
            }
        }

        binding.login.setOnClickListener {
            callSignIn()
        }
        binding.loginBiometric.setOnClickListener {
            callBiometricSignIn()
        }
    }

    private fun callSignIn() {
        binding.loading.visibility = View.VISIBLE
        viewModel.signIn(
            binding.username.text.toString(),
            binding.password.text.toString(),
        )
    }

    private fun getBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricAuth.createPromptInfo(
            context = requireContext(),
            title = "Biometric Authentication Sample",
            subtitle = "Please complete biometric for authentication",
            description = "Complete Biometric for authentication",
            confirmationRequired = false,
            negativeTextButton = "Cancel",
        )
    }

    private fun callBiometricSignIn() {
        BiometricAuth.processBiometric(
            fragment = this,
            mode = BiometricMode.DECRYPT,
            promptInfo = getBiometricPrompt(),
        ) { result ->
            handleBiometricResult(result)
        }
    }

    private fun handleBiometricResult(biometricResult: BiometricResult) {
        if (biometricResult is BiometricResult.Success) {
            val cipher = biometricResult.getCipher()
            var cipherError = cipher == null
            val token = cipher?.let {
                BiometricAuth.decryptSavedAuthenticationData(
                    cipher = cipher,
                    clazz = Token::class.java,
                    fallbackUnrecoverable = {
                        cipherError = true
                    },
                )
            }
            if (cipherError) {
                binding.loginBiometric.visibility = View.GONE
                AlertUtils.showSecuritySettingChangedDialog(requireContext()) {
                    // do logout & remove biometric login data
                    // loginViewModel.signOut {  }
                    BiometricAuth.removeEncryptedData()
                }
            }

            if (token != null) {
                // From here we can use the token (or your authentication data).
                // For this sample, I can try another steps to make sure we can use the latest token
                // Because the biometric saved token may outdated with normal token (Ex: refresh token was called).
                /* val normalToken = viewModel.getToken()
                 val usedToken = if (token != normalToken && normalToken != null) {
                     // this case biometric saved token is outdated, update it and use normalToken
                     BiometricAuth.encryptAndPersistAuthenticationData(normalToken, cipher, null)
                     normalToken
                 } else {
                     // this account is signed out, so use the biometric saved token
                     // just update biometric saved token as a normal Token
                     // TODO: you should save token for check isLoggedIn
                     // Ex: yourSharedPref.saveToken(token)
                     CredentialsAuth.saveToken(token)
                     token
                 }
                 // Use of usedToken here
                 Log.d("LoginBiometric", usedToken.toString())*/

                goToHome()
            }
        } else {
            val isBiometricError =
                biometricResult is BiometricResult.Error && biometricResult.isBiometricLockout()
            if (isBiometricError) {
                binding.loginBiometric.visibility = View.GONE
                AlertUtils.showBiometricLockoutDialog(requireContext()) {
                    // do your logic
                }
            }
            val isCipherError =
                biometricResult is BiometricResult.RuntimeException && biometricResult.isKeyInvalidatedError()
            if (isCipherError) {
                AlertUtils.showSecuritySettingChangedDialog(requireContext()) {
                    binding.loginBiometric.visibility = View.GONE
                }
            } else {
                Toast.makeText(
                    context,
                    (biometricResult as? BiometricResult.Error)?.errorString.orEmpty(),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
}
