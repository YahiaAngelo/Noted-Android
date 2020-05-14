package com.noted.noted.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel

@Parcel
open class Reminder : RealmObject {
    @PrimaryKey
    var id : Long = 0
    var startDate : Long = 0
    var endDate: Long = 0
    var repeat : Boolean = false
    constructor() : super()
    constructor(id : Long = 0, startDate : Long = 0, endDate: Long = 0, repeat : Boolean = false){
        this.id = id
        this .startDate = startDate
        this.endDate = endDate
        this.repeat = repeat
    }
}