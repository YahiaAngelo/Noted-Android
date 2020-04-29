package com.noted.noted.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.noted.noted.R
import com.noted.noted.databinding.ActivityNoteAddBinding
import com.noted.noted.model.Note
import com.noted.noted.model.NoteCategory
import io.realm.Realm
import io.realm.RealmList
import org.parceler.Parcels
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates

lateinit var binding: ActivityNoteAddBinding
lateinit var bottomSheet: BottomSheetDialog
lateinit var categoriesBottomSheet: BottomSheetDialog
lateinit var categoriesList: RealmList<NoteCategory>
lateinit var note: Note
var newColor by Delegates.notNull<Int>()
var realm: Realm = Realm.getDefaultInstance()
var noteId by Delegates.notNull<Long>()

class NoteAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        findViewById<View>(android.R.id.content).transitionName = "note_shared_element_container"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        binding = ActivityNoteAddBinding.inflate(layoutInflater)
        noteId = UUID.randomUUID().mostSignificantBits
        categoriesList = RealmList()
        newColor = R.color.background
        if (intent.getParcelableExtra<Parcelable>("note") != null) {
            note = Parcels.unwrap(intent.getParcelableExtra("note"))
            noteId = note.id
            newColor = note.color
            binding.activityNoteAddContainer.setBackgroundColor(resources.getColor(newColor))
            binding.noteTitleEditText.setText(note.title)
            binding.noteBodyEditText.setText(note.body)
            val date =
                SimpleDateFormat("d MMM HH:mm aaa", Locale.getDefault()).format(Date(note.date))
            binding.noteDate.text = "Edited $date"
            window.statusBarColor = resources.getColor(newColor)
            window.navigationBarColor = resources.getColor(newColor)
            binding.noteAddCategoryChip.chipBackgroundColor =
                resources.getColorStateList(newColor).withAlpha(200)
            categoriesList = note.categories

        }

        setSupportActionBar(binding.activityNoteAddToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        for (category in categoriesList) {
            addCategoryChip(category)
        }

        initBottomSheet()
        initCategories()
        binding.noteAddCategoryChip.setOnClickListener {
            categoriesBottomSheet.show()
        }
        binding.activityNoteAddToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.note_add_more -> bottomSheet.show()
                R.id.note_add_save -> saveNote()
            }

            true
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_add_menu, menu);
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveNote() {
        if (binding.noteTitleEditText.text!!.isNotEmpty()) {
            realm = Realm.getDefaultInstance()
            realm.use { realm ->
                val note = Note(
                    noteId,
                    binding.noteTitleEditText.text.toString(),
                    binding.noteBodyEditText.text.toString(),
                    System.currentTimeMillis(),
                    newColor,
                    categoriesList
                )
                realm.beginTransaction()
                realm.copyToRealmOrUpdate(note)
                realm.commitTransaction()
                onBackPressed()
            }
        }
    }

    private fun initBottomSheet() {
        val view = layoutInflater.inflate(R.layout.note_add_bottom_sheet, null)
        view.setBackgroundColor(resources.getColor(newColor))
        bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        bottomSheet.setContentView(view)
        val listView: ListView = view.findViewById(R.id.bottom_sheet_listView)
        val chipGroup: ChipGroup = view.findViewById(R.id.bottom_sheet_chipGroup)
        val itemsList: MutableList<HashMap<String, String>> =
            emptyList<HashMap<String, String>>().toMutableList()
        val itemListTitles = arrayOf("Delete", "Copy", "Share")
        val itemListImages =
            arrayOf(R.drawable.ic_delete, R.drawable.ic_file_copy, R.drawable.ic_share)
        for ((position, item) in itemListTitles.withIndex()) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = item
            hm["listview_image"] = itemListImages[position].toString()
            itemsList.add(hm)
        }
        val from = arrayOf("listview_title", "listview_image")
        val to = arrayOf(R.id.list_text, R.id.list_image)
        val simpleAdapter =
            SimpleAdapter(this, itemsList, R.layout.simple_list_layout, from, to.toIntArray())
        listView.adapter = simpleAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> deleteNote()
            }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip_background -> newColor = R.color.background
                R.id.chip_blue -> newColor = R.color.card_blue
                R.id.chip_green -> newColor = R.color.card_green
                R.id.chip_purple -> newColor = R.color.card_purple
                R.id.chip_red -> newColor = R.color.card_red
                R.id.chip_violet -> newColor = R.color.card_violet
                R.id.chip_yellow -> newColor = R.color.card_yellow
            }
            binding.activityNoteAddContainer.setBackgroundColor(resources.getColor(newColor, theme))
            window.statusBarColor = resources.getColor(newColor, theme)
            binding.noteAddCategoryChip.chipBackgroundColor =
                resources.getColorStateList(newColor, theme)
            view.setBackgroundColor(resources.getColor(newColor, theme))
        }


    }

    private fun initCategories() {
        val dbCategories = realm.where(NoteCategory::class.java).findAll()
        val view = layoutInflater.inflate(R.layout.simple_listview_layout, null)
        view.setBackgroundColor(resources.getColor(newColor, theme))
        categoriesBottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        categoriesBottomSheet.setContentView(view)
        val listView: ListView = view.findViewById(R.id.simple_listView)
        val itemsList: MutableList<HashMap<String, String>> =
            emptyList<HashMap<String, String>>().toMutableList()
        val itemListTitles = arrayOf("Add category").toMutableList()
        val itemListImages = arrayOf(R.drawable.ic_add).toMutableList()
        for (category in dbCategories) {

            itemListTitles.add(category.title)
            itemListImages.add(R.drawable.ic_label)

        }
        for ((position, item) in itemListTitles.withIndex()) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = item
            hm["listview_image"] = itemListImages[position].toString()
            itemsList.add(hm)
        }
        val from = arrayOf("listview_title", "listview_image")
        val to = arrayOf(R.id.list_text, R.id.list_image)
        val simpleAdapter =
            SimpleAdapter(this, itemsList, R.layout.simple_list_layout, from, to.toIntArray())
        listView.adapter = simpleAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Add category")
                        .setView(R.layout.dialog_edittext_layout)
                        .setPositiveButton("Add") { dialog, _ ->
                            val text =
                                (dialog as? AlertDialog)?.findViewById<EditText>(R.id.dialog_editText)?.text?.toString()
                            if (text != null) {
                                val noteCategory =
                                    NoteCategory(UUID.randomUUID().mostSignificantBits, text)
                                saveCategory(noteCategory)
                                categoriesBottomSheet.dismiss()
                                initCategories()
                                categoriesBottomSheet.show()
                                dialog.dismiss()
                            }

                        }.setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                else -> {
                    val noteCategory = dbCategories[position - 1]
                    categoriesList.add(noteCategory)
                    addCategoryChip(noteCategory!!)
                    categoriesBottomSheet.dismiss()
                }
            }
        }

    }


    private fun addCategoryChip(noteCategory: NoteCategory) {
       if (binding.chipGroup.findViewById<Chip>(noteCategory.id.toInt()) == null){
           val chip = Chip(this)
           chip.id = noteCategory.id.toInt()
           chip.chipBackgroundColor = resources.getColorStateList(newColor, theme).withAlpha(200)
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

    private fun saveCategory(noteCategory: NoteCategory) {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            realm.beginTransaction()
            realm.copyToRealm(noteCategory)
            realm.commitTransaction()
        }
    }

    private fun deleteNote() {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            val note = realm.where(Note::class.java).equalTo("id", noteId).findFirst()
            realm.beginTransaction()
            note!!.deleteFromRealm()
            realm.commitTransaction()
            bottomSheet.dismiss()
            finish()
        }
    }


}