package com.sun.auth.google.standard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
     * Init the google authentication with callbacks.
     * @param fragment The current [Fragment].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     * @param oneTapSignInCallback The OneTap sign in callback.
     */
    @JvmStatic
    fun initialize(
        fragment: Fragment,
        signInCallback: SignInCallback? = null,
        signOutCallback: SignOutCallback? = null,
        oneTapSignInCallback: OneTapSignInCallback? = null,
    ) {
        check(fragment.activity == null || fragment.activity?.isFinishing == true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        initialize(
            activity = fragment.requireActivity(),
            signInCallback = signInCallback,
            signOutCallback = signOutCallback,
            oneTapSignInCallback = oneTapSignInCallback,
        )
    }

    /**
     * Init the google authentication with callbacks.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     * @param oneTapSignInCallback The OneTap sign in callback.
     */
    @JvmStatic
    fun initialize(
        activity: FragmentActivity,
        signInCallback: SignInCallback? = null,
        signOutCallback: SignOutCallback? = null,
        oneTapSignInCallback: OneTapSignInCallback? = null,
    ) {
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
            signOutCallback,
            oneTapSignInCallback,
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
     *
     * Note: OneTapSignIn not able to use this function.
     */
    fun signOut(revokeAccess: Boolean) {
        authClient?.signOut(revokeAccess)
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
     */
    fun showOneTapSignIn() {
        authClient?.showOneTapSignIn()
    }
}
