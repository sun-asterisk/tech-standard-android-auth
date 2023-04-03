package com.sun.auth.biometricauth

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class CryptographyManagerImpl(
    private val sharedPrefApi: SharedPrefApi,
    private val gson: Gson,
) : CryptographyManager {

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun getInitializedCipherForDecryption(
        keyName: String,
        initializationVector: ByteArray,
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(
                /* tLen = */ AUTHENTICATION_TAG_BIT_LENGTH,
                /* src = */ initializationVector,
            ),
        )
        return cipher
    }

    override fun <T> encryptData(data: T, cipher: Cipher): CiphertextWrapper {
        val ciphertext = cipher.doFinal(gson.toJson(data).toByteArray(Charsets.UTF_8))
        return CiphertextWrapper(ciphertext, cipher.iv)
    }

    override fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, type: Class<T>): T {
        val plaintext = cipher.doFinal(ciphertext)
        return gson.fromJson(String(plaintext, Charsets.UTF_8), type)
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let {
            // If SecretKey was previously created for that keyName, then grab and return it.
            return it as SecretKey
        }

        // No key found, a new SecretKey must be generated for the given keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            /* keystoreAlias = */ keyName,
            /* purposes = */ KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setInvalidatedByBiometricEnrollment(true)
            }
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            /* algorithm = */ KeyProperties.KEY_ALGORITHM_AES,
            /* provider = */ ANDROID_KEYSTORE,
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun persistCiphertextWrapperToSharedPrefs(
        prefKey: String,
        ciphertextWrapper: CiphertextWrapper,
    ) {
        sharedPrefApi.put(prefKey, ciphertextWrapper)
    }

    override fun getCiphertextWrapperFromSharedPrefs(prefKey: String): CiphertextWrapper? {
        return sharedPrefApi.get(prefKey, CiphertextWrapper::class.java)
    }

    override fun removeCiphertextWrapperFromSharedPrefs(prefKey: String) {
        sharedPrefApi.removeKey(prefKey)
    }

    override fun removeAllCiphertextFromSharedPrefs() {
        sharedPrefApi.clear()
    }

    companion object {
        private const val AUTHENTICATION_TAG_BIT_LENGTH = 128
        private const val KEY_SIZE = 256
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        internal const val BIOMETRIC_CYPHER_KEY = "BIOMETRIC_CYPHER_KEY"
        internal const val BIOMETRIC_KEY_NAME = "BiometricKeyName"
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }
}
