package com.noted.noted.repositories

import com.noted.noted.model.Note
import com.noted.noted.model.NoteCategory
import com.noted.noted.utils.LiveRealmData
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.Sort

class NoteRepo {
    var realm: Realm = Realm.getDefaultInstance()

    fun deleteNote(note: Note){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            note.deleteFromRealm()
            it.commitTransaction()
        }
    }

    fun deleteNote(id : Long){
        realm = Realm.getDefaultInstance()
        realm.use {
            val note = realm.where(Note::class.java).equalTo("id", id).findFirst()
            realm.beginTransaction()
            note!!.deleteFromRealm()
            realm.commitTransaction()
            realm.close()
        }
    }

    fun getNotes():LiveRealmData<Note>{
        realm = Realm.getDefaultInstance()
        return realm.where(Note::class.java).sort("date", Sort.DESCENDING).findAllAsync().asLiveData()
    }

    fun addNote(note: Note){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            it.copyToRealmOrUpdate(note)
            it.commitTransaction()
            it.close()
        }
    }

    fun getCategories():RealmResults<NoteCategory>{
        realm = Realm.getDefaultInstance()
        return realm.where(NoteCategory::class.java).findAll()
    }

    fun saveCategory(noteCategory: NoteCategory) {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            realm.beginTransaction()
            realm.copyToRealm(noteCategory)
            realm.commitTransaction()
            realm.close()
        }
    }

    fun deleteCategory(noteCategory: NoteCategory) {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            realm.beginTransaction()
            noteCategory.deleteFromRealm()
            realm.commitTransaction()
            realm.close()
        }
    }

}    private fun <T: RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)
