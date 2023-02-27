package com.sun.auth.sample.facebook.standard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.AccessToken
import com.facebook.Profile
import com.sun.auth.facebook.standard.FacebookStandardAuth
import com.sun.auth.facebook.standard.SignInCallback
import com.sun.auth.facebook.standard.SignOutCallback
import com.sun.auth.sample.SocialAuthResult

class FacebookAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        FacebookStandardAuth.signIn()
    }

    fun signOut() {
        FacebookStandardAuth.signOut()
    }

    fun isSignedIn(): Boolean {
        return FacebookStandardAuth.isSignedIn()
    }

    fun getProfile(): Profile? {
        return FacebookStandardAuth.getProfile()
    }

    fun initFacebookSignIn(activity: FragmentActivity) {
        FacebookStandardAuth.initialize(
            activity,
            signInCallback = object : SignInCallback {
                override fun onResult(accessToken: AccessToken?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(data = accessToken, error = error)
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
