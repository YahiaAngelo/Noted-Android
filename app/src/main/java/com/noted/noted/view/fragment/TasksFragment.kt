package com.noted.noted.view.fragment

import android.app.ActivityOptions
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.mikepenz.fastadapter.ClickListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.noted.noted.R
import com.noted.noted.databinding.FragmentTasksBinding
import com.noted.noted.model.NoteCategory
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.repositories.TaskRepo
import com.noted.noted.utils.ReminderWorker
import com.noted.noted.utils.Utils
import com.noted.noted.view.activity.TaskViewActivity
import com.noted.noted.view.bindItem.TaskBinding
import com.noted.noted.view.customView.SimpleSwipeCallback
import com.noted.noted.view.customView.SimpleSwipeDragCallback
import com.noted.noted.viewmodel.TasksFragmentViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels.*
import java.util.*


class TasksFragment : BaseFragment(), ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {


    private lateinit var binding: FragmentTasksBinding
    private val itemAdapter = ItemAdapter<TaskBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var touchCallback: SimpleDragCallback
    private lateinit var touchHelper: ItemTouchHelper
    private var addTaskDialog: BottomSheetDialog? = null
    private val viewModel: TasksFragmentViewModel by viewModel()
    private val taskRepo: TaskRepo by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTasksBinding.inflate(inflater, container, false)

        initAdapter()
        touchCallback = SimpleSwipeDragCallback(
            this,
            this,
            ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_delete,
                requireContext().theme
            )!!,
            ItemTouchHelper.LEFT,
            requireContext().resources.getColor(R.color.card_red, requireContext().theme)
        ).withBackgroundSwipeRight(
            requireContext().resources.getColor(
                R.color.primary,
                requireContext().theme
            )
        )
            .withLeaveBehindSwipeRight(
                ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.ic_add,
                    requireContext().theme
                )!!
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
        fastAdapter.onClickListener = object : ClickListener<TaskBinding> {
            override fun invoke(
                v: View?,
                adapter: IAdapter<TaskBinding>,
                item: TaskBinding,
                position: Int
            ): Boolean {
                val intent = Intent(activity, TaskViewActivity::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    item.taskCard,
                    "task_shared_element_container"
                )
                intent.putExtra("task", wrap(item.task))
                startActivity(intent, options.toBundle())

                return true
            }

        }
        viewModel.getTasks().observe(viewLifecycleOwner) {
            binding.tasksRecycler.post {
                itemAdapter.setNewList(it)
                binding.tasksPlaceholder.visibility = if (itemAdapter.adapterItemCount > 0)  View.GONE else View.VISIBLE

            }
        }
    }

    private fun initTasksAdd() {
        val currentCalendar = Calendar.getInstance()
        var selectedCalendar: Calendar? = null
        val view = layoutInflater.inflate(R.layout.task_add_bottom_sheet, null)
        addTaskDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetMenuTheme)
        addTaskDialog!!.setContentView(view)
        val titleEditText: TextInputEditText = view.findViewById(R.id.task_add_editText)
        val descEditText: TextInputEditText = view.findViewById(R.id.task_add_desc_editText)
        val descButton: MaterialButton = view.findViewById(R.id.task_add_desc)
        val reminderButton: MaterialButton = view.findViewById(R.id.task_add_reminder)
        val repeatButton: MaterialButton = view.findViewById(R.id.task_add_repeat)
        val saveButton: MaterialButton = view.findViewById(R.id.task_add_save)

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
            if (reminderButton.isChecked) {
                reminderButton.performClick()
            }
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            selectedCalendar = Calendar.getInstance()
            selectedCalendar!!.time = Date(it)

            TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    selectedCalendar!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar!!.set(Calendar.MINUTE, minute)
                    selectedCalendar!!.set(Calendar.SECOND, 0)
                },
                currentCalendar.get(Calendar.HOUR_OF_DAY),
                currentCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        reminderButton.setOnClickListener {
            if (reminderButton.isChecked) {
                materialDatePicker.showNow(childFragmentManager, materialDatePicker.tag)
            }
        }

        saveButton.setOnClickListener {
            if (titleEditText.text!!.isNotEmpty()) {

                val task = Task(
                    UUID.randomUUID().mostSignificantBits,
                    titleEditText.text.toString(),
                    descEditText.text.toString(),
                    false,
                    System.currentTimeMillis()
                )
                if (selectedCalendar != null) {
                    val reminder = Reminder(
                        UUID.randomUUID().mostSignificantBits,
                        selectedCalendar!!.time.time, repeatButton.isChecked
                    )
                    task.reminder = reminder
                    ReminderWorker.setReminder(task.title, task.reminder!!.id, task.id, task.reminder!!.date, task.reminder!!.repeat, requireContext())
                    //alarmUtils.setAlarm(task, requireContext())
                }

                taskRepo.addTask(task)
                update()
                addTaskDialog!!.dismiss()
                initTasksAdd()
            }

        }


    }

    fun showTasksAdd() {
        if (addTaskDialog != null) {
            addTaskDialog!!.show()
        }
    }

    private fun update() {
        itemAdapter.itemFilter.filterPredicate = { item: TaskBinding, constraint: CharSequence? ->
            item.taskTitle.text.contains(constraint.toString(), true)
        }
        itemAdapter.filter("")
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
            if (task.reminder != null) {
                ReminderWorker.cancel(task.reminder!!.id, requireContext())
            }
            taskRepo.deleteTask(task)

        } else {
            fastAdapter.notifyItemChanged(position)
            Utils.showCategories(requireContext(), layoutInflater, object :
                Utils.Companion.OnSelectedCategory {
                override fun onSelected(noteCategory: NoteCategory) {
                    taskRepo.addCategoryToTask(itemAdapter.getAdapterItem(position).task, noteCategory)
                    fastAdapter.notifyItemChanged(position)
                }
            })
        }
    }


    override fun filterCategories(categoryId : Int) {
        itemAdapter.itemFilter.filterPredicate = { item: TaskBinding, constraint: CharSequence? ->
            item.categoriesGroup.findViewById<Chip>(Integer.parseInt(constraint.toString())) != null
        }
        itemAdapter.filter(categoryId.toString())
    }



    override fun refresh() {
        update()
    }

    override fun filterItem(string: String) {
        itemAdapter.itemFilter.filterPredicate = { item: TaskBinding, constraint: CharSequence? ->
            item.taskTitle.text.contains(constraint.toString(), true)
        }
        itemAdapter.filter(string)
    }
}