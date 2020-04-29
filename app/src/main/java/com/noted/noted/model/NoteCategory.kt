package com.noted.noted.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.parceler.Parcel
import org.parceler.Parcels
import org.parceler.converter.CollectionParcelConverter

@Parcel(implementations =  [io.realm.com_noted_noted_model_NoteCategoryRealmProxy::class] , value = Parcel.Serialization.BEAN, analyze = [NoteCategory::class])
open class NoteCategory(@PrimaryKey var id : Long = 0, var title:String = ""): RealmObject()


open class CategoryListParcelConverter : RealmListParcelConverter<NoteCategory>() {
    override fun itemFromParcel(parcel: android.os.Parcel?): NoteCategory {
        return Parcels.unwrap(parcel?.readParcelable(NoteCategory::class.java.classLoader))

    }

    override fun itemToParcel(input: NoteCategory?, parcel: android.os.Parcel?) {
        parcel?.writeParcelable(Parcels.wrap(NoteCategory::class.java, input), 0)
    }

}

abstract class RealmListParcelConverter<T:RealmObject> : CollectionParcelConverter<T, RealmList<T>>() {
    override fun createCollection(): RealmList<T> {
        return RealmList<T>()
    }
}