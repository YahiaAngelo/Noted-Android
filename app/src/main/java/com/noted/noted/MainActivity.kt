package com.noted.noted

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
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
import com.noted.noted.view.fragment.NotesFragment
import com.transitionseverywhere.extra.Scale
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.mainFab.outlineAmbientShadowColor = resources.getColor(R.color.primary,theme)
            binding.mainFab.outlineSpotShadowColor = resources.getColor(R.color.primary,theme)
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.mainBottomNavigation, navHostFragment.navController)

        binding.mainFab.setOnClickListener {
            val currentFragment = supportFragmentManager.currentNavigationFragment
            if (currentFragment is NotesFragment){
                val intent = Intent(this, NoteAddActivity::class.java)
                startActivity(intent)
            }

        }

        KeyboardVisibilityEvent.setEventListener(this, object : KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
               if (!isOpen && binding.mainSearch.isFocused){
                   binding.mainSearch.clearFocus()
               }
            }

        })
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

    val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}