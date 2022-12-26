package com.sun.auth.social.facebook

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.sun.auth.social.*
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser

internal class FacebookAuth(
    activity: FragmentActivity,
    signInCallback: SocialAuthSignInCallback?,
    signOutCallback: SocialAuthSignOutCallback?
) : BaseSocialAuth(activity, signInCallback, signOutCallback), DefaultLifecycleObserver {
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val facebookInstance by lazy { LoginManager.getInstance() }
    private val config by lazy { SocialAuth.getSocialConfig(SocialType.FACEBOOK) as FacebookConfig }

    init {
        facebookInstance.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                firebaseAuthWithToken(result.accessToken)
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
                    firebaseAuthWithToken(accessToken)
                }

                override fun onFailure() {
                    signInCallback?.onResult(user = null, error = SocialAuthApiException(null))
                }

                override fun onError(exception: Exception) {
                    signInCallback?.onResult(user = null, error = exception)
                }
            })
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
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
        val token = AccessToken.getCurrentAccessToken()
        return token != null && !token.isExpired
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

    override fun getUser(): SocialUser? {
        return SocialUser(type = SocialType.FACEBOOK, user = firebaseAuth.currentUser)
    }

    private fun firebaseAuthWithToken(accessToken: AccessToken?) {
        if (accessToken == null) {
            signInCallback?.onResult(user = null, error = NoTokenGeneratedException())
            return
        }
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { data ->
                signInCallback?.onResult(
                    user = SocialUser(type = SocialType.FACEBOOK, user = data.user),
                    error = null
                )
            }
            .addOnFailureListener {
                signInCallback?.onResult(user = null, error = it)
            }
    }
}
