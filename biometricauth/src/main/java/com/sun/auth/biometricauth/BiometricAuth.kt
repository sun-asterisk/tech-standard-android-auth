package com.sun.auth.biometricauth

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.sun.auth.biometricauth.internal.CryptographyManager
import com.sun.auth.biometricauth.internal.CryptographyManagerImpl
import com.sun.auth.biometricauth.internal.StrongestAuthenticators
import com.sun.auth.biometricauth.internal.UnableToDecryptData
import com.sun.auth.biometricauth.internal.UnableToEncryptData
import com.sun.auth.biometricauth.internal.UnableToInitializeCipher
import com.sun.auth.biometricauth.internal.getStrongestAuthenticators
import com.sun.auth.core.onException
import com.sun.auth.core.weak
import javax.crypto.Cipher

object BiometricAuth {
    private var context: Context? by weak(null)
    private var config: BiometricConfig? = null
    private lateinit var cryptographyManager: CryptographyManager

    @JvmStatic
    internal fun initialize(context: Context, config: BiometricConfig) {
        this.context = context.applicationContext
        this.config = config
        this.cryptographyManager = CryptographyManagerImpl(context, config)
    }

    /**
     * Check whether the biometric is available and enrolled
     * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
     * @return true if available and enrolled.
     */
    fun isBiometricAvailable(allowDeviceCredentials: Boolean = false): Boolean {
        return context?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.Available
    }

    /**
     * Check whether the biometric is unavailable or unsupported on this device
     * @param allowDeviceCredentials true if allows PIN/Pattern/Password to login
     * @return true if unavailable or unsupported
     */
    fun isBiometricUnAvailable(allowDeviceCredentials: Boolean = false): Boolean {
        return context?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.UnAvailable
    }

    /**
     * Check whether the biometric is insecure or not.
     * Insecure means the biometric is supported but the hardware sensor has security vulnerability.
     * @param allowDeviceCredentials true if allows PIN/Pattern/Password to login
     * @return true if biometric is insecure
     */
    fun isBiometricInsecure(allowDeviceCredentials: Boolean = false): Boolean {
        return context?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.InsecureHardWare
    }

    /**
     * Check whether the biometric is enrolled or not.
     * @param allowDeviceCredentials true if allows PIN/Pattern/Password to login
     * @return true if biometric is enrolled.
     */
    fun isBiometricNotEnrolled(allowDeviceCredentials: Boolean = false): Boolean {
        return context?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.NotEnrolled
    }

    /**
     * Encrypt data with given cipher.
     * @param data The data you want to encrypt.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     */
    fun <T> encryptData(data: T, cipher: Cipher) = cryptographyManager.encryptData(data, cipher)

    /**
     * Decrypt the cipher text with given cipher and convert to an object with clazz type.
     * @param ciphertext The cipher text to decrypt.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     * @param clazz The Class of decrypted object to convert.
     */
    fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, clazz: Class<T>) =
        cryptographyManager.decryptData(ciphertext, cipher, clazz)

    /**
     * Persist the encrypted data to storage.
     * @param encryptedData The encrypted [EncryptedData] object want to save
     */
    fun saveEncryptedData(encryptedData: EncryptedData) {
        cryptographyManager.saveEncryptedData(encryptedData)
    }

    /**
     * Gets the saved encrypted data from storage.
     * @return The encrypted [EncryptedData] object or null
     */
    fun getEncryptedData(): EncryptedData? {
        return cryptographyManager.getEncryptedData()
    }

    /**
     * Remove the encrypted data from storage.
     */
    fun removeEncryptedData() {
        cryptographyManager.removeEncryptedData()
    }

    /**
     * Encrypt and persist the authentication data with given cipher.
     * @param data The authentication data you want to encrypt and persist to storage.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     */
    fun <T> encryptAndPersistAuthenticationData(
        data: T,
        cipher: Cipher,
        fallbackUnrecoverable: (() -> Unit)? = null,
    ) {
        runCatching {
            val encryptedData = encryptData(data, cipher)
            saveEncryptedData(encryptedData)
        }.onException(UnableToInitializeCipher::class, UnableToEncryptData::class) {
            fallbackUnrecoverable?.invoke()
        }
    }

    /**
     * Decrypt the saved data with given cipher and convert to an object with clazz type.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     * @param clazz The Class of authentication object to convert.
     *
     * @return The authentication data or null
     */
    fun <T> decryptSavedAuthenticationData(
        cipher: Cipher,
        clazz: Class<T>,
        fallbackUnrecoverable: (() -> Unit)? = null,
    ): T? {
        val encryptedData = getEncryptedData()
        if (encryptedData == null) {
            fallbackUnrecoverable?.invoke()
            return null
        }
        return runCatching {
            decryptData(encryptedData.ciphertext, cipher, clazz)
        }.onException(UnableToInitializeCipher::class, UnableToDecryptData::class) {
            fallbackUnrecoverable?.invoke()
            null
        }.getOrNull()
    }

    /**
     * Gets saved encrypted authentication data from storage.
     * @return encrypted authentication data.
     */
    fun getEncryptedAuthenticationData(): EncryptedData? {
        return getEncryptedData()
    }

    /**
     * Create Biometric PromptInfo object with specified options.
     *
     * @param title required, title of prompt.
     * @param subtitle required, sub title of prompt.
     * @param description required, description of prompt.
     * @param confirmationRequired optional, Sets a system hint for whether to require explicit user
     *  confirmation after a passive biometric (e.g. iris or face) has been recognized,
     *  see [BiometricPrompt.PromptInfo.Builder.setConfirmationRequired].
     * @param negativeTextButton The label to be used for the negative button on the prompt.
     *  Note: only visible if not allow device credentials authentication (pin/password/pattern)
     * @return Biometric PromptInfo object with specified options.
     */
    @Suppress("LongParameterList")
    fun createPromptInfo(
        context: Context,
        title: String,
        subtitle: String,
        description: String,
        confirmationRequired: Boolean = false,
        negativeTextButton: String? = null,
    ): BiometricPrompt.PromptInfo {
        checkNotNull(config) { "Call initBiometricAuth() from your application first!" }
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
        }

        val strongestAuthenticators =
            context.getStrongestAuthenticators(config!!.allowDeviceCredentials)
        if (strongestAuthenticators is StrongestAuthenticators.Available) {
            promptInfo.setAllowedAuthenticators(strongestAuthenticators.authenticators)
            if (!strongestAuthenticators.allowDeviceCredentials) {
                promptInfo.setNegativeButtonText(negativeTextButton ?: "Cancel")
            }
        }
        return promptInfo.build()
    }

    /**
     * Start biometric process, verify and get biometric authentication result.
     *
     * @param fragment The current fragment
     * @param mode The biometric mode which want to launch, see [BiometricMode]
     * @param promptInfo The BiometricPrompt should appear and behave, use [createPromptInfo]
     * @param callback The callback with [BiometricResult] when biometric process complete.
     */
    fun processBiometric(
        fragment: Fragment? = null,
        mode: BiometricMode,
        promptInfo: BiometricPrompt.PromptInfo,
        callback: (result: BiometricResult) -> Unit,
    ) {
        processBiometricInternal(
            fragment = fragment,
            activity = null,
            mode = mode,
            promptInfo = promptInfo,
            callback = callback,
        )
    }

    /**
     * Start biometric process, verify and get biometric authentication result.
     *
     * @param activity The current fragment activity
     * @param mode The biometric mode which want to launch, see [BiometricMode]
     * @param promptInfo The BiometricPrompt should appear and behave, use [createPromptInfo]
     * @param callback The callback with [BiometricResult] when biometric process complete.
     */
    fun processBiometric(
        activity: FragmentActivity,
        mode: BiometricMode,
        promptInfo: BiometricPrompt.PromptInfo,
        callback: (result: BiometricResult) -> Unit,
    ) {
        processBiometricInternal(
            fragment = null,
            activity = activity,
            mode = mode,
            promptInfo = promptInfo,
            callback = callback,
        )
    }

    private fun processBiometricInternal(
        fragment: Fragment? = null,
        activity: FragmentActivity? = null,
        mode: BiometricMode,
        promptInfo: BiometricPrompt.PromptInfo,
        callback: (result: BiometricResult) -> Unit,
    ) {
        try {
            // init the cipher by mode
            val cipher = if (mode == BiometricMode.ENCRYPT) {
                cryptographyManager.getInitializedCipherForEncryption()
            } else {
                getEncryptedData()?.initializationVector?.let {
                    cryptographyManager.getInitializedCipherForDecryption(it)
                }
            }
            if (cipher == null) {
                callback.invoke(
                    BiometricResult.RuntimeException(UnableToInitializeCipher(null)),
                )
                return
            }
            val context = fragment?.requireContext() ?: activity!!
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = if (fragment != null) {
                BiometricPrompt(fragment, executor, generateAuthenticationCallback(callback))
            } else {
                BiometricPrompt(activity!!, executor, generateAuthenticationCallback(callback))
            }
            // verify biometric
            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Throwable) {
            if (e is UnableToDecryptData || e is UnableToInitializeCipher) {
                // can not work with current cipher anymore
                removeEncryptedData()
            }
            callback.invoke(BiometricResult.RuntimeException(e))
        }
    }

    private fun generateAuthenticationCallback(callback: (result: BiometricResult) -> Unit):
        BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                callback.invoke(BiometricResult.Error(errCode, errString.toString()))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback.invoke(BiometricResult.Failed)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback.invoke(BiometricResult.Success(result))
            }
        }
    }
}
