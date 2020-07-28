package com.noted.noted.repositories

import com.noted.noted.model.NoteCategory
import com.noted.noted.model.Task
import com.noted.noted.utils.LiveRealmData
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.Sort

class TaskRepo {
    var realm: Realm = Realm.getDefaultInstance()

    fun getTasks():LiveRealmData<Task>{
        realm = Realm.getDefaultInstance()
        return realm.where(Task::class.java).sort( "checked", Sort.ASCENDING, "date", Sort.DESCENDING).findAllAsync().asLiveData()
    }

    fun filterTasksCategories(title: String):LiveRealmData<Task>{
        realm = Realm.getDefaultInstance()
        return realm.where(Task::class.java).equalTo("noteCategories.title", title)
            .sort( "checked", Sort.ASCENDING, "date", Sort.DESCENDING).findAllAsync().asLiveData()
    }

    fun addTask(task: Task){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            it.copyToRealm(task)
            it.commitTransaction()
        }
    }

    fun deleteTask(task: Task){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            task.deleteFromRealm()
            it.commitTransaction()
        }
    }

    fun addCategoryToTask(task: Task, noteCategory: NoteCategory){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            task.noteCategories.add(noteCategory)
            it.commitTransaction()
        }
    }

    fun checkTask(task: Task, check: Boolean){
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            task.checked = check
            it.commitTransaction()
        }
    }


    private fun <T: RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)
}