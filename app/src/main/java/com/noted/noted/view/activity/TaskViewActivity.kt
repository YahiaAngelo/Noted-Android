package com.noted.noted.view.activity

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.noted.noted.R
import com.noted.noted.databinding.ActivityTaskViewBinding
import com.noted.noted.model.NoteCategory
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.repositories.TaskRepo
import com.noted.noted.utils.ReminderWorker
import com.noted.noted.utils.Utils
import io.realm.RealmList
import org.koin.android.ext.android.inject
import org.parceler.Parcels
import java.text.SimpleDateFormat
import java.util.*

class TaskViewActivity : AppCompatActivity() {
    private lateinit var binding:ActivityTaskViewBinding
    private lateinit var task : Task
    private val taskRepo: TaskRepo by inject()
    private lateinit var categoriesList: RealmList<NoteCategory>
    private var reminder: Reminder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        findViewById<View>(android.R.id.content).transitionName = "task_shared_element_container"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background, theme))
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }
        binding = ActivityTaskViewBinding.inflate(layoutInflater)
        setSupportActionBar(binding.activityTaskViewToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        task = Parcels.unwrap(intent.getParcelableExtra("task"))

        binding.taskExtendedFab.visibility = if (task.checked) View.GONE else View.VISIBLE
        binding.taskTitleEdittext.setText(task.title)
        if (task.desc.isNotEmpty()){
            binding.taskDescEdittext.setText(task.desc)
        }

        if (task.reminder != null){
            reminder = task.reminder!!
            checkReminder()
        }

        binding.taskViewReminderChip.setOnClickListener {
            if(reminder == null){
                Utils.showCalendar(this, supportFragmentManager, object : Utils.Companion.OnSelectedCalendar{
                    override fun onSelected(calendar: Calendar) {
                        reminder = Reminder(
                            UUID.randomUUID().mostSignificantBits,
                            calendar.time.time, false
                        )
                        task.reminder = reminder
                        ReminderWorker.setReminder(task.title, task.reminder!!.id, task.id, task.reminder!!.date, task.reminder!!.repeat, this@TaskViewActivity)
                        checkReminder()
                    }

                })
            }
        }

        categoriesList = task.noteCategories

        for (category in categoriesList){
            addCategoryChip(category)
        }

        binding.taskViewCategoryChip.setOnClickListener {
            Utils.showCategories(this, layoutInflater, object : Utils.Companion.OnSelectedCategory{
                override fun onSelected(noteCategory: NoteCategory) {
                    categoriesList.add(noteCategory)
                    addCategoryChip(noteCategory)
                }

            })
        }

        binding.activityTaskViewToolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.task_view_save -> saveTask()
            }

            true
        }

        binding.taskExtendedFab.setOnClickListener {
            task.checked = true
            saveTask()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.taskExtendedFab.outlineAmbientShadowColor = binding.taskExtendedFab.backgroundTintList!!.defaultColor
            binding.taskExtendedFab.outlineSpotShadowColor = binding.taskExtendedFab.backgroundTintList!!.defaultColor
        }

    }

    private fun saveTask(){
        val newTask = Task(task.id, binding.taskTitleEdittext.text.toString(), binding.taskDescEdittext.text.toString(), task.checked, task.date)
        newTask.noteCategories = categoriesList
        if (reminder != null){
            newTask.reminder = reminder
        }
        taskRepo.updateTask(newTask)
        finish()
    }

    private fun checkReminder(){
        if (reminder != null){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val datePattern = if (sharedPref.getBoolean("hour_format", false)) "d MMM HH:mm aaa" else "d MMM hh:mm aaa"
            val date = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(task.reminder!!.date))
            binding.taskViewReminderChip.text = date
            if (task.reminder!!.repeat){
                binding.taskViewReminderChip.chipIcon = resources.getDrawable(R.drawable.ic_repeat, theme)
            }
            binding.taskViewReminderChip.isCloseIconVisible = true
            binding.taskViewReminderChip.setOnCloseIconClickListener {
                binding.taskViewReminderChip.text = "Add Reminder"
                binding.taskViewReminderChip.chipIcon = resources.getDrawable(R.drawable.ic_access_alarms, theme)
                binding.taskViewReminderChip.isCloseIconVisible = false
                ReminderWorker.cancel(reminder!!.id, this)
                reminder = null
            }
        }
    }

    private fun addCategoryChip(noteCategory: NoteCategory) {
        if (binding.chipGroup.findViewById<Chip>(noteCategory.id.toInt()) == null) {
            val chip = Chip(this)
            chip.id = noteCategory.id.toInt()
            chip.chipStrokeWidth = 2F
            chip.chipStrokeColor = resources.getColorStateList(R.color.text_primary, theme)
            chip.text = noteCategory.title
            chip.setTextColor(resources.getColor(R.color.text_primary, theme))
            chip.closeIcon = resources.getDrawable(R.drawable.ic_close, theme)
            chip.closeIconTint = resources.getColorStateList(R.color.text_secondary, theme)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                categoriesList.remove(noteCategory)
                binding.chipGroup.removeView(chip)
            }
            binding.chipGroup.addView(chip)
        }


    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.task_view_menu, menu);
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}