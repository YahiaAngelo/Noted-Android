package com.noted.noted.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel

@Parcel
open class Reminder : RealmObject {
    @PrimaryKey
    var id : Long = 0
    var date : Long = 0
    var repeat : Boolean = false

    constructor() : super()
    constructor(id : Long = 0, date : Long = 0, repeat : Boolean = false){
        this.id = id
        this.date = date
        this.repeat = repeat
    }

}