package com.sun.auth.sample.facebook.firebase

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.Profile
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.PROVIDER_FACEBOOK
import com.sun.auth.facebook.firebase.FacebookFirebaseAuth
import com.sun.auth.facebook.firebase.SignInCallback
import com.sun.auth.facebook.firebase.SignOutCallback
import com.sun.auth.sample.SocialAuthResult

class FacebookFirebaseAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        FacebookFirebaseAuth.signIn()
    }

    fun signOut() {
        FacebookFirebaseAuth.signOut()
    }

    fun isSignedIn(): Boolean {
        return FacebookFirebaseAuth.isSignedIn()
    }

    fun getUser(): Profile? {
        return FacebookFirebaseAuth.getProfile()
    }

    fun getLinkedAccounts(): UserInfo? {
        return FacebookFirebaseAuth.getLinkedAccounts(PROVIDER_FACEBOOK)
    }

    fun initFacebookSignIn(activity: FragmentActivity) {
        FacebookFirebaseAuth.initialize(
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
