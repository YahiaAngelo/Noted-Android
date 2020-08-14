package com.noted.noted.viewmodel

import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.noted.noted.model.Note
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.view.bindItem.NoteBinding
import io.realm.RealmResults

class NotesFragmentViewModel(private val noteRepo: NoteRepo) : ViewModel(){


    private lateinit var mNotesResult: LiveData<List<NoteBinding>>

    init {
        subscribeToLatestNotes()
    }

    fun getNotes() : LiveData<List<NoteBinding>>{
        return mNotesResult
    }

    private fun subscribeToLatestNotes(){
       val notesList = noteRepo.getNotes()
        mNotesResult = Transformations.map(notesList,
            Function<RealmResults<Note>, List<NoteBinding>> {
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
        noteRepo.realm.close()
        super.onCleared()
    }



}