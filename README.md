[![](https://jitpack.io/v/sun-asterisk/tech-standard-android-auth.svg)](https://jitpack.io/#sun-asterisk/tech-standard-android-auth)

# tech-standard-android-auth

This is repo contains multiple helper modules which help you quickly adapt your Authentication
process to your application.<br>
See [sample code](app/src/main/java/com/sun/auth/sample) & each library module for more detail.

## [Authentication with credential](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Credential-authentication)
This [Credentials auth library](https://github.com/sun-asterisk/tech-standard-android-auth/tree/master/credentialsauth) 
provides a simple way to help you easy to handle credentials authentication and token management.
Some of features are `signIn`, `signOut`, `refreshToken`...

## [Authentication with Google](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Google-Standard-Authentication)
There are 2 small libraries built on top of `Firebase authentication` and `Google Sign-In SDK` for android.
1. With [Firebase authentication](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Google-Authentication-via-Firebase)
you can manage the authentication result from Firebase. 
2. With [Google Standard authentication](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Google-Standard-Authentication)
you get the GoogleSignInAccount result directly from Google, and provide signIn info to your BackEnd.

## [Authentication with Facebook](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Facebook-Standard-authentication)
Same with Google, the 2 libraries built on top of `Firebase authentication` and `Facebook SDK` for android.
1. With [Firebase authentication](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Facebook-Authentication-via-Firebase)
you can manage the authentication result from Firebase.
2. With [Facebook Standard authentication](https://github.com/sun-asterisk/tech-standard-android-auth/wiki/Facebook-Standard-authentication)
you get the result directly from Facebook, and provide signIn info to your BackEnd.

## Getting started
From project `build.gradle` (or `settings.gradle`), add Jitpack maven
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
Then add required dependencies to your `app/build.gradle`
```groovy
dependencies {
    implementation "com.github.sun-asterisk.tech-standard-android-auth:core:${latest_version}" // required
    
    implementation "com.github.sun-asterisk.tech-standard-android-auth:credentialsauth:${latest_version}"
    implementation "com.github.sun-asterisk.tech-standard-android-auth:googleauth:${latest_version}"
    implementation "com.github.sun-asterisk.tech-standard-android-auth:googlefirebaseauth:${latest_version}"
    implementation "com.github.sun-asterisk.tech-standard-android-auth:facebookauth:${latest_version}"
    implementation "com.github.sun-asterisk.tech-standard-android-auth:facebookfirebaseauth:${latest_version}"
}
```
That's it, to see more detail, see [Wiki](https://github.com/sun-asterisk/tech-standard-android-auth/wiki)

## Contributing
Feel free to make a pull request. Make sure your code is formatted and fixed lint issues.

1. Clean project first to copy team-props git-hooks
2. Pull and checkout from `develop` branch
3. A commit message must be have one of these prefixes [add|modify|fix|revert|hotfix], Ex: [Modify] README.md
4. To auto format code, run `./gradlew ktlintFormat`
5. To check lint issues, run `./gradlew detekt`
See all reports in `reports` folder.

## License
```
Copyright 2023 Sun-Asterisk.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
