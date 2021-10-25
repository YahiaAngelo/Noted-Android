package com.noted.noted.model

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
open class Reminder(@PrimaryKey var id : Long = 0, var date : Long = 0, var repeat : Boolean = false) : RealmObject(), Parcelable