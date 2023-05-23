package com.sun.auth.google.standard

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.sun.auth.core.CancellationAuthException
import com.sun.auth.core.SocialAuth
import com.sun.auth.core.SocialAuthException
import com.sun.auth.core.UnexpectedAuthException
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback

internal class AuthClient(
    boundActivity: FragmentActivity,
    private val config: GoogleConfig,
    private val signInCallback: SignInCallback<GoogleSignInAccount>?,
) : SocialAuth(boundActivity) {
    private var oneTapSignInCallback: OneTapSignInCallback? = null
    private val signInClient: GoogleSignInClient by lazy {
        verifyActivity()
        GoogleSignIn.getClient(activity!!, config.signInOptions)
    }
    private val oneTapClient: SignInClient by lazy {
        verifyActivity()
        Identity.getSignInClient(activity!!)
    }
    private var signInLauncher: ActivityResultLauncher<Intent>? = null
    private var oneTapSignInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = activity?.activityResultRegistry?.register(
            REQUEST_GOOGLE_SIGN_IN,
            owner,
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            handleSignInResult(result)
        }
        if (config.enableOneTapSignIn) {
            oneTapSignInLauncher = activity?.activityResultRegistry?.register(
                REQUEST_GOOGLE_SIGN_IN_ONE_TAP,
                owner,
                ActivityResultContracts.StartIntentSenderForResult(),
            ) { result ->
                handleOneTapSignInResult(result)
            }
        }
    }

    fun showOneTapSignIn(callback: OneTapSignInCallback) {
        check(config.enableOneTapSignIn) {
            "You must enable OneTap SignIn from GoogleConfig"
        }
        oneTapSignInCallback = callback
        val oneTapSignInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(config.webClientId)
                .setFilterByAuthorizedAccounts(config.enableFilterByAuthorizedAccounts)
                .build(),
        ).build()

        oneTapClient.beginSignIn(oneTapSignInRequest)
            .addOnSuccessListener {
                launchOneTapSignIn(it.pendingIntent)
            }.addOnFailureListener {
                oneTapSignInCallback?.onResult(error = SocialAuthException(it))
            }
    }

    override fun signIn() {
        check(config.webClientId.isNotBlank()) {
            "You must provide correct web_client_id first!"
        }
        try {
            signInLauncher?.launch(signInClient.signInIntent)
        } catch (e: Exception) {
            signInCallback?.onResult(error = SocialAuthException(e))
        }
    }

    override fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_CANCELED) {
            signInCallback?.onResult(error = CancellationAuthException())
            return
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if (account == null) {
                signInCallback?.onResult(error = UnexpectedAuthException())
            } else {
                signInCallback?.onResult(data = account)
            }
        } catch (e: ApiException) {
            if (e.status.isCanceled) {
                signInCallback?.onResult(error = CancellationAuthException())
            } else {
                signInCallback?.onResult(error = SocialAuthException(e))
            }
        }
    }

    override fun isSignedIn(): Boolean {
        return getUser() != null
    }

    fun getUser(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity!!)
    }

    override fun signOut(revokeAccess: Boolean, signOutCallback: SignOutCallback?) {
        signInClient.signOut().addOnSuccessListener {
            if (revokeAccess) {
                signInClient.revokeAccess().addOnCompleteListener {
                    signOutCallback?.onResult()
                }
            } else {
                signOutCallback?.onResult()
            }
        }.addOnFailureListener {
            signOutCallback?.onResult(error = SocialAuthException(it))
        }
    }

    private fun launchOneTapSignIn(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
            oneTapSignInLauncher?.launch(intentSenderRequest)
        } catch (e: Exception) {
            oneTapSignInCallback?.onResult(error = SocialAuthException(e))
        }
    }

    private fun handleOneTapSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_CANCELED) {
            oneTapSignInCallback?.onResult(error = CancellationAuthException())
            return
        }
        try {
            val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
            oneTapSignInCallback?.onResult(credential = googleCredential)
        } catch (e: ApiException) {
            if (e.statusCode == Activity.RESULT_CANCELED) {
                oneTapSignInCallback?.onResult(error = CancellationAuthException())
            } else {
                oneTapSignInCallback?.onResult(error = SocialAuthException(e))
            }
        }
    }

    companion object {
        private const val REQUEST_GOOGLE_SIGN_IN = "REQUEST_GOOGLE_SIGN_IN"
        private const val REQUEST_GOOGLE_SIGN_IN_ONE_TAP = "REQUEST_GOOGLE_SIGN_IN_ONE_TAP"
    }
}
