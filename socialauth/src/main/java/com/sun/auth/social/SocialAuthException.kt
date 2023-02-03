package com.sun.auth.social

/**
 * Base Class of the error when authenticate with given info.
 * @param originalThrowable The origin error.
 */
open class SocialAuthException(originalThrowable: Throwable?) :
    RuntimeException(originalThrowable)

/**
 * This exception occurs when Provider token id is not generated. Try to sign in again.
 */
class NoTokenGeneratedException : SocialAuthException(null)

/**
 * This exception occurs when User cancel to sign in.
 */
class SocialCancelAuthException : SocialAuthException(null)

/**
 * This exception occurs rarely, developer should check. Maybe try to sign out and sign in again.
 */
class SocialAuthApiException(throwable: Throwable?) : SocialAuthException(throwable)
