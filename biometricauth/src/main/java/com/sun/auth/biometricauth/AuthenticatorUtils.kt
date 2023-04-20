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

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

var allowDeviceCredentials = false
sealed interface StrongestAuthenticators {
    data class Available(val authenticators: Int) : StrongestAuthenticators {
        val allowDeviceCredentials: Boolean
            get() = authenticators and DEVICE_CREDENTIAL != 0
    }
    object InsecureHardWare : StrongestAuthenticators
    object NotAvailable : StrongestAuthenticators
    object NotEnrolled : StrongestAuthenticators
}

internal fun BiometricManager.mapToStrongestAuthenticators(authenticators: Int): StrongestAuthenticators {
    return when (canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> StrongestAuthenticators.Available(authenticators)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> StrongestAuthenticators.InsecureHardWare
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> StrongestAuthenticators.NotEnrolled
        else -> StrongestAuthenticators.NotAvailable
        // else cases
        // BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        // BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
        // BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
        // BiometricManager.BIOMETRIC_STATUS_UNKNOWN
    }
}

internal fun Context.getStrongestAuthenticators(): StrongestAuthenticators {
    val biometricManager = BiometricManager.from(this)
    val authenticatorsToTry =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && allowDeviceCredentials) {
            arrayListOf(
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
                BIOMETRIC_STRONG,
                BIOMETRIC_WEAK or DEVICE_CREDENTIAL,
                BIOMETRIC_WEAK,
                DEVICE_CREDENTIAL,
            )
        } else {
            arrayListOf(BIOMETRIC_STRONG, BIOMETRIC_WEAK)
        }
    authenticatorsToTry.forEach { authenticator ->
        val outcome = biometricManager.mapToStrongestAuthenticators(authenticator)
        if (outcome !is StrongestAuthenticators.NotAvailable) {
            return outcome
        }
    }
    return StrongestAuthenticators.NotAvailable
}
