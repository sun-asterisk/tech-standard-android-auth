package com.sun.auth.biometricauth

import javax.crypto.Cipher

internal interface CryptographyManager {

    fun getInitializedCipherForEncryption(keyName: String): Cipher

    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    fun <T> encryptData(data: T, cipher: Cipher): CiphertextWrapper

    fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, type: Class<T>): T

    fun persistCiphertextWrapperToSharedPrefs(prefKey: String, ciphertextWrapper: CiphertextWrapper)

    fun getCiphertextWrapperFromSharedPrefs(prefKey: String): CiphertextWrapper?

    fun removeCiphertextWrapperFromSharedPrefs(prefKey: String)

    fun removeAllCiphertextFromSharedPrefs()
}
