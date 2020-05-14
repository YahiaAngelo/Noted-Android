package com.noted.noted.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.noted.noted.MainActivity
import com.noted.noted.R
import com.noted.noted.databinding.FragmentTasksBinding
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.view.bindItem.TaskBinding
import com.noted.noted.view.customView.SimpleSwipeCallback
import com.noted.noted.view.customView.SimpleSwipeDragCallback
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort


class TasksFragment : BaseFragment(), ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private var searchView: TextInputEditText? = null
    private var searchTextWatcher: TextWatcher? = null
    private lateinit var binding: FragmentTasksBinding
    private val itemAdapter = ItemAdapter<TaskBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var mRealm: Realm
    private lateinit var touchCallback: SimpleDragCallback
    private lateinit var touchHelper: ItemTouchHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTasksBinding.inflate(inflater, container, false)

        initAdapter()
        mRealm = Realm.getDefaultInstance()
         touchCallback = SimpleSwipeDragCallback(
             this,
             this,
             requireContext().resources.getDrawable(R.drawable.ic_delete, requireContext().theme),
             ItemTouchHelper.LEFT,
             requireContext().resources.getColor(R.color.card_red, requireContext().theme)
         ).withBackgroundSwipeRight(requireContext().resources.getColor(R.color.primary, requireContext().theme))
             .withLeaveBehindSwipeRight(requireContext().resources.getDrawable(R.drawable.ic_archive, requireContext().theme))


         touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.tasksRecycler)

        val layoutManager = LinearLayoutManager(context)
        binding.tasksRecycler.layoutManager = layoutManager
        binding.tasksRecycler.adapter = fastAdapter
        binding.tasksRecycler.itemAnimator = DefaultItemAnimator()


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initAdapter() {
        itemAdapter.itemFilter.filterPredicate = { item: TaskBinding, constraint: CharSequence? ->
            item.taskTitle.text.contains(constraint.toString(), true)
        }
        initSearch()
    }

    private fun initTasksAdd(){

    }

    private fun update() {
        val tasksList = mRealm.where(Task::class.java).sort("date", Sort.DESCENDING).findAll()
        itemAdapter.setNewList(tasksList.toBinding())
        itemAdapter.setNewList(generateItems())

    }

    private fun RealmResults<Task>.toBinding(): List<TaskBinding> {
        val taskBindingList: MutableList<TaskBinding> = mutableListOf()
        for (task in this) {
            taskBindingList.add(TaskBinding(task))
        }
        return taskBindingList
    }

    private fun initSearch() {
        val activity = this.activity
        if (activity is MainActivity) {
            searchView = activity.findViewById(R.id.main_search)
            searchTextWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    itemAdapter.filter(s)
                }


            }
            searchView!!.addTextChangedListener(searchTextWatcher)
        }
    }

    private fun generateItems() :List<TaskBinding>{
        return listOf(
            TaskBinding(Task(1, "This is my first task", "", false, Reminder(), System.currentTimeMillis())),
            TaskBinding(Task(2, "This is my second task", "", false, Reminder(), System.currentTimeMillis()))

        )
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun onPause() {
        if (searchView != null && searchTextWatcher != null) {
            searchView!!.removeTextChangedListener(searchTextWatcher)
        }
        super.onPause()
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {

    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
        return true
    }

    override fun itemSwiped(position: Int, direction: Int) {
        if (direction == ItemTouchHelper.LEFT){
            itemAdapter.remove(position)
        }else{
            itemAdapter.remove(position)
        }
    }
}