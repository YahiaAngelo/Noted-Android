package com.noted.noted.model

import android.os.Parcelable
import com.noted.noted.R
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@Parcelize
open class Note(@PrimaryKey
                 var id: Long = 0,
                 var title: String = "",
                 var body: String = "",
                 var date: Long = 0,
                 var color: Int = R.color.background,
                 var colorHex: String = "",
                 var isFavorite: Boolean = false,
                 var priority: Int = 0,
                 var categories: @WriteWith<NoteCategory.NoteCategoryRealmListParceler> RealmList<NoteCategory> = RealmList()) : RealmObject(), Parcelable


