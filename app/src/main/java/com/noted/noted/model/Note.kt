package com.noted.noted.model

import com.noted.noted.R
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel
import org.parceler.ParcelPropertyConverter

@Parcel(implementations = [io.realm.com_noted_noted_model_NoteRealmProxy::class],
value = Parcel.Serialization.BEAN,
analyze = [Note::class])
open class Note : RealmObject{
    @PrimaryKey
    var id: Long = 0
    var title = ""
    var body = ""
    var date: Long = 0
    var color = R.color.background
    var colorHex = ""
    var isFavorite = false
    open var categories: RealmList<NoteCategory> = RealmList()
        @ParcelPropertyConverter(CategoryListParcelConverter::class)
        set

    constructor() : super()

    constructor(
        id: Long,
        title: String,
        body: String,
        date: Long,
        color: Int,
        categories: RealmList<NoteCategory>
    ) {
        this.id = id
        this.title = title
        this.body = body
        this.date = date
        this.color = color
        this.categories = categories
    }



}


