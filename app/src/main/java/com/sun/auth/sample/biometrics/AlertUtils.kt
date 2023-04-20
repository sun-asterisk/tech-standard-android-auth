package com.sun.auth.sample.biometrics

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AlertUtils {

    fun showSecuritySettingChangedDialog(context: Context, doOnPositiveClick: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Biometric login disabled")
            .setMessage("The biometric settings on your device have changed. For security purposes, you will need to login again")
            .setPositiveButton("OK") { _, _ ->
                doOnPositiveClick()
            }
            .show()
    }

    fun showEnrollBiometricDialog(
        context: Context,
        doOnNegativeClick: () -> Unit,
        doOnPositiveClick: () -> Unit,
    ) {
        AlertDialog.Builder(context)
            .setTitle("Device Security")
            .setMessage("You need to enable device biometric first")
            .setNegativeButton("Cancel") { _, _ ->
                doOnNegativeClick()
            }
            .setPositiveButton("Enable") { _, _ ->
                doOnPositiveClick()
            }
            .show()
    }

    fun showBiometricLockoutDialog(context: Context, doOnPositiveClick: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Attempts exceeded")
            .setMessage("You have exceeded the maximum allowed biometric attempts, please try again later")
            .setPositiveButton("OK") { _, _ ->
                doOnPositiveClick()
            }
            .show()
    }
}
