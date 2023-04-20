package com.sun.auth.sample.biometrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BaseFragment<VB : ViewDataBinding, VM : ViewModel> : Fragment() {
    @get:LayoutRes
    abstract val fragmentLayout: Int
    abstract val viewModel: VM
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, fragmentLayout, container, false)
        binding.executePendingBindings()
        return binding.root
    }
}
