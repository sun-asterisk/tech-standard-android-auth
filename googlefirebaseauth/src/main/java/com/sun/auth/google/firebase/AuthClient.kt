package com.sun.auth.google.firebase

import android.app.Activity
import android.app.PendingIntent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.auth.core.*
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback

class AuthClient(
    boundActivity: FragmentActivity,
    private val config: GoogleConfig,
    private val signInCallback: SignInCallback<AuthResult>?,
) : SocialAuth(boundActivity) {
    private val signInClient: SignInClient by lazy {
        verifyActivity()
        Identity.getSignInClient(activity!!)
    }
    private var signInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private val firebaseAuth by lazy { Firebase.auth }

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = activity?.activityResultRegistry?.register(
            REQUEST_GOOGLE_SIGN_IN,
            owner,
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            handleSignInResult(result)
        }
    }

    override fun signIn() {
        check(config.webClientId.isNotBlank()) {
            "You must provide web_client_id first!"
        }
        val signInRequest = GetSignInIntentRequest.builder()
            .setServerClientId(config.webClientId)
            .build()

        signInClient.getSignInIntent(signInRequest)
            .addOnCompleteListener {
                launchSignIn(it.result)
            }
            .addOnFailureListener {
                signInCallback?.onResult(error = SocialAuthException(it))
            }
    }

    override fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_CANCELED) {
            signInCallback?.onResult(error = CancellationAuthException())
            return
        }
        try {
            val googleCredential = signInClient.getSignInCredentialFromIntent(result.data)
            val idToken = googleCredential.googleIdToken
            if (idToken.isNullOrBlank()) {
                signInCallback?.onResult(error = UnexpectedAuthException())
            } else {
                firebaseAuthWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            if (e.statusCode == Activity.RESULT_CANCELED) {
                signInCallback?.onResult(error = CancellationAuthException())
            } else {
                signInCallback?.onResult(error = SocialAuthException(e))
            }
        }
    }

    override fun isSignedIn(): Boolean {
        return getUser() != null
    }

    fun getUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut(revokeAccess: Boolean, signOutCallback: SignOutCallback?) {
        signInClient.signOut().addOnSuccessListener {
            firebaseAuth.signOut()
            signOutCallback?.onResult()
        }.addOnFailureListener {
            signOutCallback?.onResult(error = SocialAuthException(it))
        }
    }

    private fun launchSignIn(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
            signInLauncher?.launch(intentSenderRequest)
        } catch (e: Exception) {
            signInCallback?.onResult(error = SocialAuthException(e))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // Got an ID token from Google. Use it to authenticate or link with other account from Firebase.
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        if (!isSignedIn() || !config.enableLinkAccounts) {
            signInWithFirebase(credential)
        } else {
            linkWithCurrentAccount(credential)
        }
    }

    private fun linkWithCurrentAccount(credential: AuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.addOnSuccessListener { data ->
                signInCallback?.onResult(data = data)
            }?.addOnFailureListener { error ->
                if (error is FirebaseAuthUserCollisionException) {
                    signInWithFirebase(credential)
                } else {
                    signInCallback?.onResult(error = SocialAuthException(error))
                }
            }
    }

    private fun signInWithFirebase(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { data ->
                signInCallback?.onResult(data = data)
            }.addOnFailureListener {
                if (it is FirebaseAuthInvalidCredentialsException) {
                    signInCallback?.onResult(error = InvalidCredentialsException(it))
                } else {
                    signInCallback?.onResult(error = SocialAuthException(it))
                }
            }
    }

    fun showOneTapSignIn() {
        check(config.enableOneTapSignIn) {
            "You must enable OneTap SignIn from GoogleConfig"
        }
        val oneTapSignInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(config.webClientId)
                    .setFilterByAuthorizedAccounts(config.enableFilterByAuthorizedAccounts)
                    .build(),
            ).build()

        signInClient.beginSignIn(oneTapSignInRequest)
            .addOnSuccessListener {
                launchSignIn(it.pendingIntent)
            }
            .addOnFailureListener {
                signInCallback?.onResult(error = SocialAuthException(it))
            }
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

    companion object {
        private const val REQUEST_GOOGLE_SIGN_IN = "REQUEST_GOOGLE_SIGN_IN"
    }
}
