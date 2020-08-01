package com.noted.noted.viewmodel

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.noted.noted.model.Task
import com.noted.noted.repositories.TaskRepo
import com.noted.noted.view.bindItem.TaskBinding
import io.realm.RealmResults

class TasksFragmentViewModel(private val taskRepo: TaskRepo) : ViewModel() {

    private lateinit var mTasksResult: LiveData<List<TaskBinding>>

    init {
        subscribeToLatestTasks()
    }

    fun getTasks() : LiveData<List<TaskBinding>> {
        return mTasksResult
    }

    private fun subscribeToLatestTasks(){
        val tasksList = taskRepo.getTasks()
        mTasksResult = Transformations.map(tasksList, Function<RealmResults<Task>, List<TaskBinding>> {
                return@Function it.toBinding()
            })

    }

    private fun RealmResults<Task>.toBinding(): List<TaskBinding> {
        val taskBindingList: MutableList<TaskBinding> = mutableListOf()
        for (task in this) {
            taskBindingList.add(TaskBinding(task, taskRepo))
        }
        return taskBindingList
    }

    override fun onCleared() {
        taskRepo.realm.close()
        super.onCleared()
    }


}