package com.sun.auth.sample.google

import com.sun.auth.social.model.SocialUser

data class GoogleAuthResult(val user: SocialUser?, val exception: Throwable?)