package com.noted.noted.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noted.noted.R
import com.noted.noted.model.NoteCategory
import io.realm.Realm
import java.util.*
import kotlin.collections.HashMap

class Utils {
    companion object{

        fun showCategories(context: Context, layoutInflater: LayoutInflater, onSelectedCategory: OnSelectedCategory){
            val realm = Realm.getDefaultInstance()
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
                                    categoriesBottomSheet.dismiss()
                                    showCategories(context, layoutInflater, object:OnSelectedCategory{
                                        override fun onSelected(noteCategory: NoteCategory) {
                                        }

                                    } )
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
            categoriesBottomSheet.show()


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
    }


}