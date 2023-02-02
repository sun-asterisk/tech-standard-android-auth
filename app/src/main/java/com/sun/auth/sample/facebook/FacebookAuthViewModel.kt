package com.sun.auth.sample.facebook

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sun.auth.sample.SocialAuthResult
import com.sun.auth.social.SocialAuth
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser

class FacebookAuthViewModel : ViewModel() {
    private val _signInState = MutableLiveData<SocialAuthResult>()
    val signInState: LiveData<SocialAuthResult> = _signInState

    private val _signOutState = MutableLiveData<Throwable?>()
    val signOutState: LiveData<Throwable?> = _signOutState

    fun signIn() {
        SocialAuth.signIn(SocialType.FACEBOOK)
    }

    fun logout() {
        SocialAuth.signOut(SocialType.FACEBOOK)
    }

    fun isLoggedIn(): Boolean {
        return SocialAuth.isSignedIn(SocialType.FACEBOOK)
    }

    fun getUser(): SocialUser? {
        return SocialAuth.getUser(SocialType.FACEBOOK)
    }

    fun initFacebookSignIn(activity: FragmentActivity) {
        SocialAuth.initialize(
            types = arrayOf(SocialType.FACEBOOK),
            activity = activity,
            signInCallback = object : SocialAuthSignInCallback {
                override fun onResult(user: SocialUser?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(user, error)
                }
            },
            signOutCallback = object : SocialAuthSignOutCallback {
                override fun onResult(error: Throwable?) {
                    _signOutState.value = error
                }
            },
        )
    }
}
