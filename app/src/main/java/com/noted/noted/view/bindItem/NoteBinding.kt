package com.noted.noted.view.bindItem

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.noted.noted.R
import com.noted.noted.databinding.ItemNoteBinding
import com.noted.noted.model.Note
import io.noties.markwon.Markwon

class NoteBinding(var note: Note) : AbstractBindingItem<ItemNoteBinding>() {

    lateinit var noteCard:MaterialCardView
    lateinit var noteTitle:TextView
    lateinit var noteBody:TextView

    override var identifier: Long
        get() = note.id
        set(value) {}

    override val type: Int
        get() = R.id.fastadapter_item


    override fun bindView(binding: ItemNoteBinding, payloads: List<Any>) {
        val context = binding.root.context
        noteCard = binding.noteCard
        noteTitle = binding.noteTitle
        noteBody = binding.noteBody
        val markwon = Markwon.create(context)
        binding.noteCard.setCardBackgroundColor(context.resources.getColorStateList(note.color, context.theme))
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.noteCard.outlineAmbientShadowColor = context.resources.getColor(note.color, context.theme)
            binding.noteCard.outlineSpotShadowColor = context.resources.getColor(note.color, context.theme)
        }
        binding.noteTitle.text = note.title
        markwon.setMarkdown(noteBody, note.body)
        binding.noteChipGroup.removeAllViews()
        for (category in note.categories!!){
            val chip = Chip(context)
            chip.chipBackgroundColor = context.resources.getColorStateList(note.color, context.theme)
            chip.chipStrokeWidth = 2F
            chip.chipStrokeColor = context.resources.getColorStateList(R.color.text_primary, context.theme)
            chip.text = category.title
            chip.id = category.id.toInt()
            chip.setOnClickListener{
                noteCard.performClick()
            }
            binding.noteChipGroup.addView(chip)
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemNoteBinding {
        return ItemNoteBinding.inflate(inflater, parent, false)
    }
}