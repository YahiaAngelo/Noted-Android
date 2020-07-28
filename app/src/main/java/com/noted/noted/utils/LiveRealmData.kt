package com.noted.noted.utils

import androidx.lifecycle.LiveData
import io.realm.*

class LiveRealmData<T : RealmModel>(private val results: RealmResults<T>) :
    LiveData<RealmResults<T>>() {
    private val listener: RealmChangeListener<RealmResults<T>> =
        RealmChangeListener { results -> value = results }

    override fun onActive() {
        results.addChangeListener(listener)
        listener.onChange(results)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }

}

fun <T: RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)

