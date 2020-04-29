package com.noted.noted.model

import com.noted.noted.R
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel
import org.parceler.ParcelPropertyConverter





/*@Parcel(
    implementations = [io.realm.com_noted_noted_model_NoteRealmProxy::class],
    value = Parcel.Serialization.BEAN,
    analyze =  [Note::class] )
open class Note(@PrimaryKey var id:Long = 0, var title:String = "", var body:String = "",var date: Long = 0, var color:Int = R.color.background):RealmObject(){

    @ParcelPropertyConverter(RealmListParcelConverter::class)
    open var categories: RealmList<NoteCategory>? = null


}

 */
