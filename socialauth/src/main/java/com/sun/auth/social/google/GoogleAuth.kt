package com.sun.auth.social.google

import android.app.Activity
import android.app.PendingIntent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.sun.auth.social.BaseSocialAuth
import com.sun.auth.social.SocialAuth.getPlatformConfig
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.*

internal class GoogleAuth(
    activity: FragmentActivity,
    signInCallback: SocialAuthSignInCallback?,
    signOutCallback: SocialAuthSignOutCallback?
) : BaseSocialAuth(activity, signInCallback, signOutCallback), DefaultLifecycleObserver {

    private val signInClient: SignInClient by lazy { Identity.getSignInClient(activity) }
    private var signInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private val config: GoogleConfig by lazy { getPlatformConfig(SocialType.GOOGLE) as GoogleConfig }

    init {
        if (config.enableOneTapSignIn && firebaseAuth.currentUser == null) {
            showOneTapSignIn()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = activity?.activityResultRegistry?.register(
            GOOGLE_SIGN_IN_REQUEST,
            owner,
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            handleSignInResult(result)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.e("x", "xxxx onDestroy is called")
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
                signInCallback?.onResult(user = null, error = AuthApiException(it))
            }
    }

    override fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_CANCELED) {
            signInCallback?.onResult(user = null, error = CancelAuthException())
            return
        }

        val googleCredential = signInClient.getSignInCredentialFromIntent(result.data)
        val idToken = googleCredential.googleIdToken
        if (idToken.isNullOrBlank()) {
            signInCallback?.onResult(user = null, error = NoTokenGeneratedException())
        } else {
            firebaseAuthWithGoogle(idToken)
        }
    }

    override fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getUser(): SocialUser {
        return SocialUser(SocialType.GOOGLE, firebaseAuth.currentUser)
    }

    override fun signOut(clearToken: Boolean) {
        signInClient.signOut().addOnSuccessListener {
            firebaseAuth.signOut()
            signOutCallback?.onResult()
        }.addOnFailureListener {
            signOutCallback?.onResult(AuthApiException(it))
        }

    }

    override fun revoke() {
        // Do nothing
    }

    private fun launchSignIn(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
            signInLauncher?.launch(intentSenderRequest)
        } catch (e: Exception) {
            signInCallback?.onResult(user = null, error = AuthApiException(e))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // Got an ID token from Google. Use it to authenticate with Firebase.
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { data ->
                signInCallback?.onResult(
                    user = SocialUser(type = SocialType.GOOGLE, user = data.user),
                    error = null
                )
            }.addOnFailureListener {
                if (it is FirebaseAuthInvalidCredentialsException) {
                    signInCallback?.onResult(
                        user = null,
                        error = AuthApiException(ModifiedDateTimeException())
                    )
                } else {
                    signInCallback?.onResult(user = null, error = AuthApiException(it))
                }
            }
    }

    private fun showOneTapSignIn() {
        val oneTapSignInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(config.webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        signInClient.beginSignIn(oneTapSignInRequest)
            .addOnSuccessListener {
                launchSignIn(it.pendingIntent)
            }
            .addOnFailureListener {
                signInCallback?.onResult(user = null, error = AuthApiException(it))
            }

    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST = "GOOGLE_SIGN_IN_REQUEST"
    }
}