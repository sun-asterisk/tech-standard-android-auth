package com.sun.auth.sample.biometrics.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sun.auth.biometricauth.BiometricHelper
import com.sun.auth.biometricauth.BiometricMode
import com.sun.auth.biometricauth.BiometricResult
import com.sun.auth.biometricauth.isBiometricAvailable
import com.sun.auth.biometricauth.isBiometricNotEnrolled
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
    private val biometricHelper by lazy { BiometricHelper.getInstance(requireContext()) }
    private val biometricState = BiometricState()

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
            biometricHelper.getEncryptedAuthenticationData() != null
        val isBiometricAvailable = context.isBiometricAvailable()

        val isChecked = if (hasBiometricLoginEnabled && !isBiometricAvailable) {
            // seem settings biometric was changed
            biometricHelper.removeEncryptedData()
            false
        } else {
            hasBiometricLoginEnabled
        }

        updateBiometricOption(
            biometricState.copy(
                isBiometricAvailable = isBiometricAvailable,
                isBiometricChecked = isChecked,
            ),
        )

        binding.tvId.text = "Welcome: ${viewModel.getToken()?.crAccessToken}"
    }

    private fun setupViews() {
        binding.swBiometric.setOnClickListener {
            if (!binding.swBiometric.isChecked) {
                disableBiometric()
            } else {
                if (context.isBiometricNotEnrolled()) {
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
                binding.tvId.text = "Updated: ${it.token?.accessToken}"
            }
        }
    }

    private fun showProcessError() {
        Toast.makeText(context, "Unexpected Error occurs", Toast.LENGTH_SHORT).show()
    }

    private fun updateBiometricOption(biometricState: BiometricState) {
        binding.swBiometric.apply {
            visibility = if (biometricState.isBiometricAvailable) View.VISIBLE else View.GONE
            isChecked = biometricState.isBiometricChecked
        }
    }

    private fun getBiometricPrompt(): BiometricPrompt.PromptInfo {
        return biometricHelper.createPromptInfo(
            context = requireContext(),
            title = "Biometric Authentication Sample",
            subtitle = "Enable biometric authentication",
            description = "Please complete biometric to enable biometric authentication",
            confirmationRequired = false,
            negativeTextButton = "Cancel",
            allowDeviceCredentials = false,
        )
    }

    private fun enableBiometricLogin() {
        biometricHelper.processBiometric(
            fragment = this,
            mode = BiometricMode.ENCRYPT,
            promptInfo = getBiometricPrompt(),
        ) {
            handleBiometricResult(it)
        }
    }

    private fun handleBiometricResult(biometricResult: BiometricResult) {
        if (biometricResult is BiometricResult.Success) {
            val cipher = biometricResult.getCipher()
            var cipherError = cipher == null
            cipher?.let {
                biometricHelper.encryptAndPersistAuthenticationData(
                    data = viewModel.getToken(),
                    cipher = it,
                    fallbackUnrecoverable = {
                        cipherError = true
                    },
                )
            }
            if (cipherError) {
                updateBiometricOption(biometricState.copy(isBiometricChecked = false))
            }
        } else {
            updateBiometricOption(biometricState.copy(isBiometricChecked = false))

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
        biometricHelper.removeEncryptedData()
        updateBiometricOption(biometricState.copy(isBiometricChecked = false))
    }

    private fun showEnrollBiometricDialog() {
        AlertUtils.showEnrollBiometricDialog(
            requireContext(),
            doOnNegativeClick = {
                updateBiometricOption(biometricState.copy(isBiometricChecked = false))
            },
            doOnPositiveClick = {
                updateBiometricOption(biometricState.copy(isBiometricChecked = false))
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
        Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }
}
