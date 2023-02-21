package com.sun.auth.core

/**
 * Base Class of the error when authenticate with given info.
 * @param originalThrowable The origin error if have.
 */
sealed class SocialAuthException(originalThrowable: Throwable?) :
    RuntimeException(originalThrowable)

/**
 * This exception occurs when Provider user is not generated. Try to sign in again.
 */
class UnexpectedAuthException : SocialAuthException(RuntimeException("Can not generate user/token"))

/**
 * This exception occurs when User cancel sign in process.
 */
class CancellationAuthException :
    SocialAuthException(RuntimeException("User cancel the authentication process"))

/**
 * This exception occurs when User's device time or given info is incorrect, must be correct to sign in.
 */
class InvalidCredentialsException(originalThrowable: Throwable?) :
    SocialAuthException(originalThrowable)
