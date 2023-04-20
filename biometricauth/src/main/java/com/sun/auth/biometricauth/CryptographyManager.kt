package com.sun.auth.biometricauth

import javax.crypto.Cipher

internal interface CryptographyManager {

    fun getInitializedCipherForEncryption(): Cipher

    fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher

    fun <T> encryptData(data: T, cipher: Cipher): CipherData

    fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, type: Class<T>): T

    fun persistCiphertextWrapperToSharedPrefs(cipherData: CipherData)

    fun getCiphertextWrapperFromSharedPrefs(): CipherData?

    fun removeCiphertextWrapperFromSharedPrefs()

    fun removeAllCiphertextFromSharedPrefs()
}
