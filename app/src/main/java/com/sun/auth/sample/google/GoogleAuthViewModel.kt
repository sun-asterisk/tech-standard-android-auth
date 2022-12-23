package com.sun.auth.sample.google

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sun.auth.social.SocialAuth
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser

class GoogleAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<GoogleAuthResult>()
    val signInState: LiveData<GoogleAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        SocialAuth.signIn(SocialType.GOOGLE)
    }

    fun logout() {
        SocialAuth.signOut(SocialType.GOOGLE)
    }

    fun isLoggedIn(): Boolean {
        return SocialAuth.isSignedIn(SocialType.GOOGLE)
    }

    fun getUser(): SocialUser? {
        return SocialAuth.getUser(SocialType.GOOGLE)
    }

    fun initGoogleSignIn(activity: FragmentActivity) {
        SocialAuth.initialize(
            activity = activity,
            signInCallback = object : SocialAuthSignInCallback {
                override fun onResult(user: SocialUser?, error: Throwable?) {
                    _signInState.value = GoogleAuthResult(user, error)
                }
            },
            signOutCallback = object : SocialAuthSignOutCallback {
                override fun onResult(error: Throwable?) {
                    _signOutState.value = error
                }
            })
    }
}