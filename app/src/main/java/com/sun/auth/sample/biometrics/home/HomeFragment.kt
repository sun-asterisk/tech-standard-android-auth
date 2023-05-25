package com.sun.auth.sample.biometrics.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sun.auth.biometricauth.BiometricAuth
import com.sun.auth.biometricauth.BiometricMode
import com.sun.auth.biometricauth.BiometricResult
import com.sun.auth.sample.R
import com.sun.auth.sample.ViewModelFactory
import com.sun.auth.sample.biometrics.AlertUtils
import com.sun.auth.sample.biometrics.BaseFragment
import com.sun.auth.sample.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val fragmentLayout: Int get() = R.layout.fragment_home
    override val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory()).get(
            HomeViewModel::class.java,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observes()
    }

    override fun onResume() {
        super.onResume()
        validateData()
    }

    private fun validateData() {
        val hasBiometricLoginEnabled =
            BiometricAuth.getEncryptedAuthenticationData() != null
        val isBiometricAvailable = BiometricAuth.isBiometricAvailable()

        val isChecked = if (hasBiometricLoginEnabled && !isBiometricAvailable) {
            // seem settings biometric was changed
            BiometricAuth.removeEncryptedData()
            false
        } else {
            hasBiometricLoginEnabled
        }

        updateBiometricOption(isChecked)
        binding.tvId.text = "Welcome: ${viewModel.getToken()?.crAccessToken}"
    }

    private fun setupViews() {
        binding.swBiometric.setOnClickListener {
            if (!binding.swBiometric.isChecked) {
                disableBiometric()
            } else {
                if (BiometricAuth.isBiometricNotEnrolled()) {
                    showEnrollBiometricDialog()
                } else {
                    enableBiometricLogin()
                }
            }
        }
        binding.refreshToken.setOnClickListener {
            viewModel.refreshToken()
        }
        binding.signOut.setOnClickListener {
            viewModel.signOut {
                findNavController().popBackStack()
            }
        }
    }

    private fun observes() {
        viewModel.refreshTokenResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (it.error != null) {
                showProcessError()
            } else {
                binding.tvId.text = "Refreshed: ${it.token?.accessToken}"
            }
        }
    }

    private fun showProcessError() {
        Toast.makeText(context, "Unexpected Error occurs", Toast.LENGTH_SHORT).show()
    }

    private fun updateBiometricOption(isChecked: Boolean) {
        binding.swBiometric.isChecked = isChecked
    }

    private fun getBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricAuth.createPromptInfo(
            context = requireContext(),
            title = "Biometric Authentication Sample",
            subtitle = "Enable biometric authentication",
            description = "Please complete biometric to enable biometric authentication",
            confirmationRequired = false,
            negativeTextButton = "Cancel",
        )
    }

    private fun enableBiometricLogin() {
        BiometricAuth.processBiometric(
            fragment = this,
            mode = BiometricMode.ENCRYPT,
            promptInfo = getBiometricPrompt(),
        ) { result ->
            handleBiometricResult(result)
        }
    }

    private fun handleBiometricResult(biometricResult: BiometricResult) {
        if (biometricResult is BiometricResult.Success) {
            val cipher = biometricResult.getCipher()
            var cipherError = cipher == null
            cipher?.let {
                BiometricAuth.encryptAndPersistAuthenticationData(
                    data = viewModel.getToken(),
                    cipher = it,
                    fallbackUnrecoverable = {
                        cipherError = true
                    },
                )
            }
            if (cipherError) {
                updateBiometricOption(false)
            }
        } else {
            updateBiometricOption(false)

            val isBiometricError =
                biometricResult is BiometricResult.Error && biometricResult.isBiometricLockout()
            if (isBiometricError) {
                showBiometricLockoutDialog()
                return
            }

            val isCipherError = biometricResult is BiometricResult.RuntimeException &&
                biometricResult.isKeyInvalidatedError()
            if (isCipherError) {
                AlertUtils.showSecuritySettingChangedDialog(requireContext()) {
                    findNavController().popBackStack()
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

    private fun disableBiometric() {
        BiometricAuth.removeEncryptedData()
        updateBiometricOption(false)
    }

    private fun showEnrollBiometricDialog() {
        AlertUtils.showEnrollBiometricDialog(
            requireContext(),
            doOnNegativeClick = {
                updateBiometricOption(false)
            },
            doOnPositiveClick = {
                updateBiometricOption(false)
                goToAndroidSecuritySettings()
            },
        )
    }

    private fun showBiometricLockoutDialog() {
        AlertUtils.showBiometricLockoutDialog(requireContext()) {
            // do your logic
        }
    }

    private fun goToAndroidSecuritySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }
}
