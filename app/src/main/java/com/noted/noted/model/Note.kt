package com.noted.noted.model

import android.os.Parcelable
import com.noted.noted.R
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


@Parcelize
open class Note(@PrimaryKey var id:Long = 0, var title:String = "", var body:String = "",var date: Long = 0, var color:Int = R.color.background):RealmObject(),
    Parcelable {

}

