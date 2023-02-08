package com.sun.auth.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sun.auth.sample.credentials.other.CredentialAuthViewModel
import com.sun.auth.sample.credentials.suntech.SunTechViewModel
import com.sun.auth.sample.facebook.FacebookAuthViewModel
import com.sun.auth.sample.google.GoogleAuthViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CredentialAuthViewModel::class.java)) {
            return CredentialAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            return GoogleAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(FacebookAuthViewModel::class.java)) {
            return FacebookAuthViewModel() as T
        }
        if (modelClass.isAssignableFrom(SunTechViewModel::class.java)) {
            return SunTechViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}