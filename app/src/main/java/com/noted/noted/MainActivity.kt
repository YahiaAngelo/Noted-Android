package com.noted.noted

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.UnderlineSpan
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.noted.noted.databinding.ActivityMainBinding
import com.noted.noted.model.NoteCategory
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.utils.Utils
import com.noted.noted.view.activity.AboutActivity
import com.noted.noted.view.activity.BaseActivity
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.activity.SettingsActivity
import com.noted.noted.view.fragment.BaseFragment
import com.noted.noted.view.fragment.NotesFragment
import com.noted.noted.view.fragment.TasksFragment
import com.squareup.picasso.Picasso
import io.realm.Realm


class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var realm = Realm.getDefaultInstance()
    private val RCSIGNIN = 4586
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var headerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        auth = Firebase.auth
        sharedPreference = getPreferences(Context.MODE_PRIVATE)
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
        headerView = binding.navView.getHeaderView(0)
        val loginButton = headerView.findViewById<MaterialButton>(R.id.google_login)
        val userInfo = headerView.findViewById<LinearLayout>(R.id.main_user_info)
        val logoutText = headerView.findViewById<TextView>(R.id.main_user_logout)
        val spannableStringBuilder = SpannableStringBuilder(logoutText.text)
        spannableStringBuilder.setSpan(UnderlineSpan(),0, spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        logoutText.text = spannableStringBuilder
        logoutText.setOnClickListener {
            if (auth.currentUser != null){
                auth.signOut()
                loginButton.visibility = View.VISIBLE
                userInfo.visibility = View.GONE
                with(sharedPreference.edit()){
                    putString("profile_img", "placeholder")
                    putString("user_email", "")
                    commit()
                }
            }
        }

        if (auth.currentUser != null){
            loginButton.visibility = View.GONE
            userInfo.visibility = View.VISIBLE
        }
        loginButton.setOnClickListener {
            login()
        }
        checkUserInfo()

        val categoriesList = realm.where(NoteCategory::class.java).findAll()
        val menu = binding.navView.menu
        val categoriesSubMenu = menu.addSubMenu(R.id.categories_group, 123, Menu.NONE, "Categories")

        categoriesSubMenu.add(R.id.categories_group, R.id.all_categories_item, Menu.NONE, "All categories").setIcon(R.drawable.ic_label).isCheckable = true

        for (category in categoriesList){
            categoriesSubMenu.add(R.id.categories_group, category.id.toInt(), Menu.NONE, category.title).setIcon(R.drawable.ic_label).isCheckable = true
        }
        categoriesSubMenu.add(R.id.categories_group, R.id.add_category_item, Menu.NONE, "Add category").setIcon(R.drawable.ic_add).isCheckable = false

        menu.add(R.id.others_group, R.id.settings_item, Menu.NONE, "Settings").setIcon(R.drawable.ic_settings).isCheckable = false
        menu.add(R.id.others_group, R.id.about_item, Menu.NONE, "About").setIcon(R.drawable.ic_info_black).isCheckable = false

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
                R.id.about_item ->{
                    val aboutIntent = Intent(this, AboutActivity::class.java)
                    startActivity(aboutIntent)
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

    private fun login(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RCSIGNIN)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RCSIGNIN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                with(sharedPreference.edit()){
                    putString("profile_img", account.photoUrl.toString())
                    putString("user_email", account.email)
                    commit()
                }
                checkUserInfo()
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    headerView.findViewById<MaterialButton>(R.id.google_login).visibility = View.GONE
                    headerView.findViewById<LinearLayout>(R.id.main_user_info).visibility = View.VISIBLE
                    Snackbar.make(binding.navView, "Your notes are now synced !", Snackbar.LENGTH_SHORT).show()
                    binding.mainDrawerLayout.closeDrawer(binding.navView)
                    NoteRepo.NotesWorker.uploadNotes()

                } else {
                    // If sign in fails, display a message to the user.
                    // ...
                    Snackbar.make(binding.navView, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()

                }

                // ...
            }
    }

    private fun checkUserInfo(){
        val headerView = binding.navView.getHeaderView(0)
        val profileImage = headerView.findViewById<ImageView>(R.id.main_user_image)
        val emailText = headerView.findViewById<TextView>(R.id.main_user_email)
        emailText.text = sharedPreference.getString("user_email", "")
        val imgLink = sharedPreference.getString("profile_img", "placeholder")
        Picasso.get()
            .load(imgLink)
            .error(R.drawable.ic_person)
            .into(profileImage)
    }

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}