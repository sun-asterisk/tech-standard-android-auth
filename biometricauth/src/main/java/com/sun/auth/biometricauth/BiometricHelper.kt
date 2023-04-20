package com.sun.auth.biometricauth

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.sun.auth.core.weak
import javax.crypto.Cipher

@Suppress("TooManyFunctions", "UnusedPrivateMember", "LongParameterList", "CyclomaticComplexMethod")
class BiometricHelper private constructor() {
    private val gson = Gson()
    private var context: Context? by weak(null)
    private lateinit var cryptographyManager: CryptographyManager
    private fun init(context: Context) {
        this.context = context
        cryptographyManager =
            CryptographyManagerImpl(boundContext = context.applicationContext, gson = gson)
    }

    fun isBiometricAvailable(): Boolean {
        return context?.getStrongestAuthenticators() is StrongestAuthenticators.Available
    }

    fun isBiometricNotEnrolled(): Boolean {
        return context?.getStrongestAuthenticators() is StrongestAuthenticators.NotEnrolled
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
     * Persist the cipher text to storage.
     *
     * @param cipherData The Encrypted [CipherData] object want to save
     */
    fun persistCiphertextWrapperToSharedPrefs(cipherData: CipherData) {
        cryptographyManager.persistCiphertextWrapperToSharedPrefs(cipherData)
    }

    /**
     * Gets the saved cipher text from storage.
     * @return The Encrypted [CipherData] object
     */
    fun getCiphertextWrapperFromSharedPrefs(): CipherData? {
        return cryptographyManager.getCiphertextWrapperFromSharedPrefs()
    }

    fun removeCiphertextWrapperFromSharedPrefs() {
        cryptographyManager.removeCiphertextWrapperFromSharedPrefs()
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
            persistCiphertextWrapperToSharedPrefs(encryptedData)
        } catch (e: UnableEncryptData) {
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
            val encryptedData = getCiphertextWrapperFromSharedPrefs()
            if (encryptedData == null) {
                fallbackUnrecoverable?.invoke()
                return null
            }
            return decryptData(encryptedData.ciphertext, cipher, clazz)
        } catch (e: Exception) {
//            removeCiphertextWrapperFromSharedPrefs()
            fallbackUnrecoverable?.invoke()
            return null
        }
    }

    /**
     * Gets saved encrypted authentication data from storage.
     * @return encrypted authentication data.
     */
    fun getAuthenticationDataCipherFromSharedPrefs(): CipherData? {
        return getCiphertextWrapperFromSharedPrefs()
    }

    /**
     * Start biometric process, verify and get biometric authentication result.
     *
     * @param fragment The current fragment
     * @param mode The biometric mode which want to launch, see [BiometricMode]
     * @param promptInfo The BiometricPrompt should appear and behave, use [BiometricPromptUtils.createPromptInfo]
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
     * @param promptInfo The BiometricPrompt should appear and behave, use [BiometricPromptUtils.createPromptInfo]
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
            val cipher = if (mode == BiometricMode.ON) {
                cryptographyManager.getInitializedCipherForEncryption()
            } else {
                getCiphertextWrapperFromSharedPrefs()?.initializationVector?.let {
                    cryptographyManager.getInitializedCipherForDecryption(it)
                }
            }
            if (cipher == null) {
                callback.invoke(BiometricResult.Failed)
                return
            }
            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Throwable) {
            if (e is UnableDecryptData || e is UnableInitializeCipher) {
                // can not work with current cipher anymore
                removeCiphertextWrapperFromSharedPrefs()
            }
            callback.invoke(BiometricResult.BiometricRuntimeException(e))
        }
    }

    private fun generateAuthenticationCallback(callback: (result: BiometricResult) -> Unit): BiometricPrompt.AuthenticationCallback {
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

        fun getInstance(context: Context): BiometricHelper {
            if (instance == null) {
                instance = BiometricHelper().apply { init(context) }
            }
            return instance!!
        }
    }
}
