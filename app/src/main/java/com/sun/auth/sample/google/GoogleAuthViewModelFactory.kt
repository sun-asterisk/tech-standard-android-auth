package com.sun.auth.sample.google

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel provider factory to instantiate GoogleSignInViewModel.
 * Required given GoogleSignInViewModel has a non-empty constructor
 */
class GoogleAuthViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            return GoogleAuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}