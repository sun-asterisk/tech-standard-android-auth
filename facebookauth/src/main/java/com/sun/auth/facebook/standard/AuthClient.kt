package com.sun.auth.facebook.standard

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.facebook.* // ktlint-disable no-wildcard-imports
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.sun.auth.core.CancellationAuthException
import com.sun.auth.core.SocialAuth

internal class AuthClient(
    boundActivity: FragmentActivity,
    private val config: FacebookConfig,
    private val signInCallback: SignInCallback?,
    private val signOutCallback: SignOutCallback?,
) : SocialAuth(boundActivity) {
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val facebookInstance by lazy { LoginManager.getInstance() }
    private val facebookCallback by lazy {
        object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val profileTracker: ProfileTracker = object : ProfileTracker() {
                    override fun onCurrentProfileChanged(
                        oldProfile: Profile?,
                        currentProfile: Profile?,
                    ) {
                        Profile.setCurrentProfile(currentProfile)
                        signInCallback?.onResult(accessToken = result.accessToken)
                        stopTracking()
                    }
                }
                profileTracker.startTracking()
            }

            override fun onCancel() {
                signInCallback?.onResult(error = CancellationAuthException())
            }

            override fun onError(error: FacebookException) {
                signInCallback?.onResult(error = error)
            }
        }
    }
    private val facebookLoginStatusCallback by lazy {
        object : LoginStatusCallback {
            override fun onCompleted(accessToken: AccessToken) {
                signInCallback?.onResult(accessToken = accessToken)
            }

            override fun onFailure() {
                // Do nothing
            }

            override fun onError(exception: Exception) {
                // Do nothing
            }
        }
    }

    init {
        verifyActivity()
        if (!config.useFacebookLoginButton) {
            facebookInstance.registerCallback(callbackManager, facebookCallback)
        }
        if (config.enableLoginStatus) {
            facebookInstance.retrieveLoginStatus(activity!!, facebookLoginStatusCallback)
        }
    }

    fun setLoginButton(button: LoginButton) {
        check(config.useFacebookLoginButton) {
            "Must enable config useFacebookLoginButton first!"
        }
        check(button.permissions.isNotEmpty() || config.readPermissions.isNotEmpty()) {
            "Must set config permissions or LoginButton permissions"
        }
        if (button.permissions.isEmpty()) {
            button.permissions = config.readPermissions
        }
        button.registerCallback(callbackManager, facebookCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        facebookInstance.unregisterCallback(callbackManager)
    }

    override fun signIn() {
        facebookInstance.logInWithReadPermissions(
            activity!!,
            callbackManager,
            config.readPermissions,
        )
    }

    override fun handleSignInResult(result: ActivityResult) {
        // Do nothing
    }

    override fun isSignedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    override fun signOut(revokeAccess: Boolean) {
        try {
            facebookInstance.logOut()
            signOutCallback?.onResult()
        } catch (e: Exception) {
            signOutCallback?.onResult(e)
        }
    }

    fun getProfile(): Profile? {
        return Profile.getCurrentProfile()
    }

    fun getAccessToken(): AccessToken? {
        return AccessToken.getCurrentAccessToken()
    }
}
