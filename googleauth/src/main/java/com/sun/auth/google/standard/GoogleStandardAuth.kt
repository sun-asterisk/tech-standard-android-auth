package com.sun.auth.google.standard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback
import com.sun.auth.core.weak

@Suppress("Unused")
object GoogleStandardAuth {
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
    fun initialize(
        fragment: Fragment,
        signInCallback: SignInCallback<GoogleSignInAccount>? = null,
    ) {
        check(fragment.activity != null || fragment.activity?.isFinishing != true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        checkNotNull(config) {
            "You must call initGoogleAuth first!"
        }
        authClient = AuthClient(
            boundActivity = fragment.requireActivity(),
            config = config!!,
            signInCallback = signInCallback,
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
    fun initialize(
        activity: FragmentActivity,
        signInCallback: SignInCallback<GoogleSignInAccount>? = null,
    ) {
        check(!activity.isFinishing) {
            "The FragmentActivity is currently unavailable!"
        }
        checkNotNull(config) {
            "You must call initGoogleAuth first!"
        }
        authClient = AuthClient(
            boundActivity = activity,
            config = config!!,
            signInCallback = signInCallback,
        ).apply {
            activity.lifecycle.addObserver(this)
        }
    }

    /**
     * Start SignIn process.
     *
     * Note: OneTapSignIn not able to use this function.
     */
    fun signIn() {
        authClient?.signIn()
    }

    /**
     * Check current SignIn status.
     * @return true if google account is signed in, otherwise false
     *
     * Note: OneTapSignIn always return false.
     */
    fun isSignedIn(): Boolean {
        return authClient?.isSignedIn() ?: false
    }

    /**
     * Sign out the current account.
     * @param revokeAccess if true, disconnect Google account from your app.
     * @param signOutCallback The sign out callback.
     * Note: OneTapSignIn not able to use this function.
     */
    fun signOut(revokeAccess: Boolean, signOutCallback: SignOutCallback? = null) {
        authClient?.signOut(revokeAccess, signOutCallback)
    }

    /**
     * Gets the current signed in account.
     * @return the current signed in account or null.
     *
     * Note: OneTapSignIn is not able to use this function.
     */
    fun getUser(): GoogleSignInAccount? {
        return authClient?.getUser()
    }

    /**
     * Show OneTap SignIn UI.
     *
     * In some cases, this UI is disabled when user cancelled several times.
     *  @param callback  The [OneTapSignInCallback].
     */
    fun showOneTapSignIn(callback: OneTapSignInCallback) {
        authClient?.showOneTapSignIn(callback)
    }
}
