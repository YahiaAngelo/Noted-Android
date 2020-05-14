package com.noted.noted.view.bindItem

import android.animation.ValueAnimator
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.noted.noted.R
import com.noted.noted.databinding.ItemTaskBinding
import com.noted.noted.model.Task

class TaskBinding(var task: Task) : AbstractBindingItem<ItemTaskBinding>(){

    lateinit var taskTitle: TextView
    lateinit var taskCheckBox:MaterialCheckBox
    override var identifier: Long = task.id
    override val type: Int
        get() = R.id.task_item_id

    override fun bindView(binding: ItemTaskBinding, payloads: List<Any>) {
        val context = binding.root.context
        taskTitle = binding.taskTitle
        taskCheckBox = binding.taskCheckbox
        binding.taskTitle.text = task.title
        if (task.checked){
            binding.taskCheckbox.isChecked = true
            val spannableStringBuilder = SpannableStringBuilder(binding.taskTitle.text)
            spannableStringBuilder.setSpan(StrikethroughSpan(), 0, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.taskTitle.text = spannableStringBuilder
        }
        binding.taskCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                binding.taskTitle.startStrikeThroughAnimation()
            }else{
                binding.taskTitle.reverseStrikeThroughAnimation()
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

    fun checkTask(){
        if (taskCheckBox.isChecked){
            taskCheckBox.isChecked = false
            taskTitle.reverseStrikeThroughAnimation()
        }else{
            taskCheckBox.isChecked = true
            taskTitle.startStrikeThroughAnimation()
        }
    }
}