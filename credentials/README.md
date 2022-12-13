# Credentials

This is small helper module to help you quickly do some authentication actions like:

- Login with username & password.
- Refresh token manually or automatically (if you are using `OkHttp` for API request).
- Local check you are logged in or not.
- Gets the token which is saved.
- Logout current user from local.

## 1 Login with username & password.

There are only 3 steps for login with username & password using `Credentials` module.

### 1.1 Create your token model extends from [AuthToken](../credentials/src/main/java/com/sun/auth/credentials/repositories/model/AuthToken.kt)

```kt
data class Token(
    @Expose @SerializedName("id") val id: String? = null,
    @Expose @SerializedName("user_id") val userId: String? = null,
    @Expose @SerializedName("token") val accessToken: String? = null,
    @Expose @SerializedName("refresh_token") val refreshToken: String? = null,
    @Expose @SerializedName("expired_at") val expiresIn: String? = null
) : AuthToken {
    override val crAccessToken: String
        get() = accessToken.orEmpty()
    override var crRefreshToken: String? = null
        get() = refreshToken ?: field
        set(value) {
            if (field == value) return
            field = value
        }
}
```

### 1.2 Create your config for Credentials via [Builder](../credentials/src/main/java/com/sun/auth/credentials/CredentialsAuth.kt).

```kt
CredentialsAuth.Builder()
    .context(applicationContext)
    .loginUrl(url = "https://your.url/api/v1/users/sign_in")
    .basicAuthentication(username = "username", password = "password") // if needed
    .authTokenClazz(Token::class.java) // required
    .build()
```

You can add more configs
see [`CredentialsAuth.Builder`](../credentials/src/main/java/com/sun/auth/credentials/CredentialsAuth.kt)

### 1.3 Call your login API to get result.

```kt
fun login(username: String, password: String) {
    viewModelScope.launch(Dispatchers.IO) {
        CredentialsAuth.getInstance().login(
            requestBody = LoginRequest(username, password),
            callback = object : AuthCallback<Token> {
                override fun success(data: Token?) {
                    _authenResult.postValue(AuthenResult(success = data))
                }

                override fun failure(exception: AuthException?) {
                    _authenResult.postValue(AuthenResult(error = exception))
                }
            }
        )
    }
}
```

## 2 Refresh token

### 2.1 Refresh token manually

Refresh token manually with 2 steps:

#### 2.1.1 Create refresh token Request using OkHttp

```kt
// PATCH request body sample
val requestBody = MultipartBody.Builder().addPart(
    MultipartBody.Part.createFormData(
        "refresh_token",
        getToken()?.crRefreshToken.orEmpty()
    )
).build()

// PUT, POST request body sample
val json = JsonObject().apply { // using gson
    addProperty("refresh_token", getToken()?.crRefreshToken.orEmpty())
}
val requestBody = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)

val refreshTokenRequest = Request.Builder()
      .url("https://your.url/api/v1/auth_tokens")
      .put(requestBody)
    //.post(requestBody) 
    //.patch(requestBody)
      .build()
```

#### 2.1.2 Call refresh token manually

```kt
fun refreshToken() {
    viewModelScope.launch(Dispatchers.IO) {
        CredentialsAuth.getInstance().refreshToken(
            request = refreshTokenRequest,
            callback = object : AuthCallback<Token> {
                override fun success(data: Token?) {
                    _refreshTokenResult.postValue(AuthenResult(success = data))
                }

                override fun failure(exception: AuthException?) {
                    _refreshTokenResult.postValue(AuthenResult(error = exception))
                }
            }
        )
    }
}
```

### 2.2 Refresh token automatically using `OkHttp`

There are 2 steps to achieve this

#### 2.2.1 Build refresh token request when token is updated via `Credentials.Builder`

```kt
CredentialsAuth.Builder()
    ...
    .authTokenChanged(object : AuthTokenChanged<Token> {
        override fun onTokenUpdate(token: Token?): Request {
            return buildRefreshTokenRequest(token)
        }
    })
    .build()

fun buildRefreshTokenRequest(token: Token?): Request? {
    // See 2.1.1 Create refresh token Request using OkHttp
}
```

#### 2.2.2 Add TokenAuthenticator to your OkHttp client setting

```kt
client = OkHttpClient().newBuilder()
    .authenticator(TokenAuthenticator<Token>())
    .addInterceptor(...)
    ...
    .build()
```

## 3 Local check you are logged in or not.

Just simply call this method

```kt
return CredentialsAuth.getInstance().isLoggedIn<Token>()
```

## 4 Gets the token which is saved.

Just simply call this method

```kt
return CredentialsAuth.getInstance().getToken<Token>()
```

## 5 Logout current user from local.

Just simply call this method

```kt
CredentialsAuth.getInstance().logout {
  // Do your job when logout. Ex: clear other local data.
}
```
