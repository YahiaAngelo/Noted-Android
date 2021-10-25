package com.noted.noted.model

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@Parcelize
open class Task(@PrimaryKey
                 var id: Long = 0,
                 var title: String = "",
                 var desc: String = "",
                 var checked: Boolean = false,
                 var reminder: Reminder? = null,
                 var date: Long = 0,
                 var noteCategories: @WriteWith<NoteCategory.NoteCategoryRealmListParceler>RealmList<NoteCategory> = RealmList()) : RealmObject(), Parcelable