package com.sun.auth.social.google

import com.sun.auth.social.SocialAuthException

/**
 * This exception occurs when User's device time is incorrect, must be correct to sign in.
 */
class ModifiedDateTimeException : SocialAuthException(null)
