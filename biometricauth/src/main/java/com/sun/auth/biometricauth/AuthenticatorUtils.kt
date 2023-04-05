/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.auth.biometricauth

import android.os.Build
import androidx.biometric.BiometricManager

/**
 * AuthenticatorUtils version 1.2.0-alpha5
 * Utilities related to [BiometricManager.Authenticators] constants.
 */
internal object AuthenticatorUtils {
    /**
     * A bitmask for the portion of an authenticators value related to biometric sensor class.
     */
    private const val BIOMETRIC_CLASS_MASK = 0x7FFF

    /**
     * Converts the given set of allowed authenticator types to a unique, developer-readable string.
     *
     * @param authenticators A bit field representing a set of allowed authenticator types.
     * @return A string that uniquely identifies the set of authenticators and can be used in
     * developer-facing contexts (e.g. error messages).
     */
    fun convertToString(authenticators: Int): String {
        return when (authenticators) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG -> "BIOMETRIC_STRONG"
            BiometricManager.Authenticators.BIOMETRIC_WEAK -> "BIOMETRIC_WEAK"
            BiometricManager.Authenticators.DEVICE_CREDENTIAL -> "DEVICE_CREDENTIAL"
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL -> "BIOMETRIC_STRONG | DEVICE_CREDENTIAL"
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL -> "BIOMETRIC_WEAK | DEVICE_CREDENTIAL"
            else -> authenticators.toString()
        }
    }

    /**
     * Checks if the given set of allowed authenticator types is supported on this Android version.
     *
     * @param authenticators A bit field representing a set of allowed authenticator types.
     * @return Whether user authentication with the given set of allowed authenticator types is
     * supported on the current Android version.
     */
    fun isSupportedCombination(authenticators: Int): Boolean {
        return when (authenticators) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG,
            BiometricManager.Authenticators.BIOMETRIC_WEAK,
            BiometricManager.Authenticators.BIOMETRIC_WEAK
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            -> {
                true
            }
            BiometricManager.Authenticators.DEVICE_CREDENTIAL -> {
                // A biometric can be used instead of device credential prior to API 30.
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            }
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            -> {
                // A Class 2 (Weak) biometric can be used instead of device credential on API 28-29.
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
                    Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
            }
            else ->
                // 0 means "no authenticator types" and is supported. Other values are not.
                authenticators == 0
        }
    }

    /**
     * Checks if a device credential is included in the given set of allowed authenticator types.
     *
     * @param authenticators A bit field representing a set of allowed authenticator types.
     * @return Whether [BiometricManager.Authenticators.DEVICE_CREDENTIAL] is an allowed authenticator type.
     */
    fun isDeviceCredentialAllowed(authenticators: Int): Boolean {
        return authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL != 0
    }
}
