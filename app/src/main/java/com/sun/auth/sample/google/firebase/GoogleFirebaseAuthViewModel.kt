package com.sun.auth.sample.google.firebase

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.PROVIDER_GOOGLE
import com.sun.auth.google.firebase.GoogleFirebaseAuth
import com.sun.auth.google.firebase.SignInCallback
import com.sun.auth.google.firebase.SignOutCallback
import com.sun.auth.sample.SocialAuthResult

class GoogleFirebaseAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        GoogleFirebaseAuth.signIn()
    }

    fun signOut() {
        GoogleFirebaseAuth.signOut()
    }

    fun isSignedIn(): Boolean {
        return GoogleFirebaseAuth.isSignedIn()
    }

    fun getUser(): FirebaseUser? {
        return GoogleFirebaseAuth.getUser()
    }

    fun getUserInfo(): UserInfo? {
        return GoogleFirebaseAuth.getLinkedAccounts(PROVIDER_GOOGLE)
    }

    fun initGoogleSignIn(activity: FragmentActivity) {
        GoogleFirebaseAuth.initialize(
            activity,
            signInCallback = object : SignInCallback {
                override fun onResult(authResult: AuthResult?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(data = authResult, exception = error)
                }
            },
            signOutCallback = object : SignOutCallback {
                override fun onResult(error: Throwable?) {
                    _signOutState.value = error
                }
            },
        )
    }
}
