package com.noted.noted.viewmodel

import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.noted.noted.model.Note
import com.noted.noted.utils.LiveRealmData
import com.noted.noted.view.bindItem.NoteBinding
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.Sort

class NotesFragmentViewModel(savedStateHandle: SavedStateHandle) : ViewModel(){

    val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private lateinit var mNotesResult: LiveData<List<NoteBinding>>

    init {
        subscribeToLatestNotes()
    }

    fun getNotes() : LiveData<List<NoteBinding>>{
        return mNotesResult
    }

    private fun subscribeToLatestNotes(){
       val notesList = realm.where(Note::class.java).sort("date", Sort.DESCENDING).findAllAsync().asLiveData()
        mNotesResult = Transformations.map(notesList,
            Function<RealmResults<Note>, List<NoteBinding>> {
                Log.e("Noted", "New data!")

                return@Function it.toBinding()
            })

    }


    private fun RealmResults<Note>.toBinding():List<NoteBinding>{
        val noteBindingList : MutableList<NoteBinding> = mutableListOf()
        for (note in this){
            noteBindingList.add(NoteBinding(note))
        }
        return noteBindingList
    }

    override fun onCleared() {
        Log.e("Noted", "I'm cleared")
        realm.close()
        super.onCleared()
    }

    private fun <T: RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)


}