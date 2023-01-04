package com.sun.auth.social.facebook

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.sun.auth.social.*
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.PROVIDER_FACEBOOK
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser

internal class FacebookAuth(
    activity: FragmentActivity,
    signInCallback: SocialAuthSignInCallback?,
    signOutCallback: SocialAuthSignOutCallback?
) : BaseSocialAuth(activity, signInCallback, signOutCallback) {
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val facebookInstance by lazy { LoginManager.getInstance() }
    private val config by lazy { SocialAuth.getSocialConfig(SocialType.FACEBOOK) as FacebookConfig }

    init {
        facebookInstance.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                firebaseAuthWithFacebook(result.accessToken)
            }

            override fun onCancel() {
                signInCallback?.onResult(user = null, error = SocialCancelAuthException())
            }

            override fun onError(error: FacebookException) {
                signInCallback?.onResult(user = null, error = error)
            }
        })
        if (config.autoSignIn) {
            facebookInstance.retrieveLoginStatus(activity, object : LoginStatusCallback {
                override fun onCompleted(accessToken: AccessToken) {
                    firebaseAuthWithFacebook(accessToken)
                }

                override fun onFailure() {
                    // Do nothing
                }

                override fun onError(exception: Exception) {
                    // Do nothing
                }
            })
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        facebookInstance.unregisterCallback(callbackManager)
    }

    override fun signIn() {
        facebookInstance.logInWithReadPermissions(
            activity!!,
            callbackManager,
            config.readPermissions
        )
    }

    override fun handleSignInResult(result: ActivityResult) {
        // Do nothing
    }

    override fun isSignedIn(): Boolean {
        if (!config.enableLinkAccounts) {
            return getUser() != null
        }
        return firebaseAuth.currentUser != null
    }

    override fun getUser(): SocialUser? {
        val users = firebaseAuth.currentUser?.providerData
        if (users.isNullOrEmpty()) return null
        for (user in users) {
            if (user.providerId.lowercase() == PROVIDER_FACEBOOK) {
                return SocialUser(type = SocialType.FACEBOOK, firebaseAuth.currentUser)
            }
        }
        return null
    }

    override fun signOut(clearToken: Boolean) {
        try {
            facebookInstance.logOut()
            firebaseAuth.signOut()
            signOutCallback?.onResult()
        } catch (e: Exception) {
            signOutCallback?.onResult(e)
        }
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken?) {
        if (accessToken == null) {
            signInCallback?.onResult(user = null, error = NoTokenGeneratedException())
            return
        }
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        if (!isSignedIn() || !config.enableLinkAccounts) {
            signInWithFirebase(SocialType.FACEBOOK, credential)
        } else {
            linkWithCurrentAccount(SocialType.FACEBOOK, credential)
        }
    }
}
