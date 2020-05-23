package com.kundalini.core

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.util.*

class AtomicObservableValue<T>(intialValue: T? = null) : ObservableValue<T> {
    private var _value: T? = intialValue
    private val changeListeners = ArrayList<ChangeListener<in T>>()
    private val invalidationListeners = ArrayList<InvalidationListener>()

    override fun removeListener(listener: ChangeListener<in T>?) {
        synchronized(this) {
            changeListeners.remove(listener)
        }
    }

    override fun removeListener(listener: InvalidationListener?) {
        synchronized(this) {
            invalidationListeners.remove(listener)
        }
    }

    override fun addListener(listener: ChangeListener<in T>?) {
        if (listener == null) return
        synchronized(this) {
            changeListeners.add(listener)
        }
    }

    override fun addListener(listener: InvalidationListener?) {
        if (listener == null) return
        synchronized(this) {
            invalidationListeners.add(listener)
        }
    }

    override fun getValue(): T? = synchronized(this) {
        _value
    }

    fun setValue(value: T?) {
        synchronized(this) {
            val oldValue = _value
            _value = value
            changeListeners.forEach { it.changed(this@AtomicObservableValue, oldValue, _value) }
        }
    }
}