package com.noted.noted.utils

import android.app.TimePickerDialog
import android.content.*
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noted.noted.R
import com.noted.noted.model.NoteCategory
import io.realm.Realm
import java.util.*
import kotlin.collections.HashMap

class Utils {
    companion object{

        fun showCategories(context: Context, layoutInflater: LayoutInflater, onSelectedCategory: OnSelectedCategory){
            var realm = Realm.getDefaultInstance()
            val dbCategories = realm.where(NoteCategory::class.java).findAll()
            val view = layoutInflater.inflate(R.layout.simple_listview_layout, null)
            val categoriesBottomSheet = BottomSheetDialog(context, R.style.BottomSheetMenuTheme)
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
                SimpleAdapter(context, itemsList, R.layout.simple_list_layout, from, to.toIntArray())
            listView.adapter = simpleAdapter

            listView.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        MaterialAlertDialogBuilder(context)
                            .setTitle("Add category")
                            .setView(R.layout.dialog_edittext_layout)
                            .setPositiveButton("Add") { dialog, _ ->
                                val text =
                                    (dialog as? AlertDialog)?.findViewById<EditText>(R.id.dialog_editText)?.text?.toString()
                                if (text != null) {
                                    val noteCategory =
                                        NoteCategory(UUID.randomUUID().mostSignificantBits, text)
                                    saveCategory(noteCategory)

                                    itemListTitles.add(text)
                                    itemListImages.add(R.drawable.ic_label)
                                    val hashMap =  HashMap<String, String>()
                                    hashMap["listview_title"] = text
                                    hashMap["listview_image"] = itemListImages[itemsList.size].toString()
                                    itemsList.add(itemsList.size, hashMap)
                                    simpleAdapter.notifyDataSetChanged()
                                    dialog.dismiss()
                                }

                            }.setNegativeButton(context.resources.getString(android.R.string.cancel)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    else -> {
                        val noteCategory = dbCategories[position - 1]!!
                        onSelectedCategory.onSelected(noteCategory)
                        categoriesBottomSheet.dismiss()
                    }
                }
            }
            listView.setOnItemLongClickListener { _, _, position, _ ->
                val noteCategory = dbCategories[position - 1]!!
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete category")
                    .setMessage("Do you want to delete ${noteCategory.title} category ?")
                    .setNeutralButton("Delete"
                    ) { dialog, _ ->
                        itemsList.removeAt(position)
                        realm = Realm.getDefaultInstance()
                        realm.use {
                            it.beginTransaction()
                            noteCategory.deleteFromRealm()
                            it.commitTransaction()
                        }
                        simpleAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    .setNegativeButton(context.resources.getString(android.R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                true
            }
            categoriesBottomSheet.show()


        }

        fun showCalendar(context: Context, fragmentManager: FragmentManager, onSelectedCalendar: OnSelectedCalendar){
            val currentCalendar = Calendar.getInstance()
            var selectedCalendar: Calendar? = null
            val materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker()
            materialDatePickerBuilder.setTitleText("Reminder Date")
            val calendarConstraints = CalendarConstraints.Builder()
            calendarConstraints.setStart(MaterialDatePicker.todayInUtcMilliseconds())
            calendarConstraints.setValidator(DateValidatorPointForward.now())
            materialDatePickerBuilder.setCalendarConstraints(calendarConstraints.build())
            val materialDatePicker = materialDatePickerBuilder.build()
            materialDatePicker.addOnPositiveButtonClickListener {
                selectedCalendar = Calendar.getInstance()
                selectedCalendar!!.time = Date(it)

                TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        selectedCalendar!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedCalendar!!.set(Calendar.MINUTE, minute)
                        selectedCalendar!!.set(Calendar.SECOND, 0)
                        onSelectedCalendar.onSelected(selectedCalendar!!)
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            materialDatePicker.showNow(fragmentManager, materialDatePicker.tag)
        }

        fun shareText(context: Context, string: String){
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, string)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)

        }
        fun copyToClipboard(context: Context, string: String){
            val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("My Note", string)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied this note to clipboard.", Toast.LENGTH_SHORT).show()
        }

        private fun saveCategory(noteCategory: NoteCategory) {
           val realm = Realm.getDefaultInstance()
            realm.use {
                it.beginTransaction()
                it.copyToRealm(noteCategory)
                it.commitTransaction()
            }
        }
        interface OnSelectedCategory{
           fun onSelected(noteCategory: NoteCategory)
        }
        interface OnSelectedCalendar{
            fun onSelected(calendar: Calendar)
        }
    }


}