package com.sun.auth.sample.facebook.firebase

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.PROVIDER_FACEBOOK
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback
import com.sun.auth.facebook.firebase.FacebookFirebaseAuth
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
        FacebookFirebaseAuth.signOut(object : SignOutCallback {
            override fun onResult(error: Throwable?) {
                _signOutState.value = error
            }
        })
    }

    fun isSignedIn(): Boolean {
        return FacebookFirebaseAuth.isSignedIn()
    }

    fun getUser(): FirebaseUser? {
        return FacebookFirebaseAuth.getFirebaseUser()
    }

    fun getLinkedAccounts(): UserInfo? {
        return FacebookFirebaseAuth.getLinkedAccounts(PROVIDER_FACEBOOK)
    }

    fun initFacebookSignIn(activity: FragmentActivity) {
        FacebookFirebaseAuth.initialize(
            activity,
            signInCallback = object : SignInCallback<AuthResult> {
                override fun onResult(data: AuthResult?, error: Throwable?) {
                    _signInState.value = SocialAuthResult(data = data, error = error)
                }
            },
        )
    }
}
