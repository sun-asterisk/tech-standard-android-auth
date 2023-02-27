package com.sun.auth.sample

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.sun.auth.core.CancellationAuthException
import com.sun.auth.core.InvalidCredentialsException
import com.sun.auth.core.SocialAuthException
import com.sun.auth.core.UnexpectedAuthException

// NOTE: This is sample code, you should handle it better with your code flow.
fun FragmentActivity.handleSocialAuthError(error: Throwable) {
    val message = when (error) {
        is CancellationAuthException -> "User cancel signIn process!"
        is InvalidCredentialsException -> "Device's time or provided credentials is incorrect!"
        is UnexpectedAuthException -> "Provider can not verify this user, try later!"
        is SocialAuthException -> {
            error.originalThrowable?.message.orEmpty()
        }
        else -> "An unknown error occurs, try later!"
    }
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
