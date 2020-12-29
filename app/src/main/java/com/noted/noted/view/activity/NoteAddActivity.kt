package com.noted.noted.view.activity

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.noted.noted.R
import com.noted.noted.databinding.ActivityNoteAddBinding
import com.noted.noted.model.Note
import com.noted.noted.model.NoteCategory
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.utils.Utils
import io.realm.RealmList
import org.koin.android.ext.android.inject
import org.parceler.Parcels
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class NoteAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityNoteAddBinding
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var categoriesBottomSheet: BottomSheetDialog
    private lateinit var categoriesList: RealmList<NoteCategory>
    private var autoSave by Delegates.notNull<Boolean>()
    private var note: Note? = null
    private var newColor by Delegates.notNull<Int>()
    private var hexColor : String? = null
    private var noteId by Delegates.notNull<Long>()
    private val noteRepo: NoteRepo by inject()
    private var isTextChanged = false
    private var isFavorite = false
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        findViewById<View>(android.R.id.content).transitionName = "note_shared_element_container"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }
        binding = ActivityNoteAddBinding.inflate(layoutInflater)


        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        noteId = UUID.randomUUID().mostSignificantBits
        categoriesList = RealmList()
        newColor = R.color.background
        if (intent.getParcelableExtra<Parcelable>("note") != null) {
            note = Parcels.unwrap(intent.getParcelableExtra("note"))
            noteId = note!!.id
            newColor = note!!.color
            isFavorite = note!!.isFavorite
            binding.noteTitleEditText.setText(note!!.title)
            binding.noteBodyEditText.renderMD(note!!.body)
            val date =
                SimpleDateFormat("d MMM HH:mm aaa", Locale.getDefault()).format(Date(note!!.date))
            val editedString = "${resources.getString(R.string.edited)} $date"
            binding.noteDate.text = editedString
            categoriesList = note!!.categories
            if (!note!!.colorHex.isNullOrEmpty()){
                hexColor = note?.colorHex
                updateColorsFromHex(null)
            }else{
                updateColors(null)
            }

        }
        setSupportActionBar(binding.activityNoteAddToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        setupMarkwon()

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
                R.id.note_add_favorite -> {
                    isFavorite = !isFavorite
                    isTextChanged = true
                    it.icon = if (isFavorite) ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite, theme) else ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite_border, theme)
                }
            }

            true
        }

        autoSave = sharedPref.getBoolean("notes_auto_save", false)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_add_menu, menu);
        if (isFavorite){
            menu!!.findItem(R.id.note_add_favorite).icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite, theme)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveNote() {
        if (binding.noteTitleEditText.text!!.isNotEmpty()) {
            binding.noteStylesBar.visibility = View.GONE
                val note = Note(
                    noteId,
                    binding.noteTitleEditText.text.toString(),
                    binding.noteBodyEditText.getMD(),
                    System.currentTimeMillis(),
                    newColor,
                    categoriesList
                )
            if (hexColor!= null){
                note.colorHex = hexColor as String
            }
                note.isFavorite = isFavorite
                noteRepo.addNote(note)
                finish()

        }else{
            Toast.makeText(this, "Please add a title to your note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initBottomSheet() {
        val view = layoutInflater.inflate(R.layout.note_add_bottom_sheet, null)
        if (!hexColor.isNullOrEmpty()){
            view.setBackgroundColor(parseColor(hexColor))
        }else{
            view.setBackgroundColor(resources.getColor(newColor, theme))
        }
        bottomSheet = BottomSheetDialog(this, R.style.BottomSheetMenuTheme)
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
        listView.setOnItemClickListener { _, _, position, _ ->
            if (note != null){
                when (position) {
                    0 -> deleteNote()
                    1 -> Utils.copyToClipboard(this,"${binding.noteTitleEditText.text.toString()}\n\n${binding.noteBodyEditText.text.toString()}")
                    2 -> if(note != null){Utils.shareNote(note!!, this, layoutInflater)}
                }
            }
            bottomSheet.dismiss()
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
                R.id.chip_colorize ->{
                    val colorPickerDialog = ColorPickerDialog.newBuilder()
                        .setColor(if (hexColor.isNullOrEmpty()) resources.getColor(note!!.color, theme) else parseColor(hexColor))
                        .setShowAlphaSlider(true)
                        .create()
                    colorPickerDialog.setColorPickerDialogListener(object: ColorPickerDialogListener{
                        override fun onColorSelected(dialogId: Int, color: Int) {
                            hexColor = "#${Integer.toHexString(color)}"
                            
                            updateColorsFromHex(view)
                        }

                        override fun onDialogDismissed(dialogId: Int) {

                        }

                    })

                    colorPickerDialog.showNow(supportFragmentManager, "colorPicker")


                }
            }
            isTextChanged = true
            updateColors(view)
        }



    }

    private fun initCategories() {
        val dbCategories = noteRepo.getCategories()
        val view = layoutInflater.inflate(R.layout.simple_listview_layout, null)
        view.setBackgroundColor(resources.getColor(newColor, theme))
        categoriesBottomSheet = BottomSheetDialog(this, R.style.BottomSheetMenuTheme)
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

        listView.setOnItemClickListener { _, _, position, _ ->
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

        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position > 0){
                val noteCategory = dbCategories[position - 1]!!
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete category")
                    .setMessage("Do you want to delete ${noteCategory.title} category ?")
                    .setNeutralButton("Delete"
                    ) { dialog, _ ->
                        itemsList.removeAt(position)
                        noteRepo.deleteCategory(noteCategory)
                        simpleAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    .setNegativeButton(this.resources.getString(android.R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            true
        }

    }


    private fun addCategoryChip(noteCategory: NoteCategory) {
        if (binding.chipGroup.findViewById<Chip>(noteCategory.id.toInt()) == null) {
            val chip = Chip(this)
            chip.id = noteCategory.id.toInt()
            chip.chipBackgroundColor = resources.getColorStateList(newColor, theme).withAlpha(200)
            chip.chipStrokeWidth = 2F
            chip.chipStrokeColor = resources.getColorStateList(R.color.text_primary, theme)
            chip.text = noteCategory.title
            chip.setTextColor(ResourcesCompat.getColor(resources, R.color.text_primary, theme))
            chip.closeIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_close, theme)
            chip.closeIconTint = resources.getColorStateList(R.color.text_secondary, theme)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                categoriesList.remove(noteCategory)
                binding.chipGroup.removeView(chip)
            }
            binding.chipGroup.addView(chip)
        }


    }

    private fun updateColors(view: View?){
        binding.activityNoteAddContainer.setBackgroundColor(resources.getColor(newColor, theme))
        window.statusBarColor = resources.getColor(newColor, theme)
        window.navigationBarColor = resources.getColor(newColor, theme)
        binding.noteAddCategoryChip.chipBackgroundColor =
            resources.getColorStateList(newColor, theme)
        view?.setBackgroundColor(resources.getColor(newColor, theme))
    }
    private fun updateColorsFromHex(view: View?){
        binding.activityNoteAddContainer.setBackgroundColor(parseColor(hexColor))
        window.statusBarColor = parseColor(hexColor)
        window.navigationBarColor = parseColor(hexColor)
        binding.noteAddCategoryChip.chipBackgroundColor =
            ColorStateList.valueOf(parseColor(hexColor))
        view?.setBackgroundColor(parseColor(hexColor))
        val isInvertColor = sharedPref.getBoolean("notes_adaptive_text", false)
        if (isInvertColor){
            val invertedColor = Utils.invertColor(hexColor!!.substring(1,hexColor!!.length))
            binding.noteTitleEditText.setTextColor(invertedColor)
            binding.noteBodyEditText.setTextColor(invertedColor)
        }
    }

    private fun saveCategory(noteCategory: NoteCategory) {
       noteRepo.saveCategory(noteCategory)
    }

    private fun deleteNote() {
        noteRepo.deleteNote(noteId)
        bottomSheet.dismiss()
        finish()
    }

    private fun setupMarkwon() {

        binding.noteBodyEditText.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.noteStylesBar.visibility = View.VISIBLE
                binding.noteDate.visibility = View.GONE
                binding.noteBodyEditText.setStylesBar(binding.noteStylesBar)

            }else{
                binding.noteStylesBar.visibility = View.GONE
                binding.noteDate.visibility = View.VISIBLE
            }
        }

        binding.noteBodyEditText.taskBoxBackgroundColor = ResourcesCompat.getColor(resources, R.color.background, theme)
        binding.noteBodyEditText.taskBoxColor = ResourcesCompat.getColor(resources, R.color.primary, theme)
    }

    private fun showSaveConfirmDialog(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Discard changes")
            .setMessage("Do you want to exit without saving ?")
            .setPositiveButton("Save"
            ) { _, _ -> saveNote() }
            .setNegativeButton("Discard") { _, _ -> finish() }
            .show()
    }
    private fun checkTextChanged(){
        if (note != null && note!!.body != binding.noteBodyEditText.getMD()){
            isTextChanged = true
        }
    }

    override fun onBackPressed() {
        checkTextChanged()
        if (isTextChanged){
            if (autoSave){
                saveNote()
            }else{
                showSaveConfirmDialog()
            }
        }else{
            super.onBackPressed()
        }


    }
}