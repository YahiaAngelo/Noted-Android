package com.noted.noted.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.noted.noted.R
import com.noted.noted.databinding.ActivityNoteAddBinding
import com.noted.noted.model.Note
import io.realm.Realm
import java.util.*
import kotlin.collections.HashMap

lateinit var binding: ActivityNoteAddBinding
lateinit var bottomSheet : BottomSheetDialog
var newColor: Int = R.color.background
var realm:Realm = Realm.getDefaultInstance()
var noteId = UUID.randomUUID().mostSignificantBits

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
        //Probably not the best way to check for intent extras but meh
        if (intent.getStringExtra("title")!= null){
            noteId = intent.getLongExtra("id", UUID.randomUUID().mostSignificantBits)
            val color = intent.getIntExtra("color", R.color.background)
            newColor = color
            binding.activityNoteAddContainer.setBackgroundColor(resources.getColor(color))
            binding.noteTitleEditText.setText(intent.getStringExtra("title"))
            binding.noteBodyEditText.setText(intent.getStringExtra("body"))
            window.statusBarColor = resources.getColor(color)
            window.navigationBarColor = resources.getColor(color)
            binding.noteAddCategoryChip.chipBackgroundColor = resources.getColorStateList(color)
        }
        setSupportActionBar(binding.activityNoteAddToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)


        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initBottomSheet()
        binding.activityNoteAddToolbar.setOnMenuItemClickListener {
           when(it.itemId){
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

    private fun saveNote(){
        if (binding.noteTitleEditText.text!!.isNotEmpty()){
            realm = Realm.getDefaultInstance()
            realm.use { realm ->
                val note = Note(
                    noteId,binding.noteTitleEditText.text.toString(), binding.noteBodyEditText.text.toString(), System.currentTimeMillis(),
                    newColor)
                realm.beginTransaction()
                realm.copyToRealmOrUpdate(note)
                realm.commitTransaction()
                finish()
            }
        }
    }

    private fun initBottomSheet(){
        val view = layoutInflater.inflate(R.layout.note_add_bottom_sheet, null)
        val container : LinearLayout = view.findViewById(R.id.bottom_sheet_container)
        container.setBackgroundResource(intent.getIntExtra("color", R.color.background))
        bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        bottomSheet.setContentView(view)
        val listView : ListView = view.findViewById(R.id.bottom_sheet_listView)
        val chipGroup : ChipGroup = view.findViewById(R.id.bottom_sheet_chipGroup)
        val itemsList : MutableList<HashMap<String, String>> = emptyList<HashMap<String, String>>().toMutableList()
        val itemListTitles = arrayOf("Delete", "Copy", "Share")
        val itemListImages = arrayOf(R.drawable.ic_delete, R.drawable.ic_file_copy, R.drawable.ic_share)
        for ((position, item) in itemListTitles.withIndex()){
            val hm = HashMap<String, String>()
            hm["listview_title"] = item
            hm["listview_image"] = itemListImages[position].toString()
            itemsList.add(hm)
        }
        val from = arrayOf("listview_title", "listview_image")
        val to = arrayOf(R.id.list_text, R.id.list_image)
        val simpleAdapter = SimpleAdapter(this, itemsList, R.layout.simple_list_layout, from, to.toIntArray())
        listView.adapter = simpleAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            when(position){
                0 ->  deleteNote()
            }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val chip:Chip = view.findViewById(checkedId)
            when(checkedId){
                R.id.chip_background -> newColor = R.color.background
                R.id.chip_blue -> newColor = R.color.card_blue
                R.id.chip_green -> newColor = R.color.card_green
                R.id.chip_purple -> newColor = R.color.card_purple
                R.id.chip_red -> newColor = R.color.card_red
                R.id.chip_violet -> newColor = R.color.card_violet
                R.id.chip_yellow -> newColor = R.color.card_yellow
            }
            binding.activityNoteAddContainer.setBackgroundColor(resources.getColor(newColor))
            window.statusBarColor = resources.getColor(newColor)
            binding.noteAddCategoryChip.chipBackgroundColor = resources.getColorStateList(newColor)
            container.setBackgroundColor(resources.getColor(newColor))
        }


    }

    private fun deleteNote(){
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