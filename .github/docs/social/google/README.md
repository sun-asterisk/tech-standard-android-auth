Before you start using Google Authentication with this helper, you must setup your Firebase Project
follow [this Docs](https://firebase.google.com/docs/android/setup).

## Google Authentication via Firebase

There are 3 simple steps you need to do:

### 1 Setup your GoogleConfig

From anywhere you have your context, but recommended is `Application`, `Activity` or `Fragment`.
This should be called first, before you start to call any authentication actions.

#### 1.1 Call inside multi socials config setup

```kt
initSocialAuth {
    google(getString(R.string.google_web_client_id)) {
        enableOneTapSignIn = true
        enableFilterByAuthorizedAccounts = true
    }
    facebook(getString(R.string.facebook_application_id)) {
        ...
    }
}
```

#### 1.2 Call it separatedly (only use Google Auth)

```kt
initGoogleAuth(getString(R.string.google_web_client_id)) {
    enableOneTapSignIn = true
    enableFilterByAuthorizedAccounts = true
}
```

### 2 Initialize activity with callbacks you want to get result

From your `FragmentActivity` or `Fragment`

```kt
fun initGoogleSignIn(activity: FragmentActivity) {
    SocialAuth.initialize(
        activity = activity,
        signInCallback = object : SocialAuthSignInCallback {
            override fun onResult(user: SocialUser?, error: Throwable?) {
                // Gets the sign in result
            }
        },
        signOutCallback = object : SocialAuthSignOutCallback {
            override fun onResult(error: Throwable?) {
                // Gets the sign out result
            }
        })
}
```

### 3 Try authentication actions

```kt
fun signIn() {
    SocialAuth.signIn(SocialType.GOOGLE)
}

fun logout() {
    SocialAuth.signOut(SocialType.GOOGLE)
}

fun isLoggedIn(): Boolean {
    return SocialAuth.isSignedIn(SocialType.GOOGLE)
}

fun getUser(): SocialUser? {
    return SocialAuth.getUser(SocialType.GOOGLE)
}
```
