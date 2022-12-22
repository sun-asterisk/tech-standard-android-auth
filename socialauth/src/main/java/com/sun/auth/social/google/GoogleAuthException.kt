package com.sun.auth.social.google

import com.sun.auth.social.SocialAuthException

/**
 * This exception occurs when Google token id is not generated. Try to sign in again.
 */
class NoTokenGeneratedException : SocialAuthException(null)

/**
 * This exception occurs when User's device time is incorrect, must be correct to sign in.
 */
class ModifiedDateTimeException: SocialAuthException(null)
