package com.noted.noted.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel

@Parcel(implementations =  [io.realm.com_noted_noted_model_NoteCategoryRealmProxy::class] , value = Parcel.Serialization.BEAN, analyze = [NoteCategory::class])
open class NoteCategory(@PrimaryKey var id : Long = 0, var title:String = ""): RealmObject()




