package com.sun.auth.biometricauth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.sun.auth.biometricauth.BiometricError.ERROR_AUTHENTICATOR_CONFLICT
import com.sun.auth.biometricauth.BiometricError.ERROR_BIOMETRIC_MODE_IS_OFF
import com.sun.auth.biometricauth.BiometricError.ERROR_BIOMETRIC_NOT_SET
import com.sun.auth.biometricauth.BiometricError.ERROR_NO_CIPHER_CREATED
import com.sun.auth.biometricauth.BiometricError.MESSAGE_AUTHENTICATORS_CONFLICT
import com.sun.auth.biometricauth.BiometricError.MESSAGE_BIOMETRIC_MODE_IS_OFF
import com.sun.auth.biometricauth.BiometricError.MESSAGE_BIOMETRIC_PROCESS_FAIL
import com.sun.auth.biometricauth.BiometricError.MESSAGE_BIOMETRIC_UN_SUPPORTED
import com.sun.auth.biometricauth.BiometricError.MESSAGE_NO_BIOMETRIC_SETTINGS
import com.sun.auth.biometricauth.BiometricError.MESSAGE_NO_CIPHER_CREATED
import com.sun.auth.biometricauth.CryptographyManagerImpl.Companion.BIOMETRIC_CYPHER_KEY
import com.sun.auth.biometricauth.CryptographyManagerImpl.Companion.BIOMETRIC_KEY_NAME
import com.sun.auth.core.weak
import javax.crypto.Cipher

@Suppress("TooManyFunctions", "UnusedPrivateMember", "LongParameterList", "CyclomaticComplexMethod")
class BiometricHelper private constructor() {
    private val gson = Gson()
    private var context: Context? by weak(null)
    private lateinit var cryptographyManager: CryptographyManager
    private fun init(context: Context) {
        this.context = context.applicationContext
        cryptographyManager = CryptographyManagerImpl(SharedPrefApiImpl(context, gson), gson)
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
     * @param prefKey The preference key
     * @param ciphertextWrapper The Encrypted [CiphertextWrapper] object want to save
     */
    fun persistCiphertextWrapperToSharedPrefs(
        prefKey: String,
        ciphertextWrapper: CiphertextWrapper,
    ) {
        cryptographyManager.persistCiphertextWrapperToSharedPrefs(prefKey, ciphertextWrapper)
    }

    /**
     * Gets the saved cipher text from storage.
     * @param prefKey The preference key
     * @return The Encrypted [CiphertextWrapper] object
     */
    fun getCiphertextWrapperFromSharedPrefs(prefKey: String): CiphertextWrapper? {
        return cryptographyManager.getCiphertextWrapperFromSharedPrefs(prefKey)
    }

    fun removeCiphertextWrapperFromSharedPrefs(prefKey: String) {
        cryptographyManager.removeCiphertextWrapperFromSharedPrefs(prefKey)
    }

    /**
     * Encrypt and persist the authentication data.
     * @param data The authentication data you want to encrypt and persist to storage.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     */
    fun <T> encryptAndPersistAuthenticationData(data: T, cipher: Cipher) {
        try {
            val encryptedData = encryptData(data, cipher)
            persistCiphertextWrapperToSharedPrefs(BIOMETRIC_CYPHER_KEY, encryptedData)
        } catch (e: Exception) {
            Log.e("x", "${e.printStackTrace()}")
        }
    }

    /**
     * Encrypt and persist the authentication data.
     * @param cipher The cipher from [BiometricPrompt.CryptoObject] after [BiometricPrompt.AuthenticationResult] success.
     * @param clazz The Class of authentication object to convert.
     *
     * @return The authentication data or null
     */
    fun <T> decryptSavedAuthenticationData(cipher: Cipher, clazz: Class<T>): T? {
        val encryptedData = getCiphertextWrapperFromSharedPrefs(BIOMETRIC_CYPHER_KEY)
        encryptedData ?: return null
        return decryptData(encryptedData.ciphertext, cipher, clazz)
    }

    /**
     * Gets saved encrypted authentication data from storage.
     * @return encrypted authentication data.
     */
    fun getAuthenticationDataCipherFromSharedPrefs(): CiphertextWrapper? {
        return getCiphertextWrapperFromSharedPrefs(BIOMETRIC_CYPHER_KEY)
    }

    /**
     * Start biometric process, verify and get biometric authentication result.
     *
     * @param fragment The current fragment
     * @param mode The biometric mode which want to launch, see [BiometricMode]
     * @param cipherTextWrapper The saved cipher text, null if biometric mode is [BiometricMode.ON]
     * @param promptInfo The BiometricPrompt should appear and behave, use [BiometricPromptUtils.createPromptInfo]
     * @param authenticators A bit field representing the types of [Authenticators] that maybe used for authentication.
     * @param secretKey The key name generate when create a cipher.
     * @param onError The callback when biometric process fail.
     * @param onSuccess The callback when biometric process success.
     */
    private fun processBiometric(
        fragment: Fragment? = null,
        mode: BiometricMode,
        cipherTextWrapper: CiphertextWrapper?,
        promptInfo: BiometricPrompt.PromptInfo,
        authenticators: Int? = null,
        secretKey: String = BIOMETRIC_KEY_NAME,
        onError: ((code: Int?, message: String?) -> Unit)? = null,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
    ) {
        processBiometricInternal(
            fragment = fragment,
            activity = null,
            mode = mode,
            cipherTextWrapper = cipherTextWrapper,
            promptInfo = promptInfo,
            authenticators = authenticators,
            secretKey = secretKey,
            onError = onError,
            onSuccess = onSuccess,
        )
    }

    /**
     * Start biometric process, verify and get biometric authentication result.
     *
     * @param activity The current fragment activity
     * @param mode The biometric mode which want to launch, see [BiometricMode]
     * @param cipherTextWrapper The saved cipher text, null if biometric mode is [BiometricMode.ON]
     * @param promptInfo The BiometricPrompt should appear and behave, use [BiometricPromptUtils.createPromptInfo]
     * @param authenticators A bit field representing the types of [Authenticators] that maybe used for authentication.
     * @param secretKey The key name generate when create a cipher.
     * @param onError The callback when biometric process fail.
     * @param onSuccess The callback when biometric process success.
     */
    fun processBiometric(
        activity: FragmentActivity,
        mode: BiometricMode,
        cipherTextWrapper: CiphertextWrapper?,
        promptInfo: BiometricPrompt.PromptInfo,
        authenticators: Int? = null,
        secretKey: String = BIOMETRIC_KEY_NAME,
        onError: ((code: Int?, message: String?) -> Unit)? = null,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
    ) {
        processBiometricInternal(
            fragment = null,
            activity = activity,
            mode = mode,
            cipherTextWrapper = cipherTextWrapper,
            promptInfo = promptInfo,
            authenticators = authenticators,
            secretKey = secretKey,
            onError = onError,
            onSuccess = onSuccess,
        )
    }

    private fun processBiometricInternal(
        fragment: Fragment? = null,
        activity: FragmentActivity? = null,
        mode: BiometricMode,
        cipherTextWrapper: CiphertextWrapper?,
        promptInfo: BiometricPrompt.PromptInfo,
        authenticators: Int?,
        secretKey: String,
        onError: ((code: Int?, message: String?) -> Unit)?,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
    ) {
        if (authenticators != null && !AuthenticatorUtils.isSupportedCombination(authenticators)) {
            onError?.invoke(BIOMETRIC_ERROR_UNSUPPORTED, MESSAGE_BIOMETRIC_UN_SUPPORTED)
            return
        }

        if (mode != BiometricMode.ON && cipherTextWrapper == null) {
            onError?.invoke(ERROR_BIOMETRIC_MODE_IS_OFF, MESSAGE_BIOMETRIC_MODE_IS_OFF)
            return
        }

        // Verify required authenticators
        val finalAuthenticators = if (promptInfo.allowedAuthenticators == 0) {
            authenticators // default no set authenticators
        } else {
            if (promptInfo.allowedAuthenticators != authenticators) {
                onError?.invoke(ERROR_AUTHENTICATOR_CONFLICT, MESSAGE_AUTHENTICATORS_CONFLICT)
                return
            } else {
                authenticators
            }
        }
        try {
            val context = fragment?.requireContext() ?: activity!!
            val canAuthenticate = from(context).canAuthenticate(finalAuthenticators!!)
            if (canAuthenticate == BIOMETRIC_SUCCESS) {
                val cipher = if (mode == BiometricMode.ON) {
                    cryptographyManager.getInitializedCipherForEncryption(secretKey)
                } else {
                    cipherTextWrapper?.initializationVector?.let {
                        cryptographyManager.getInitializedCipherForDecryption(secretKey, it)
                    }
                }
                if (cipher == null) {
                    onError?.invoke(ERROR_NO_CIPHER_CREATED, MESSAGE_NO_CIPHER_CREATED)
                    return
                }
                if (fragment != null) {
                    BiometricPromptUtils.createBiometricPrompt(
                        fragment = fragment,
                        doOnError = { errorCode, errorString ->
                            if (errorCode == BIOMETRIC_ERROR_HW_UNAVAILABLE ||
                                errorCode == BIOMETRIC_ERROR_NONE_ENROLLED
                            ) {
                                // The biometric on devices has changed
                                cryptographyManager.removeAllCiphertextFromSharedPrefs()
                            }
                            onError?.invoke(errorCode, errorString?.toString())
                        },
                        doOnSuccess = { result ->
                            if (mode == BiometricMode.OFF) {
                                // Turn off biometric mode, then clear all saved cipher texts.
                                cryptographyManager.removeAllCiphertextFromSharedPrefs()
                            }
                            onSuccess.invoke(result)
                        },
                    ).authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                }

                if (activity != null) {
                    BiometricPromptUtils.createBiometricPrompt(
                        activity = activity,
                        doOnError = { errorCode, errorString ->
                            if (errorCode == BIOMETRIC_ERROR_HW_UNAVAILABLE ||
                                errorCode == BIOMETRIC_ERROR_NONE_ENROLLED
                            ) {
                                // The biometric on devices has changed
                                cryptographyManager.removeAllCiphertextFromSharedPrefs()
                            }
                            onError?.invoke(errorCode, errorString?.toString())
                        },
                        doOnSuccess = { result ->
                            if (mode == BiometricMode.OFF) {
                                // Turn off biometric authentication mode, then clear all saved cipher texts.
                                cryptographyManager.removeAllCiphertextFromSharedPrefs()
                            }
                            onSuccess.invoke(result)
                        },
                    ).authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                }
            } else {
                onError?.invoke(canAuthenticate, MESSAGE_BIOMETRIC_PROCESS_FAIL)
            }
        } catch (ignored: Exception) {
            onError?.invoke(ERROR_BIOMETRIC_NOT_SET, MESSAGE_NO_BIOMETRIC_SETTINGS)
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
