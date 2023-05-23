package com.sun.auth.biometricauth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import com.sun.auth.core.SharedPrefApi
import com.sun.auth.core.SharedPrefApiImpl
import com.sun.auth.core.onException
import com.sun.auth.core.weak
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableEntryException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

internal class CryptographyManagerImpl(
    boundContext: Context,
    private val allowDeviceCredentials: Boolean,
) : CryptographyManager {
    private var context: Context? by weak(null)
    private val gson by lazy { Gson() }
    private val sharedPrefApi: SharedPrefApi by lazy {
        checkNotNull(context) { "Context must be provided!" }
        SharedPrefApiImpl(context!!, gson)
    }

    init {
        context = boundContext
    }

    override fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()

        var secretKey = runCatching {
            getOrCreateSecretKey()
        }.onException(KeyPermanentlyInvalidatedException::class, InvalidKeyException::class) {
            // Biometric changes tend to cause exceptions here
            tryDeleteAndRecreateKey()
        }.getOrNull()

        runCatching {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }.onException(KeyPermanentlyInvalidatedException::class, InvalidKeyException::class) {
            // PIN/pattern/password changes tend to cause exceptions here
            secretKey = tryDeleteAndRecreateKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }.getOrNull()

        return cipher
    }

    override fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        runCatching {
            val secretKey = getOrCreateSecretKey()
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                IvParameterSpec(initializationVector),
            )
        }.onException(
            KeyPermanentlyInvalidatedException::class,
            InvalidKeyException::class,
        ) {
            throw UnableToInitializeCipher(it)
        }.getOrThrow()

        return cipher
    }

    override fun <T> encryptData(data: T, cipher: Cipher): EncryptedData {
        return runCatching {
            val ciphertext = cipher.doFinal(gson.toJson(data).toByteArray(Charsets.UTF_8))
            EncryptedData(ciphertext, cipher.iv)
        }.onException(IllegalBlockSizeException::class, BadPaddingException::class) {
            throw UnableToEncryptData(it)
        }.getOrThrow()
    }

    override fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, type: Class<T>): T {
        return runCatching {
            val plaintext = cipher.doFinal(ciphertext)
            gson.fromJson(String(plaintext, Charsets.UTF_8), type)
        }.onException(
            InvalidAlgorithmParameterException::class,
            IllegalBlockSizeException::class,
            BadPaddingException::class,
        ) {
            throw UnableToDecryptData(it)
        }.getOrThrow<T>()
    }

    private fun getCipher(): Cipher {
        val transformation = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getAndLoadKeystore(): KeyStore {
        return KeyStore.getInstance(KEYSTORE).apply { load(null) }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keystore = getAndLoadKeystore()
        return if (keystore.containsAlias(BIOMETRIC_KEY_NAME)) {
            runCatching {
                keystore.getKey(BIOMETRIC_KEY_NAME, null) as SecretKey
            }.onException(
                NoSuchAlgorithmException::class,
                UnrecoverableEntryException::class,
                KeyStoreException::class,
            ) {
                tryDeleteAndRecreateKey()
            }.getOrThrow()
        } else {
            createSecretKey()
        }
    }

    private fun tryDeleteAndRecreateKey(): SecretKey {
        val keyStore = getAndLoadKeystore()
        try {
            keyStore.deleteEntry(BIOMETRIC_KEY_NAME)
        } catch (ignored: KeyStoreException) {
            // If the key cannot be deleted, then create a new one in its place
        }
        return createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        // No key found, a new SecretKey must be generated for the given keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            /* keystoreAlias = */
            BIOMETRIC_KEY_NAME,
            /* purposes = */
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).apply {
            setBlockModes(BLOCK_MODE)
            setEncryptionPaddings(PADDING)
            setKeySize(KEY_SIZE)

            setRandomizedEncryptionRequired(true)
            // FIXME: Temporary remove crash when using biometric on Android 13+
            setUserAuthenticationRequired(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // detect new biometric added or old biometric removed
                setInvalidatedByBiometricEnrollment(true)
                setUserAuthenticationValidWhileOnBody(false)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val hasStrongBox = context
                    ?.packageManager
                    ?.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
                    ?: false

                if (hasStrongBox) setIsStrongBoxBacked(true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (allowDeviceCredentials) {
                    setUserAuthenticationParameters(
                        0,
                        KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
                    )
                } else {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            } else {
                // Require authentication with biometric every time
                setUserAuthenticationValidityDurationSeconds(if (allowDeviceCredentials) 0 else 1)
            }
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            /* algorithm = */ KeyProperties.KEY_ALGORITHM_AES,
            /* provider = */ KEYSTORE,
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun saveEncryptedData(encryptedData: EncryptedData) {
        sharedPrefApi.put(PREF_KEY_ENCRYPTED_DATA, encryptedData)
    }

    override fun getEncryptedData(): EncryptedData? {
        return sharedPrefApi.get(PREF_KEY_ENCRYPTED_DATA, EncryptedData::class.java)
    }

    override fun removeEncryptedData() {
        sharedPrefApi.removeKey(PREF_KEY_ENCRYPTED_DATA)
    }

    override fun clearEncryptedData() {
        sharedPrefApi.clear()
    }

    companion object {
        private const val KEY_SIZE = 256
        private const val KEYSTORE = "AndroidKeyStore"
        internal const val PREF_KEY_ENCRYPTED_DATA = "SUN_AUTH_PREF_KEY_ENCRYPTED_DATA"
        internal const val BIOMETRIC_KEY_NAME = "SUN_AUTH_BIOMETRIC_KEY_NAME"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    }
}
