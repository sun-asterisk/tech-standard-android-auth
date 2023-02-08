# Credentials

This is small helper module to help you quickly do some Social authentication actions via `Firebase`
like:

- SignIn with Social accounts, currently support:
    - [Google](google/README.md)
    - [Facebook](facebook/README.md)
    -
- Local check you are Signed in or not.
- Gets the current Signed user.
- SignOut current user from local.

## Detail integration & usage.

See what social authentication type you want to integrate

### [1. Google Authentication](google/README.md)

### [2. Facebook Authentication](facebook/README.md)

## Other Notes:

### To access specific social authentication class

```kt
SocialAuth.getAuth<GoogleAuth>(type = SocialType.GOOGLE)
SocialAuth.getAuth<FacebookAuth>(type = SocialType.FACEBOOK)
```

### Exceptions

`ModifiedDateTimeException`: This exception occurs when User's device time is incorrect.