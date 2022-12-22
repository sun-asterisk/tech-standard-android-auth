package com.sun.auth.sample.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel provider factory to instantiate CredentialAuthViewModel.
 * Required given CredentialAuthViewModel has a non-empty constructor
 */
class CredentialAuthViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CredentialAuthViewModel::class.java)) {
            return CredentialAuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}