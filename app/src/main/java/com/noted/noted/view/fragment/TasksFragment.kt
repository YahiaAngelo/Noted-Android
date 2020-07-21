package com.noted.noted.view.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.noted.noted.R
import com.noted.noted.databinding.FragmentTasksBinding
import com.noted.noted.model.NoteCategory
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.utils.AlarmUtils
import com.noted.noted.utils.Utils
import com.noted.noted.view.bindItem.TaskBinding
import com.noted.noted.view.customView.SimpleSwipeCallback
import com.noted.noted.view.customView.SimpleSwipeDragCallback
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.koin.android.ext.android.inject
import java.util.*


class TasksFragment : BaseFragment(), ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private var searchView: TextInputEditText? = null
    private var searchTextWatcher: TextWatcher? = null
    private lateinit var binding: FragmentTasksBinding
    private val itemAdapter = ItemAdapter<TaskBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var mRealm: Realm
    private lateinit var touchCallback: SimpleDragCallback
    private lateinit var touchHelper: ItemTouchHelper
    private var addTaskDialog : BottomSheetDialog? = null
    private val alarmUtils : AlarmUtils by inject()

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
            ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_delete, requireContext().theme)!!,
            ItemTouchHelper.LEFT,
            requireContext().resources.getColor(R.color.card_red, requireContext().theme)
        ).withBackgroundSwipeRight(
            requireContext().resources.getColor(
                R.color.primary,
                requireContext().theme
            )
        )
            .withLeaveBehindSwipeRight(
                ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_add, requireContext().theme)!!
            )


        touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.tasksRecycler)

        val layoutManager = LinearLayoutManager(context)
        binding.tasksRecycler.layoutManager = layoutManager
        binding.tasksRecycler.adapter = fastAdapter
        binding.tasksRecycler.itemAnimator = DefaultItemAnimator()


        initTasksAdd()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initAdapter() {
        itemAdapter.itemFilter.filterPredicate = { item: TaskBinding, constraint: CharSequence? ->
            item.taskTitle.text.contains(constraint.toString(), true)
        }
    }

    private fun initTasksAdd() {
        val currentCalendar = Calendar.getInstance()
        var selectedCalendar : Calendar? = null
        val view = layoutInflater.inflate(R.layout.task_add_bottom_sheet, null)
        addTaskDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetMenuTheme)
        addTaskDialog!!.setContentView(view)
        val titleEditText : TextInputEditText = view.findViewById(R.id.task_add_editText)
        val descEditText : TextInputEditText = view.findViewById(R.id.task_add_desc_editText)
        val descButton : MaterialButton = view.findViewById(R.id.task_add_desc)
        val reminderButton : MaterialButton = view.findViewById(R.id.task_add_reminder)
        val repeatButton : MaterialButton = view.findViewById(R.id.task_add_repeat)
        val saveButton : MaterialButton = view.findViewById(R.id.task_add_save)

        descButton.setOnClickListener {
            descEditText.visibility = View.VISIBLE
        }

        val materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker()
        materialDatePickerBuilder.setTitleText("Reminder Date")
        val calendarConstraints = CalendarConstraints.Builder()
        calendarConstraints.setStart(MaterialDatePicker.todayInUtcMilliseconds())
        calendarConstraints.setValidator(DateValidatorPointForward.now())
        materialDatePickerBuilder.setCalendarConstraints(calendarConstraints.build())
        val materialDatePicker = materialDatePickerBuilder.build()
        materialDatePicker.addOnCancelListener {
            if (reminderButton.isChecked){
                reminderButton.performClick()
            }
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            selectedCalendar = Calendar.getInstance()
            selectedCalendar!!.time = Date(it)

            TimePickerDialog(requireContext(),
                R.style.MyDialogTheme,TimePickerDialog.OnTimeSetListener {_ , hourOfDay, minute ->
                    selectedCalendar!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar!!.set(Calendar.MINUTE, minute)
                    selectedCalendar!!.set(Calendar.SECOND, 0)

                },
                currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), false).show()
        }

        reminderButton.setOnClickListener {
            if (reminderButton.isChecked){
                materialDatePicker.showNow(childFragmentManager, materialDatePicker.tag)
            }
        }

        saveButton.setOnClickListener {
            if (titleEditText.text!!.isNotEmpty()){
                mRealm = Realm.getDefaultInstance()
                mRealm.use {
                    val task = Task(UUID.randomUUID().mostSignificantBits, titleEditText.text.toString(), descEditText.text.toString(), false, System.currentTimeMillis())
                    if (selectedCalendar != null){
                      val reminder = Reminder(UUID.randomUUID().mostSignificantBits,
                        selectedCalendar!!.time.time, repeatButton.isChecked)
                        task.reminder = reminder
                        alarmUtils.setAlarm(task, requireContext())
                    }
                    it.beginTransaction()
                    it.copyToRealm(task)
                    it.commitTransaction()
                }
                update()
                addTaskDialog!!.dismiss()
                initTasksAdd()
            }

        }


    }

    fun showTasksAdd(){
        if (addTaskDialog!=null){
            addTaskDialog!!.show()
        }
    }

    private fun update() {
        val tasksList = mRealm.where(Task::class.java).sort( "checked", Sort.ASCENDING, "date", Sort.DESCENDING).findAll()
        itemAdapter.setNewList(tasksList.toBinding())

    }

    private fun RealmResults<Task>.toBinding(): List<TaskBinding> {
        val taskBindingList: MutableList<TaskBinding> = mutableListOf()
        for (task in this) {
            taskBindingList.add(TaskBinding(task))
        }
        return taskBindingList
    }


    private fun generateItems(): List<TaskBinding> {
        return listOf(
            TaskBinding(
                Task(
                    1,
                    "This is my first task",
                    "",
                    false,
                    System.currentTimeMillis()
                )
            ),
            TaskBinding(
                Task(
                    2,
                    "This is my second task",
                    "",
                    false,
                    System.currentTimeMillis()
                )
            )

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
        if (direction == ItemTouchHelper.LEFT) {
            val task = itemAdapter.getAdapterItem(position).task
            itemAdapter.remove(position)
            if (task.reminder != null){
                alarmUtils.cancelAlarm(task.reminder!!, requireContext())
            }
            mRealm = Realm.getDefaultInstance()
            mRealm.use {
                it.beginTransaction()
                task.deleteFromRealm()
                it.commitTransaction()
            }
        } else {
            fastAdapter.notifyItemChanged(position)
            Utils.showCategories(requireContext(), layoutInflater, object:
                Utils.Companion.OnSelectedCategory {
                override fun onSelected(noteCategory: NoteCategory) {
                    mRealm = Realm.getDefaultInstance()
                    mRealm.use {
                        it.beginTransaction()
                        itemAdapter.getAdapterItem(position).task.noteCategories.add(noteCategory)
                        it.commitTransaction()
                    }
                    fastAdapter.notifyItemChanged(position)
                }
            } )
        }
    }


    override fun filterCategories(categoryName: String) {
        val tasksList = mRealm.where(Task::class.java).equalTo("noteCategories.title", categoryName).sort("date", Sort.DESCENDING).findAll()
        itemAdapter.setNewList(tasksList.toBinding())
    }

    override fun refresh() {
        update()
    }

    override fun filterItem(string: String) {
        itemAdapter.filter(string)
    }
}