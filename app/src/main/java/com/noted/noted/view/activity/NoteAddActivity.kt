package com.noted.noted.view.activity

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.Window
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.noted.noted.R
import com.noted.noted.databinding.ActivityNoteAddBinding

lateinit var binding: ActivityNoteAddBinding

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
        if (intent.getStringExtra("body")!= null){
            val color = intent.getStringExtra("color")
            binding.activityNoteAddContainer.setBackgroundColor(Color.parseColor(color))
            binding.noteTitleEditText.setText(intent.getStringExtra("title"))
            binding.noteBodyEditText.setText(intent.getStringExtra("body"))
            window.statusBarColor = Color.parseColor(color)
            window.navigationBarColor = Color.parseColor(color)
            binding.noteAddCategoryChip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(color)).withAlpha(200)
        }
        setSupportActionBar(binding.activityNoteAddToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_add_menu, menu);
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}