package com.github.rahul_gill.attendance.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

inline fun <T> Fragment.observerWithLifecycle(pref: Preference<T>, crossinline onNew: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        pref.observableValue
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect {
                onNew(it)
            }
    }
}