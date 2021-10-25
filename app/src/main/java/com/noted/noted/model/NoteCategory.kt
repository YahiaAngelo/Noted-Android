package com.noted.noted.model

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
open class NoteCategory(@PrimaryKey var id : Long = 0, var title:String = ""): RealmObject(), Parcelable{
    object NoteCategoryRealmListParceler: RealmListParceler<NoteCategory> {
        override val clazz: Class<NoteCategory>
            get() = NoteCategory::class.java
    }
}




