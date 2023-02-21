Before you start using Facebook Authentication with this helper, you must setup your Firebase
Project follow [this Docs](https://firebase.google.com/docs/android/setup).

## Facebook Authentication via Firebase

There are 4 simple steps you need to do:

### 1 Create Facebook project and link to Firebase

- Go to [Facebook developer site](https://developers.facebook.com/) and create your application.
    - Save your App Id.
    - Go to **Setting > Basic** > save your **App secret**.
    - Go to **Setting > Advanced** > save your **Client token**.
- Go to **Dashboard > Add products to your app**, select **Facebook Login**.
- Go to **Firebase project > Authentication** > Enable Facebook in **Sign-in method**.
    - Add App Id.
    - Add App secret.
    - Copy **OAuth redirect URI** and go to Facebook app
    - Paste URI to **Facebook Login > Settings > Valid OAuth Redirect URIs**.
- Go to **Facebook Login > QuickStart** > Add settings for your Android application.

Notes: For additional settings please
read [Facebook documents](https://developers.facebook.com/docs/) for detail. Depending on the
[Facebook data you request from people using Facebook Login](https://developers.facebook.com/docs/permissions/reference#login_permissions)
, you may need to submit your app for review prior to launch.

### 2 Setup your FacebookConfig

From `Application` class.
This should be called first, before you start to call any authentication actions.

```kt
initFacebookAuth(
    appId = getString(R.string.facebook_app_id),
    clientToken = getString(R.string.facebook_client_token),
) {
    readPermissions = listOf("email", "public_profile")
    enableAppEvent = false
    useFacebookLoginButton = true
}
```

#### 2.3 Add Facebook activity to your Manifest

```xml
<activity android:name="com.facebook.FacebookActivity"
    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation" />

<activity android:name="com.facebook.CustomTabActivity"
    android:exported="true">    <!--To support Web login-->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
    
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    
        <data android:scheme="@string/fb_login_protocol_scheme" /> <!--fbAPP-ID, Ex: fb111000111000111-->
    </intent-filter>
</activity>
```

### 3 Initialize activity with callbacks you want to get result

From your `FragmentActivity` or `Fragment`

```kt
fun initFacebookSignIn(activity: FragmentActivity) {
    FacebookStandardAuth.initialize(
        activity,
        signInCallback = object : SignInCallback {
            override fun onResult(accessToken: AccessToken?, error: Throwable?) {
                _signInState.value = SocialAuthResult(data = accessToken, exception = error)
            }
        },
        signOutCallback = object : SignOutCallback {
            override fun onResult(error: Throwable?) {
                _signOutState.value = error
            }
        },
    )
}
```

### 4 Try authentication actions

```kt
fun signIn() {
    FacebookStandardAuth.signIn()
}

// For using Facebook LoginButton
fun setupViews() {
    FacebookStandardAuth.setLoginButton(binding.facebookSignIn)
    // other views setup
}

fun logout() {
    FacebookStandardAuth.signOut()
}

fun isLoggedIn(): Boolean {
    return FacebookStandardAuth.isSignedIn()
}



fun getFacebookToken(): AccessToken? {
    return AccessToken.getCurrentAccessToken()
}

fun getProfile(): Profile? {
  return FacebookStandardAuth.getProfile()
}
// or
fun getFacebookProfile(): Profile? {
    return Profile.getCurrentProfile()
}
```
