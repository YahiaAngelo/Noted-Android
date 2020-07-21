package com.noted.noted.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel
import org.parceler.ParcelPropertyConverter

@Parcel(implementations = arrayOf(io.realm.com_noted_noted_model_TaskRealmProxy::class),
value = Parcel.Serialization.BEAN,
analyze = arrayOf(Task::class))
open class Task : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var title: String = ""
    var desc: String = ""
    var checked: Boolean = false
    var reminder: Reminder? = null
    var date: Long = 0
    open var noteCategories: RealmList<NoteCategory> = RealmList()
        get() = field
        @ParcelPropertyConverter(CategoryListParcelConverter::class)
        set(value) {
            field = value
        }

    constructor() : super()
    constructor(
        id: Long = 0,
        title: String = "",
        desc: String = "",
        checked: Boolean = false,
        date: Long = 0
    ) {
        this.id = id
        this.title = title
        this.desc = desc
        this.checked = checked
        this.date = date
    }
}