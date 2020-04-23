package com.noted.noted.view.bindItem

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.noted.noted.R
import com.noted.noted.databinding.ItemNoteBinding
import com.noted.noted.model.Note

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
        noteCard = binding.noteCard
        noteTitle = binding.noteTitle
        noteBody = binding.noteBody
        binding.noteCard.setCardBackgroundColor(Color.parseColor(note.color))
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.noteCard.outlineAmbientShadowColor = Color.parseColor(note.color)
            binding.noteCard.outlineSpotShadowColor = Color.parseColor(note.color)
        }
        binding.noteTitle.text = note.title
        binding.noteBody.text = note.body
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemNoteBinding {
        return ItemNoteBinding.inflate(inflater, parent, false)
    }
}