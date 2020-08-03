package com.noted.noted.view.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.noted.noted.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

            val themePreference = findPreference<ListPreference>("theme")
            val notesStylePreference = findPreference<ListPreference>("notes_grid")
            val timeFormatPreference = findPreference<SwitchPreference>("hour_format")
            themePreference!!.setOnPreferenceChangeListener { _, newValue ->
                when(newValue){
                    "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                true
            }
            notesStylePreference!!.setOnPreferenceChangeListener { _, newValue ->
                with(sharedPref!!.edit()){
                   putString("notes_grid", newValue.toString())
                    commit()
                }
                Toast.makeText(context, "Please restart the app to take effect", Toast.LENGTH_SHORT).show()
                true
            }

            timeFormatPreference!!.setOnPreferenceChangeListener { _, newValue ->
                with(sharedPref!!.edit()){
                    putBoolean("hour_format", newValue as Boolean)
                    commit()
                }
            }
        }

    }
}