package com.sun.auth.social

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.google.ModifiedDateTimeException
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser
import com.sun.auth.social.utils.weak

abstract class BaseSocialAuth internal constructor(
    childActivity: FragmentActivity,
    protected val signInCallback: SocialAuthSignInCallback? = null,
    protected val signOutCallback: SocialAuthSignOutCallback? = null
) : DefaultLifecycleObserver {

    protected var activity: FragmentActivity? by weak(null)
    protected val firebaseAuth by lazy { Firebase.auth }

    init {
        activity = childActivity
    }

    abstract fun signIn()
    abstract fun handleSignInResult(result: ActivityResult)
    abstract fun isSignedIn(): Boolean
    abstract fun signOut(clearToken: Boolean)
    abstract fun getUser(): SocialUser?

    internal fun linkWithCurrentAccount(type: SocialType, credential: AuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.addOnSuccessListener { data ->
                signInCallback?.onResult(
                    user = SocialUser(
                        type = type,
                        firebaseUser = data.user,
                        additionalUserInfo = data.additionalUserInfo
                    ),
                    error = null
                )
            }?.addOnFailureListener { error ->
                if (error is FirebaseAuthUserCollisionException) {
                    signInWithFirebase(type, credential)
                } else {
                    signInCallback?.onResult(user = null, error = SocialAuthApiException(error))
                }
            }
    }

    internal fun signInWithFirebase(type: SocialType, credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { data ->
                signInCallback?.onResult(
                    user = SocialUser(
                        type = type,
                        firebaseUser = data.user,
                        additionalUserInfo = data.additionalUserInfo
                    ),
                    error = null
                )
            }.addOnFailureListener {
                if (it is FirebaseAuthInvalidCredentialsException) {
                    signInCallback?.onResult(
                        user = null,
                        error = SocialAuthApiException(ModifiedDateTimeException())
                    )
                } else {
                    signInCallback?.onResult(user = null, error = SocialAuthApiException(it))
                }
            }
    }
}
