package com.noted.noted

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.noted.noted.databinding.ActivityMainBinding
import com.noted.noted.utils.ContainerTransformConfigurationHelper
import com.noted.noted.view.activity.BaseActivity
import com.noted.noted.view.activity.NoteAddActivity
import com.transitionseverywhere.extra.Scale


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    private val holdTransition = Hold()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.mainFab.outlineAmbientShadowColor = resources.getColor(R.color.primary)
            binding.mainFab.outlineSpotShadowColor = resources.getColor(R.color.primary)
        }

        binding.mainFab.setOnClickListener {
            val intent = Intent(this, NoteAddActivity::class.java)

            startActivity(intent)
        }
    }

    fun hideMainSearch(hide: Boolean) {
        val set: TransitionSet = TransitionSet()
            .addTransition(Scale(0.7f))
            .addTransition(Fade())
            .setInterpolator(FastOutLinearInInterpolator())

        TransitionManager.beginDelayedTransition(binding.mainSearchCard, set)
        if (hide) binding.mainSearchCard.visibility = View.INVISIBLE
        else binding.mainSearchCard.visibility = View.VISIBLE

    }
    private fun configureTransitions(fragment: Fragment) {
        val enterContainerTransform = buildContainerTransform(true)
        fragment.sharedElementEnterTransition = enterContainerTransform
        holdTransition.setDuration(enterContainerTransform.duration)
        val returnContainerTransform = buildContainerTransform(false)
        fragment.sharedElementReturnTransition = returnContainerTransform
    }

    private fun buildContainerTransform(entering: Boolean): MaterialContainerTransform {
        val configurationHelper = ContainerTransformConfigurationHelper()
        val transform = MaterialContainerTransform()
        transform.setDrawingViewId(if (entering) R.id.main_fab else R.id.fragment_note_add_container)
        configurationHelper.configure(transform, entering)
        return transform
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}