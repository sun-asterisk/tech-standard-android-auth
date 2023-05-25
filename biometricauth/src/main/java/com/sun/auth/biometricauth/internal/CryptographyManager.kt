package com.sun.auth.biometricauth.internal

import com.sun.auth.biometricauth.EncryptedData
import javax.crypto.Cipher

internal interface CryptographyManager {

    fun getInitializedCipherForEncryption(): Cipher

    fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher

    fun <T> encryptData(data: T, cipher: Cipher): EncryptedData

    fun <T> decryptData(ciphertext: ByteArray, cipher: Cipher, type: Class<T>): T

    fun saveEncryptedData(encryptedData: EncryptedData)

    fun getEncryptedData(): EncryptedData?

    fun removeEncryptedData()
}
