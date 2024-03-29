package com.sun.auth.sample.google.standard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback
import com.sun.auth.google.standard.GoogleStandardAuth
import com.sun.auth.google.standard.OneTapSignInCallback
import com.sun.auth.sample.SocialAuthResult

class GoogleAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        GoogleStandardAuth.signIn()
    }

    fun signOut() {
        GoogleStandardAuth.signOut(
            revokeAccess = true,
            signOutCallback = object : SignOutCallback {
                override fun onResult(error: Throwable?) {
                    _signOutState.value = error
                }
            },
        )
    }

    fun isSignedIn(): Boolean {
        return GoogleStandardAuth.isSignedIn()
    }

    fun getUser(): GoogleSignInAccount? {
        return GoogleStandardAuth.getUser()
    }

    fun initGoogleSignIn(activity: FragmentActivity) {
        GoogleStandardAuth.initialize(
            activity,
            signInCallback = object : SignInCallback<GoogleSignInAccount> {
                override fun onResult(data: GoogleSignInAccount?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(data = data, error = error)
                }
            },
        )
    }

    fun showOneTapSignIn() {
        GoogleStandardAuth.showOneTapSignIn(object : OneTapSignInCallback {
            override fun onResult(credential: SignInCredential?, error: Throwable?) {
                _signInState.value = SocialAuthResult(data = credential, error = error)
                // TODO: Handle your credentials if needed
                // https://developers.google.com/identity/one-tap/android/get-saved-credentials#4_handle_the_users_response
                // NOTE: about Stop displaying OneTap UI if user cancel multiple times
                // https://developers.google.com/identity/one-tap/android/get-saved-credentials#disable-one-tap
            }
        })
    }
}
