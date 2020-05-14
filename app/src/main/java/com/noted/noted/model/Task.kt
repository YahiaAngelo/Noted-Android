package com.noted.noted.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel

@Parcel
open class Task : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var title: String = ""
    var desc: String = ""
    var checked: Boolean = false
    var reminder: Reminder? = null
    var date: Long = 0

    constructor() : super()
    constructor(
        id: Long = 0,
        title: String = "",
        desc: String = "",
        checked: Boolean = false,
        reminder: Reminder ,
        date: Long = 0
    ) {
        this.id = id
        this.title = title
        this.desc = desc
        this.checked = checked
        this.reminder = reminder
        this.date = date
    }
}