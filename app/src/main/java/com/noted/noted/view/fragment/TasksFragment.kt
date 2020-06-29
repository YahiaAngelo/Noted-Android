package com.noted.noted.view.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.noted.noted.MainActivity
import com.noted.noted.R
import com.noted.noted.databinding.FragmentTasksBinding
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.utils.AlarmUtils
import com.noted.noted.view.activity.realm
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
    val alarmUtils : AlarmUtils by inject()

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
        ).withBackgroundSwipeRight(
            requireContext().resources.getColor(
                R.color.primary,
                requireContext().theme
            )
        )
            .withLeaveBehindSwipeRight(
                requireContext().resources.getDrawable(
                    R.drawable.ic_archive,
                    requireContext().theme
                )
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
        initSearch()
    }

    private fun initTasksAdd() {
        val currentCalendar = Calendar.getInstance()
        val selectedCalendar = Calendar.getInstance()
        val view = layoutInflater.inflate(R.layout.task_add_bottom_sheet, null)
        addTaskDialog = BottomSheetDialog(requireContext())
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
            selectedCalendar.time = Date(it)
            TimePickerDialog(requireContext(),
                R.style.MyDialogTheme,TimePickerDialog.OnTimeSetListener {_ , hourOfDay, minute ->
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    selectedCalendar.set(Calendar.SECOND, 0)

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
                    var reminder = Reminder()
                    if (reminderButton.isChecked){
                        reminder = Reminder(UUID.randomUUID().mostSignificantBits,
                        selectedCalendar.time.time, repeatButton.isChecked)
                    }
                    val task = Task(UUID.randomUUID().mostSignificantBits, titleEditText.text.toString(), descEditText.text.toString(), false, reminder, System.currentTimeMillis())
                    realm.beginTransaction()
                    realm.copyToRealm(task)
                    realm.commitTransaction()
                    update()
                    alarmUtils.setAlarm(task, requireContext())
                    addTaskDialog!!.dismiss()
                    initTasksAdd()

                }
            }

        }


    }

    fun showTasksAdd(){
        if (addTaskDialog!=null){
            addTaskDialog!!.show()
        }
    }

    private fun update() {
        val tasksList = mRealm.where(Task::class.java).sort("date", Sort.DESCENDING).findAll()
        itemAdapter.setNewList(tasksList.toBinding())
        //itemAdapter.setNewList(generateItems())

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

    private fun generateItems(): List<TaskBinding> {
        return listOf(
            TaskBinding(
                Task(
                    1,
                    "This is my first task",
                    "",
                    false,
                    Reminder(),
                    System.currentTimeMillis()
                )
            ),
            TaskBinding(
                Task(
                    2,
                    "This is my second task",
                    "",
                    false,
                    Reminder(),
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
            itemAdapter.remove(position)
        } else {
            itemAdapter.remove(position)
        }
    }
}