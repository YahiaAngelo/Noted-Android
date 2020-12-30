package com.noted.noted.view.bindItem

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.noted.noted.R
import com.noted.noted.databinding.ItemNoteBinding
import com.noted.noted.model.Note
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class NoteBinding(var note: Note) : AbstractBindingItem<ItemNoteBinding>() {

    lateinit var noteCard:MaterialCardView
    lateinit var noteTitle:TextView
    lateinit var noteBody:TextView
    lateinit var categoriesChipGroup: ChipGroup

    override var identifier: Long  = note.id
    override val type: Int
        get() = R.id.fastadapter_item


    override fun bindView(binding: ItemNoteBinding, payloads: List<Any>) {
        val context = binding.root.context
        noteCard = binding.noteCard
        noteTitle = binding.noteTitle
        noteBody = binding.noteBody
        categoriesChipGroup = binding.noteChipGroup
        val markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(ResourcesCompat.getColor(context.resources, R.color.primary, context.theme), ResourcesCompat.getColor(context.resources, R.color.primary, context.theme), ResourcesCompat.getColor(context.resources, R.color.background, context.theme)))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _ -> visitor.forceNewLine() }
                }
            })
            .build()
        if (!note.colorHex.isEmpty()){
            binding.noteCard.setCardBackgroundColor(Color.parseColor(note.colorHex))
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                binding.noteCard.outlineAmbientShadowColor = Color.parseColor(note.colorHex)
                binding.noteCard.outlineSpotShadowColor = Color.parseColor(note.colorHex)
            }
        }else{
            binding.noteCard.setCardBackgroundColor(context.resources.getColorStateList(note.color, context.theme))
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                binding.noteCard.outlineAmbientShadowColor = context.resources.getColor(note.color, context.theme)
                binding.noteCard.outlineSpotShadowColor = context.resources.getColor(note.color, context.theme)
            }
        }

        binding.noteTitle.text = note.title
        markwon.setMarkdown(noteBody, note.body)
        noteBody.setOnClickListener {
            noteCard.performClick()
        }
        noteBody.setOnLongClickListener {
            noteCard.performLongClick()
        }
        binding.noteChipGroup.removeAllViews()
        for (category in note.categories!!){
            val chip = Chip(context)
            if (!note.colorHex.isEmpty()){
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(note.colorHex))
            }else{
                chip.chipBackgroundColor = context.resources.getColorStateList(note.color, context.theme)
            }
            chip.chipStrokeWidth = 2F
            chip.chipStrokeColor = context.resources.getColorStateList(R.color.text_primary, context.theme)
            chip.text = category.title
            chip.id = category.id.toInt()
            chip.setOnClickListener{
                noteCard.performClick()
            }
            binding.noteChipGroup.addView(chip)
        }

        if (note.isFavorite){
            binding.noteFavorite.visibility = View.VISIBLE
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemNoteBinding {
        return ItemNoteBinding.inflate(inflater, parent, false)
    }
}