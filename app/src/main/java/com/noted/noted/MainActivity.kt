package com.noted.noted

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.noted.noted.databinding.ActivityMainBinding
import com.noted.noted.model.NoteCategory
import com.noted.noted.utils.Utils
import com.noted.noted.view.activity.BaseActivity
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.activity.SettingsActivity
import com.noted.noted.view.fragment.BaseFragment
import com.noted.noted.view.fragment.NotesFragment
import com.noted.noted.view.fragment.TasksFragment
import io.realm.Realm


class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        setupNavSlider()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.mainFab.outlineAmbientShadowColor = binding.mainFab.backgroundTintList!!.defaultColor
            binding.mainFab.outlineSpotShadowColor = binding.mainFab.backgroundTintList!!.defaultColor
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.mainBottomNavigation, navHostFragment.navController)

        binding.mainFab.setOnClickListener {
            when(val currentFragment = supportFragmentManager.currentNavigationFragment){
                is NotesFragment -> {val intent = Intent(this, NoteAddActivity::class.java)
                    startActivity(intent)}
                is TasksFragment -> currentFragment.showTasksAdd()
            }

        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar, menu)
        val searchItem = menu!!.findItem(R.id.main_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val currentFragment = supportFragmentManager.currentNavigationFragment as BaseFragment
                currentFragment.filterItem(newText!!)
                return false
            }

        })

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupNavSlider(){
        val categoriesList = realm.where(NoteCategory::class.java).findAll()
        val menu = binding.navView.menu
        val categoriesSubMenu = menu.addSubMenu(R.id.categories_group, 123, Menu.NONE, "Categories")

        categoriesSubMenu.add(R.id.categories_group, R.id.all_categories_item, Menu.NONE, "All categories").setIcon(R.drawable.ic_label).isCheckable = true

        for (category in categoriesList){
            categoriesSubMenu.add(R.id.categories_group, category.id.toInt(), Menu.NONE, category.title).setIcon(R.drawable.ic_label).isCheckable = true
        }
        categoriesSubMenu.add(R.id.categories_group, R.id.add_category_item, Menu.NONE, "Add category").setIcon(R.drawable.ic_add).isCheckable = false

        menu.add(R.id.others_group, R.id.settings_item, Menu.NONE, "Settings").setIcon(R.drawable.ic_settings).isCheckable = false

        binding.navView.setNavigationItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.currentNavigationFragment as BaseFragment
            when(item.itemId){
                R.id.all_categories_item ->{currentFragment.refresh()}
                R.id.add_category_item ->{Utils.showCategories(this@MainActivity, layoutInflater, object : Utils.Companion.OnSelectedCategory{
                    override fun onSelected(noteCategory: NoteCategory) {}
                })}
                R.id.settings_item ->{
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }
                else -> currentFragment.filterCategories(item.itemId)
            }
            binding.mainDrawerLayout.closeDrawer(binding.navView)
            true
        }

        binding.mainToolbar.setNavigationOnClickListener {
            binding.mainDrawerLayout.openDrawer(binding.navView)
        }

    }

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}