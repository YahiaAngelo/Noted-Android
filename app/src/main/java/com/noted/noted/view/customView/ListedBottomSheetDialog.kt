package com.noted.noted.view.customView

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noted.noted.R

class ListedBottomSheetDialog(
    titlesList: List<String>,
    imagesList: List<Int>,
    context: Context,
    layoutInflater: LayoutInflater
) {

    private var bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetMenuTheme)
    private var listView: ListView
    private var bottomSheetView: View =
        layoutInflater.inflate(R.layout.bottomsheet_listview_layout, null)
    private val titleTextView: TextView = bottomSheetView.findViewById<TextView>(R.id.bottomsheet_title)
    var onItemClickListener: AdapterView.OnItemClickListener? = null
    set(value) {
        listView.onItemClickListener = value
        field = value
    }
    var onItemLongClickListener: AdapterView.OnItemLongClickListener? = null
        set(value) {
            listView.onItemLongClickListener = value
            field = value
        }
    var bottomSheetTitle: String? = null
    set(value) {
        titleTextView.visibility = View.VISIBLE
        titleTextView.text = value
        field = value
    }
    var bottomSheetTitleIcon: Drawable? = null
    set(value) {
        titleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(value, null, null, null)
        field = value
    }

    init {
        bottomSheetDialog.setContentView(bottomSheetView)
        listView = bottomSheetView.findViewById(R.id.bottomsheet_listView)
        val itemsList: MutableList<HashMap<String, String>> =
            emptyList<HashMap<String, String>>().toMutableList()

        for ((position, item) in titlesList.withIndex()) {
            val hm = HashMap<String, String>()
            hm["listview_title"] = item
            hm["listview_image"] = imagesList[position].toString()
            itemsList.add(hm)
        }
        val from = arrayOf("listview_title", "listview_image")
        val to = arrayOf(R.id.list_text, R.id.list_image)
        val simpleAdapter =
            SimpleAdapter(
                context,
                itemsList,
                R.layout.simple_list_layout,
                from,
                to.toIntArray()
            )
        listView.adapter = simpleAdapter


    }

    fun show(){
        bottomSheetDialog.show()
    }
}