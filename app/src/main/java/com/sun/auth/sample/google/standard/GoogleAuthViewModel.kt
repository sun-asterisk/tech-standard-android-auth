package com.sun.auth.sample.google.standard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sun.auth.google.standard.GoogleStandardAuth
import com.sun.auth.google.standard.OneTapSignInCallback
import com.sun.auth.google.standard.SignInCallback
import com.sun.auth.google.standard.SignOutCallback
import com.sun.auth.sample.SocialAuthResult

class GoogleAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        GoogleStandardAuth.signIn()
    }

    fun logout() {
        GoogleStandardAuth.signOut(revokeAccess = true)
    }

    fun isLoggedIn(): Boolean {
        return GoogleStandardAuth.isSignedIn()
    }

    fun getUser(): GoogleSignInAccount? {
        return GoogleStandardAuth.getUser()
    }

    fun initGoogleSignIn(activity: FragmentActivity) {
        GoogleStandardAuth.initialize(
            activity,
            signInCallback = object : SignInCallback {
                override fun onResult(account: GoogleSignInAccount?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(data = account, exception = error)
                }
            },
            signOutCallback = object : SignOutCallback {
                override fun onResult(error: Throwable?) {
                    _signOutState.value = error
                }
            },
            oneTapSignInCallback = object : OneTapSignInCallback {
                override fun onResult(credential: SignInCredential?, error: Throwable?) {
                    // TODO: Handle your credentials if needed
                    // https://developers.google.com/identity/one-tap/android/get-saved-credentials#4_handle_the_users_response
                    // NOTE: about Stop displaying OneTap UI if user cancel multiple times
                    // https://developers.google.com/identity/one-tap/android/get-saved-credentials#disable-one-tap
                }
            },
        )
    }

    fun showOneTapSignIn() {
        GoogleStandardAuth.showOneTapSignIn()
    }
}
