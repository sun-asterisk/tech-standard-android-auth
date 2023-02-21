package com.sun.auth.facebook.firebase

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.facebook.* // ktlint-disable no-wildcard-imports
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.* // ktlint-disable no-wildcard-imports
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.auth.core.* // ktlint-disable no-wildcard-imports

internal class AuthClient(
    boundActivity: FragmentActivity,
    private val config: FacebookConfig,
    private val signInCallback: SignInCallback?,
    private val signOutCallback: SignOutCallback?,
) : SocialAuth(boundActivity) {
    private val firebaseAuth by lazy { Firebase.auth }
    private val callbackManager by lazy { CallbackManager.Factory.create() }
    private val facebookInstance by lazy { LoginManager.getInstance() }
    private val facebookCallback by lazy {
        object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                firebaseAuthWithFacebook(result.accessToken)
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
                firebaseAuthWithFacebook(accessToken)
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
        check(button.permissions.isEmpty() || config.readPermissions.isEmpty()) {
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
        return getUser() != null
    }

    override fun signOut(revokeAccess: Boolean) {
        try {
            facebookInstance.logOut()
            firebaseAuth.signOut()
            signOutCallback?.onResult()
        } catch (e: Exception) {
            signOutCallback?.onResult(e)
        }
    }

    fun getProfile(): Profile? {
        return Profile.getCurrentProfile()
    }

    fun getUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getLinkedAccounts(provider: String): UserInfo? {
        val users = firebaseAuth.currentUser?.providerData
        if (users.isNullOrEmpty()) return null
        for (user in users) {
            if (user.providerId.lowercase() == provider) {
                return user
            }
        }
        return null
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken?) {
        if (accessToken == null) {
            signInCallback?.onResult(error = UnexpectedAuthException())
            return
        }
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        if (!isSignedIn() || !config.enableLinkAccounts) {
            signInWithFirebase(credential)
        } else {
            linkWithCurrentAccount(credential)
        }
    }

    private fun signInWithFirebase(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { data ->
                signInCallback?.onResult(authResult = data)
            }.addOnFailureListener {
                if (it is FirebaseAuthInvalidCredentialsException) {
                    signInCallback?.onResult(error = InvalidCredentialsException(it))
                } else {
                    signInCallback?.onResult(error = it)
                }
            }
    }

    private fun linkWithCurrentAccount(credential: AuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)?.addOnSuccessListener { data ->
            signInCallback?.onResult(authResult = data)
        }?.addOnFailureListener { error ->
            if (error is FirebaseAuthUserCollisionException) {
                signInWithFirebase(credential)
            } else {
                signInCallback?.onResult(error = error)
            }
        }
    }
}
