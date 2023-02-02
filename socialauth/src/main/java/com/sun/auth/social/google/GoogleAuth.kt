package com.sun.auth.social.google

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
import com.google.firebase.auth.GoogleAuthProvider
import com.sun.auth.social.BaseSocialAuth
import com.sun.auth.social.NoTokenGeneratedException
import com.sun.auth.social.SocialAuth.getSocialConfig
import com.sun.auth.social.SocialAuthApiException
import com.sun.auth.social.SocialCancelAuthException
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.PROVIDER_GOOGLE
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser

internal class GoogleAuth(
    activity: FragmentActivity,
    signInCallback: SocialAuthSignInCallback?,
    signOutCallback: SocialAuthSignOutCallback?,
) : BaseSocialAuth(activity, signInCallback, signOutCallback) {

    private val signInClient: SignInClient by lazy { Identity.getSignInClient(activity) }
    private var signInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private val config: GoogleConfig by lazy { getSocialConfig(SocialType.GOOGLE) as GoogleConfig }

    override fun onCreate(owner: LifecycleOwner) {
        if (config.enableOneTapSignIn && firebaseAuth.currentUser == null) {
            showOneTapSignIn()
        }
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
                signInCallback?.onResult(user = null, error = SocialAuthApiException(it))
            }
    }

    override fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_CANCELED) {
            signInCallback?.onResult(user = null, error = SocialCancelAuthException())
            return
        }
        try {
            val googleCredential = signInClient.getSignInCredentialFromIntent(result.data)
            val idToken = googleCredential.googleIdToken
            if (idToken.isNullOrBlank()) {
                signInCallback?.onResult(user = null, error = NoTokenGeneratedException())
            } else {
                firebaseAuthWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            if (e.statusCode == Activity.RESULT_CANCELED) {
                signInCallback?.onResult(user = null, error = SocialCancelAuthException())
            } else {
                signInCallback?.onResult(user = null, error = SocialAuthApiException(e))
            }
        }
    }

    override fun isSignedIn(): Boolean {
        if (!config.enableLinkAccounts) {
            // different firebase users
            return getUser() != null
        }
        return firebaseAuth.currentUser != null // same firebase user
    }

    override fun getUser(): SocialUser? {
        val users = firebaseAuth.currentUser?.providerData
        if (users.isNullOrEmpty()) return null
        for (user in users) {
            if (user.providerId.lowercase() == PROVIDER_GOOGLE) {
                return SocialUser(type = SocialType.GOOGLE, firebaseUser = firebaseAuth.currentUser)
            }
        }
        return null
    }

    override fun signOut(clearToken: Boolean) {
        signInClient.signOut().addOnSuccessListener {
            firebaseAuth.signOut()
            signOutCallback?.onResult()
        }.addOnFailureListener {
            signOutCallback?.onResult(SocialAuthApiException(it))
        }
    }

    private fun launchSignIn(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
            signInLauncher?.launch(intentSenderRequest)
        } catch (e: Exception) {
            signInCallback?.onResult(user = null, error = SocialAuthApiException(e))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // Got an ID token from Google. Use it to authenticate or link with other account from Firebase.
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        if (!isSignedIn() || !config.enableLinkAccounts) {
            signInWithFirebase(SocialType.GOOGLE, credential)
        } else {
            linkWithCurrentAccount(SocialType.GOOGLE, credential)
        }
    }

    private fun showOneTapSignIn() {
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
                signInCallback?.onResult(user = null, error = SocialAuthApiException(it))
            }
    }

    companion object {
        private const val REQUEST_GOOGLE_SIGN_IN = "REQUEST_GOOGLE_SIGN_IN"
    }
}
