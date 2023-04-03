package com.sun.auth.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.biometric.LoginViewModel
import com.sun.auth.sample.credentials.CredentialsAuthViewModel
import com.sun.auth.sample.facebook.firebase.FacebookFirebaseAuthViewModel
import com.sun.auth.sample.facebook.standard.FacebookAuthViewModel
import com.sun.auth.sample.google.firebase.GoogleFirebaseAuthViewModel
import com.sun.auth.sample.google.standard.GoogleAuthViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel() as T
        }
        if (modelClass.isAssignableFrom(CredentialsAuthViewModel::class.java)) {
            return CredentialsAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            return GoogleAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(GoogleFirebaseAuthViewModel::class.java)) {
            return GoogleFirebaseAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(FacebookAuthViewModel::class.java)) {
            return FacebookAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(FacebookFirebaseAuthViewModel::class.java)) {
            return FacebookFirebaseAuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
