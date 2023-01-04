package com.sun.auth.sample

import com.sun.auth.social.model.SocialUser

data class SocialAuthResult(val user: SocialUser?, val exception: Throwable?)