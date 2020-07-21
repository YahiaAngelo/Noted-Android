package com.noted.noted.view.bindItem

import android.animation.ValueAnimator
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.noted.noted.R
import com.noted.noted.databinding.ItemTaskBinding
import com.noted.noted.model.Task
import com.noted.noted.utils.AlarmUtils
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*

class TaskBinding(var task: Task) : AbstractBindingItem<ItemTaskBinding>(){

    lateinit var taskTitle: TextView
    private lateinit var taskCheckBox:MaterialCheckBox
    override var identifier: Long = task.id

    override val type: Int
        get() = R.id.task_item_id

    override fun bindView(binding: ItemTaskBinding, payloads: List<Any>) {
        val context = binding.root.context
        taskTitle = binding.taskTitle
        if (task.desc.isNotEmpty()){
            binding.taskDesc.text = task.desc
            binding.taskDesc.visibility = View.VISIBLE
        }
        taskCheckBox = binding.taskCheckbox
        binding.taskTitle.text = task.title


        if (task.reminder != null){
            if(binding.taskChipGroup.findViewById<Chip>(task.reminder!!.id.toInt()) == null){
                val chip = Chip(context)
                chip.chipBackgroundColor = context.resources.getColorStateList(R.color.background, context.theme)
                chip.chipStrokeWidth = 6F
                chip.chipStrokeColor = context.resources.getColorStateList(R.color.card_stroke_color, context.theme)
                if (task.reminder!!.repeat){
                    chip.chipIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_repeat, context.theme)
                }else{
                    chip.chipIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_access_alarms_18dp, context.theme)
                }
                chip.chipIconTint = context.resources.getColorStateList(R.color.text_primary, context.theme)
                chip.setTextColor(context.resources.getColorStateList(R.color.text_primary, context.theme))
                val date = SimpleDateFormat("d MMM HH:mm aaa", Locale.getDefault()).format(Date(task.reminder!!.date))
                chip.text = date
                chip.id = task.reminder!!.id.toInt()
                binding.taskChipGroup.addView(chip)
            }
        }

        for (category in task.noteCategories){
            if (binding.taskChipGroup.findViewById<Chip>(category.id.toInt()) == null){
                val chip = Chip(context)
                chip.chipBackgroundColor = context.resources.getColorStateList(R.color.background, context.theme)
                chip.chipStrokeWidth = 6F
                chip.chipStrokeColor = context.resources.getColorStateList(R.color.card_stroke_color, context.theme)
                chip.setTextColor(context.resources.getColorStateList(R.color.text_primary, context.theme))
                chip.text = category.title
                chip.id = category.id.toInt()
                binding.taskChipGroup.addView(chip)
            }
        }


        if (task.checked){
            binding.taskCheckbox.isChecked = true
            binding.root.setCardForegroundColor(context.resources.getColorStateList(R.color.card_stroke_color, context.theme).withAlpha(60))
            val spannableStringBuilder = SpannableStringBuilder(binding.taskTitle.text)
            spannableStringBuilder.setSpan(StrikethroughSpan(), 0, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (task.desc.isNotEmpty()){
                val descSpannableStringBuilder = SpannableStringBuilder(binding.taskDesc.text)
                descSpannableStringBuilder.setSpan(StrikethroughSpan(), 0, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.taskTitle.text = spannableStringBuilder
        }
        binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val realm = Realm.getDefaultInstance()
            val alarmUtils = AlarmUtils()
            if (isChecked){
                binding.taskTitle.startStrikeThroughAnimation()
                if (task.desc.isNotEmpty()){
                    binding.taskDesc.startStrikeThroughAnimation()
                }
                binding.root.setCardForegroundColor(context.resources.getColorStateList(R.color.card_stroke_color, context.theme).withAlpha(60))
                if (task.reminder != null){
                    alarmUtils.cancelAlarm(task.reminder!!, context)
                }
            }else{
                binding.root.setCardForegroundColor(context.resources.getColorStateList(R.color.transparent, context.theme))
                binding.taskTitle.reverseStrikeThroughAnimation()
                if (task.desc.isNotEmpty()){
                    binding.taskDesc.reverseStrikeThroughAnimation()
                }
                if (task.reminder != null){
                    alarmUtils.setAlarm(task, context)
                }
            }
            realm.use {
                realm.beginTransaction()
                task.checked = isChecked
                realm.commitTransaction()
            }
        }

    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemTaskBinding {
        return ItemTaskBinding.inflate(inflater, parent, false)
    }

    private fun TextView.startStrikeThroughAnimation(): ValueAnimator {
        val span = SpannableString(text)
        val strikeSpan = StrikethroughSpan()
        val animator = ValueAnimator.ofInt(text.length)
        animator.addUpdateListener {
            span.setSpan(strikeSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = span
            invalidate()
        }
        animator.start()
        return animator
    }

    private fun TextView.reverseStrikeThroughAnimation(): ValueAnimator {
        val span = SpannableString(text.toString())
        val strikeSpan = StrikethroughSpan()
        val animator = ValueAnimator.ofInt(text.length, 0)
        animator.addUpdateListener {
            span.setSpan(strikeSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = span
            invalidate()
        }
        animator.start()
        return animator
    }

}