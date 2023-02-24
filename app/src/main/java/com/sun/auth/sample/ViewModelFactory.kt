package com.sun.auth.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.credentials.other.CredentialAuthViewModel
import com.sun.auth.sample.credentials.suntech.SunTechViewModel
import com.sun.auth.sample.facebook.firebase.FacebookFirebaseAuthViewModel
import com.sun.auth.sample.facebook.standard.FacebookAuthViewModel
import com.sun.auth.sample.google.firebase.GoogleFirebaseAuthViewModel
import com.sun.auth.sample.google.standard.GoogleAuthViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CredentialAuthViewModel::class.java)) {
            return CredentialAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(SunTechViewModel::class.java)) {
            return SunTechViewModel() as T
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
