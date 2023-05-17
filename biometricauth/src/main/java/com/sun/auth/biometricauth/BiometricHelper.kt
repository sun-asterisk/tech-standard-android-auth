package com.sun.auth.biometricauth

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class BiometricHelper private constructor() {
    private lateinit var cryptographyManager: CryptographyManager
    private var allowDeviceCredentials: Boolean = false

    private fun init(context: Context, allowDeviceCredentials: Boolean) {
        this.allowDeviceCredentials = allowDeviceCredentials
        cryptographyManager = CryptographyManagerImpl(context, allowDeviceCredentials)
    }

    /**
     * To encrypt the data after biometric process is success.
     * @param data The data you want to encrypt.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     */
    fun <T> encryptData(data: T, cipher: Cipher) = cryptographyManager.encryptData(data, cipher)

    /**
     * To encrypt the data after biometric process is success.
     * @param ciphertext The cipher text to decrypt.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     * @param clazz The Class of decrypted object to convert.
     */
    fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, clazz: Class<T>) =
        cryptographyManager.decryptData(ciphertext, cipher, clazz)

    /**
     * Persist the encrypted data to storage.
     *
     * @param encryptedData The Encrypted [EncryptedData] object want to save
     */
    fun saveEncryptedData(encryptedData: EncryptedData) {
        cryptographyManager.saveEncryptedData(encryptedData)
    }

    /**
     * Gets the saved encrypted data from storage.
     * @return The Encrypted [EncryptedData] object
     */
    fun getEncryptedData(): EncryptedData? {
        return cryptographyManager.getEncryptedData()
    }

    fun removeEncryptedData() {
        cryptographyManager.removeEncryptedData()
    }

    /**
     * Encrypt and persist the authentication data.
     * @param data The authentication data you want to encrypt and persist to storage.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     */
    fun <T> encryptAndPersistAuthenticationData(
        data: T,
        cipher: Cipher,
        fallbackUnrecoverable: (() -> Unit)? = null,
    ) {
        try {
            val encryptedData = encryptData(data, cipher)
            saveEncryptedData(encryptedData)
        } catch (e: UnableToEncryptData) {
            fallbackUnrecoverable?.invoke()
        }
    }

    /**
     * Encrypt and persist the authentication data.
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
        try {
            val encryptedData = getEncryptedData()
            if (encryptedData == null) {
                fallbackUnrecoverable?.invoke()
                return null
            }
            return decryptData(encryptedData.ciphertext, cipher, clazz)
        } catch (e: Exception) {
            fallbackUnrecoverable?.invoke()
            return null
        }
    }

    /**
     * Gets saved encrypted authentication data from shared preferences.
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
     * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
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
        allowDeviceCredentials: Boolean = false,
    ): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
        }

        val promptAuthenticators = context.getStrongestAuthenticators(allowDeviceCredentials)
        if (promptAuthenticators is StrongestAuthenticators.Available) {
            promptInfo.setAllowedAuthenticators((promptAuthenticators.authenticators))
            if (!promptAuthenticators.allowDeviceCredentials) {
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
            val context = fragment?.requireContext() ?: activity!!
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = if (fragment != null) {
                BiometricPrompt(fragment, executor, generateAuthenticationCallback(callback))
            } else {
                BiometricPrompt(activity!!, executor, generateAuthenticationCallback(callback))
            }
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

    companion object {
        private var instance: BiometricHelper? = null

        /**
         * Get the singleton instance of [BiometricHelper].
         * @param context The application context
         * @param allowDeviceCredentials Allow using device credentials to access keystore, default is false
         */
        fun getInstance(
            context: Context,
            allowDeviceCredentials: Boolean = false,
        ): BiometricHelper {
            if (instance == null) {
                instance = BiometricHelper().apply { init(context, allowDeviceCredentials) }
            }
            return instance!!
        }
    }
}
