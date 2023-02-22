Before you start using Google Authentication with this helper, you must setup your Firebase Project
follow [this Docs](https://firebase.google.com/docs/android/setup).

## Google Authentication via Firebase

There are 3 simple steps you need to do:

### 1 Setup your GoogleConfig

From `Application` class.
This should be called first, before you start to call any authentication actions.

```kt
initGoogleAuth(
    webClientId = getString(R.string.google_web_client_id),
    signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(getString(R.string.google_web_client_id))
        .requestProfile()
        // ...
        .build(),
) {
    enableOneTapSignIn = true
    enableFilterByAuthorizedAccounts = true
}
```

### 2 Initialize activity with callbacks you want to get result

From your `FragmentActivity` or `Fragment`

```kt
fun initGoogleSignIn(activity: FragmentActivity) {
    GoogleStandardAuth.initialize(
        activity,
        signInCallback = object : SignInCallback {
            override fun onResult(account: GoogleSignInAccount?, error: Throwable?) {
                _signInState.value = SocialAuthResult(data = account, exception = error)
            }
        },
        signOutCallback = object : SignOutCallback {
            override fun onResult(error: Throwable?) {
                _signOutState.value = error
            }
        },
        oneTapSignInCallback = object : OneTapSignInCallback {
            override fun onResult(credential: SignInCredential?, error: Throwable?) {
                // TODO: Handle your credentials if needed
                // https://developers.google.com/identity/one-tap/android/get-saved-credentials#4_handle_the_users_response
                // NOTE: about Stop displaying OneTap UI if user cancel multiple times
                // https://developers.google.com/identity/one-tap/android/get-saved-credentials#disable-one-tap
            }
        },
    )
}
```

### 3 Try authentication actions

```kt
fun signIn() {
    GoogleStandardAuth.signIn()
}

fun logout() {
    GoogleStandardAuth.signOut(revokeAccess = true)
}

fun isLoggedIn(): Boolean {
    return GoogleStandardAuth.isSignedIn()
}

fun getUser(): GoogleSignInAccount? {
    return GoogleStandardAuth.getUser()
}
```
