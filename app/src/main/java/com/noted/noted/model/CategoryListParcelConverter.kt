package com.noted.noted.model

import org.parceler.Parcels

open class CategoryListParcelConverter : RealmListParcelConverter<NoteCategory>() {
    override fun itemFromParcel(parcel: android.os.Parcel?): NoteCategory {
        return Parcels.unwrap(parcel?.readParcelable(NoteCategory::class.java.classLoader))

    }

    override fun itemToParcel(input: NoteCategory?, parcel: android.os.Parcel?) {
        parcel?.writeParcelable(Parcels.wrap(NoteCategory::class.java, input), 0)
    }

}