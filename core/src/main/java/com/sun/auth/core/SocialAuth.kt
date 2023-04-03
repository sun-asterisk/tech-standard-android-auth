package com.sun.auth.core

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import com.sun.auth.core.callback.SignOutCallback

abstract class SocialAuth(boundActivity: FragmentActivity) : DefaultLifecycleObserver {
    protected var activity: FragmentActivity? by weak(null)
    init {
        activity = boundActivity
    }
    abstract fun signIn()
    abstract fun handleSignInResult(result: ActivityResult)
    abstract fun isSignedIn(): Boolean
    abstract fun signOut(revokeAccess: Boolean, signOutCallback: SignOutCallback?)
    protected fun verifyActivity() {
        check(activity != null || activity?.isFinishing != true) {
            "The FragmentActivity is currently unavailable!"
        }
    }
}
