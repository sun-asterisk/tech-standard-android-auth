package com.sun.auth.google.firebase

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.*
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback

@Suppress("Unused")
object GoogleFirebaseAuth {
    private var context: Context? by weak(null)
    private var config: GoogleConfig? = null
    private var authClient: AuthClient? = null

    /**
     * Init the context and google config.
     * @param context The current context.
     * @param config The GoogleConfig settings.
     */
    @JvmStatic
    internal fun initialize(context: Context, config: GoogleConfig) {
        this.context = context.applicationContext
        this.config = config
    }

    /**
     * Init the google authentication with callbacks. Call it before Fragment STARTED state.
     * @param fragment The current [Fragment].
     * @param signInCallback The sign in callback.
     */
    @JvmStatic
    fun initialize(fragment: Fragment, signInCallback: SignInCallback<AuthResult>? = null) {
        check(fragment.activity != null || fragment.activity?.isFinishing != true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        check(config != null) {
            "You must call initGoogleAuth first!"
        }
        authClient = AuthClient(
            fragment.requireActivity(),
            config!!,
            signInCallback,
        ).apply {
            fragment.lifecycle.addObserver(this)
        }
    }

    /**
     * Init the google authentication with callbacks. Call it before Activity STARTED state.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     */
    @JvmStatic
    fun initialize(activity: FragmentActivity, signInCallback: SignInCallback<AuthResult>? = null) {
        check(!activity.isFinishing) {
            "The FragmentActivity is currently unavailable!"
        }
        check(config != null) {
            "You must call initGoogleAuth first!"
        }
        authClient = AuthClient(
            activity,
            config!!,
            signInCallback,
        ).apply {
            activity.lifecycle.addObserver(this)
        }
    }

    /**
     * Start SignIn process.
     */
    fun signIn() {
        authClient?.signIn()
    }

    /**
     * Check current SignIn status.
     * @return true if google account is signed in, otherwise false
     */
    fun isSignedIn(): Boolean {
        return authClient?.isSignedIn() ?: false
    }

    /**
     * Sign out the current account.
     * @param signOutCallback The sign out callback.
     */
    fun signOut(signOutCallback: SignOutCallback? = null) {
        authClient?.signOut(false, signOutCallback)
    }

    /**
     * Gets the current signed in account.
     * @return the current signed in account or null.
     */
    fun getUser(): FirebaseUser? {
        return authClient?.getUser()
    }

    /**
     * Gets the current Linked UserInfo in firebase.
     * @param provider The [PROVIDER_GOOGLE] or [PROVIDER_FACEBOOK]
     * @return Firebase UserInfo or null.
     */
    fun getLinkedAccounts(provider: String): UserInfo? {
        return authClient?.getLinkedAccounts(provider)
    }

    /**
     * Show OneTap SignIn UI.
     *
     * In some cases, this UI is disabled when user cancelled several times.
     */
    fun showOneTapSignIn() {
        authClient?.showOneTapSignIn()
    }
}
