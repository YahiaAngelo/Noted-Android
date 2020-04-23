package com.noted.noted.view.activity

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.SlideFragmentBuilder
import android.content.Intent
import android.os.Bundle
import com.noted.noted.MainActivity
import com.noted.noted.R

class IntroActivity : MaterialIntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(SlideFragmentBuilder().backgroundColor(R.color.background)
            .buttonsColor(R.color.primary)
            .image(R.drawable.ic_intro_notes)
            .title("Take Notes")
            .description("Quickly add some notes")
            .build())

        addSlide(SlideFragmentBuilder().backgroundColor(R.color.background)
            .buttonsColor(R.color.primary)
            .image(R.drawable.ic_intro_tasks)
            .title("Make Tasks")
            .description("Make your own tasks and check them later")
            .build())

        addSlide(SlideFragmentBuilder().backgroundColor(R.color.background)
            .buttonsColor(R.color.primary)
            .image(R.drawable.ic_intro_reminders)
            .title("Add Reminders")
            .description("Add a reminders for yourself")
            .build())

    }

    override fun onFinish() {
        super.onFinish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}