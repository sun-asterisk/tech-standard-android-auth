package com.sun.auth.social.model

/**
 * Wrapper of the error when authenticate with given info.
 * @param originalThrowable The origin error.
 */
sealed class SocialAuthException(originalThrowable: Throwable?) :
    RuntimeException(originalThrowable)

/**
 * This exception occurs when Google token id is not generated. Try to sign in again.
 */
class NoTokenGeneratedException : SocialAuthException(null)

/**
 * This exception occurs when User cancel to sign in.
 */
class CancelAuthException : SocialAuthException(null)

/**
 * This exception occurs when User's device time is incorrect, must be correct to sign in.
 */
class ModifiedDateTimeException: SocialAuthException(null)

/**
 * This exception occurs rarely, developer should check. Maybe try to logout and sign in again.
 */
class AuthApiException(throwable: Throwable?): SocialAuthException(throwable)
