package com.github.rahul_gill.attendance.util

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import java.io.Serializable
import kotlin.reflect.KProperty

class SavableMutableLiveData<T>(
    private val savable: Bundle,
    private val key: String,
    defaultValue: T
) : MutableLiveData<T>() {
    init {
        value = defaultValue
    }

    override fun setValue(value: T) {
        super.setValue(value)
        if (value == null) {
            savable.remove(key)
            return
        }
        when (value) {
            is Int -> savable.putInt(key, value)
            is Long -> savable.putLong(key, value)
            is Float -> savable.putFloat(key, value)
            is String -> savable.putString(key, value)
            is Bundle -> savable.putBundle(key, value)
            is Serializable -> savable.putSerializable(key, value)
            is Parcelable -> savable.putParcelable(key, value)
        }
    }

    override fun getValue(): T {
        return super.getValue() ?: (savable.get(key) as T).apply { value = this }
    }
}

open class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    private val savable = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savable.putAll(savedInstanceState.getBundle("_state"))
        }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle("_state", savable)
        super.onSaveInstanceState(outState)
    }


    protected fun <T> savedStateOf(key: String, defaultValue: T) = SavableMutableLiveData(savable, key, defaultValue)
}