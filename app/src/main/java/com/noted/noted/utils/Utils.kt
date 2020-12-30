package com.noted.noted.utils

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.noted.noted.BuildConfig
import com.noted.noted.R
import com.noted.noted.model.Note
import com.noted.noted.model.NoteCategory
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.view.bindItem.NoteBinding
import com.noted.noted.view.customView.ListedBottomSheetDialog
import io.noties.markwon.Markwon
import io.realm.Realm
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class Utils {
    companion object {

        fun showCategories(
            context: Context,
            layoutInflater: LayoutInflater,
            onSelectedCategory: OnSelectedCategory
        ) {
            var realm = Realm.getDefaultInstance()
            val dbCategories = realm.where(NoteCategory::class.java).findAll()
            val view = layoutInflater.inflate(R.layout.simple_listview_layout, null)
            val categoriesBottomSheet = BottomSheetDialog(context, R.style.BottomSheetMenuTheme)
            categoriesBottomSheet.setContentView(view)
            val listView: ListView = view.findViewById(R.id.simple_listView)
            val itemsList: MutableList<HashMap<String, String>> =
                emptyList<HashMap<String, String>>().toMutableList()
            val itemListTitles = arrayOf(context.getString(R.string.add_category)).toMutableList()
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
                SimpleAdapter(
                    context,
                    itemsList,
                    R.layout.simple_list_layout,
                    from,
                    to.toIntArray()
                )
            listView.adapter = simpleAdapter

            listView.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(context.getString(R.string.add_category))
                            .setView(R.layout.dialog_edittext_layout)
                            .setPositiveButton(context.getString(R.string.add)) { dialog, _ ->
                                val text =
                                    (dialog as? AlertDialog)?.findViewById<EditText>(R.id.dialog_editText)?.text?.toString()
                                if (text != null) {
                                    val noteCategory =
                                        NoteCategory(UUID.randomUUID().mostSignificantBits, text)
                                    saveCategory(noteCategory)

                                    itemListTitles.add(text)
                                    itemListImages.add(R.drawable.ic_label)
                                    val hashMap = HashMap<String, String>()
                                    hashMap["listview_title"] = text
                                    hashMap["listview_image"] =
                                        itemListImages[itemsList.size].toString()
                                    itemsList.add(itemsList.size, hashMap)
                                    simpleAdapter.notifyDataSetChanged()
                                    dialog.dismiss()
                                }

                            }
                            .setNegativeButton(context.resources.getString(android.R.string.cancel)) { dialog, _ ->
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
                if (position > 0) {
                    val noteCategory = dbCategories[position - 1]!!
                    MaterialAlertDialogBuilder(context)
                        .setTitle(context.getString(R.string.delete_category))
                        .setMessage(context.getString(R.string.delete_confirm) + "${noteCategory.title} category ?")
                        .setNeutralButton(
                            context.getString(R.string.delete)
                        ) { dialog, _ ->
                            itemsList.removeAt(position)
                            realm = Realm.getDefaultInstance()
                            val notesWithCategory = realm.where(Note::class.java).equalTo(
                                "categories.id",
                                noteCategory.id
                            ).findAll()
                            Timber.e("Notes with this category count is ${notesWithCategory.size}")
                            realm.use { realm ->
                                realm.beginTransaction()
                                for (note in notesWithCategory) {
                                    note.categories.remove(noteCategory)
                                    NoteRepo.NotesWorker.uploadNote(note)
                                }
                                noteCategory.deleteFromRealm()
                                realm.commitTransaction()
                                realm.close()
                            }
                            simpleAdapter.notifyDataSetChanged()
                            dialog.dismiss()
                        }
                        .setNegativeButton(context.resources.getString(android.R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                true
            }
            categoriesBottomSheet.show()


        }

        fun showCalendar(
            context: Context,
            fragmentManager: FragmentManager,
            onSelectedCalendar: OnSelectedCalendar
        ) {
            val currentCalendar = Calendar.getInstance()
            var selectedCalendar: Calendar? = null
            val materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker()
            materialDatePickerBuilder.setTitleText(context.getString(R.string.reminder_date))
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

        fun shareText(context: Context, string: String) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, string)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)

        }

        private fun shareNoteImage(
            note: Note,
            context: Context,
            layoutInflater: LayoutInflater
        ) {
            val view = layoutInflater.inflate(R.layout.note_share_image_layout, null)
            val recyclerView = view.findViewById<RecyclerView>(R.id.note_share_rv)
            recyclerView.layoutManager = LinearLayoutManager(context)
            val itemAdapter = ItemAdapter<NoteBinding>()
            val fastAdapter = FastAdapter.Companion.with(itemAdapter)
            recyclerView.adapter = fastAdapter
            itemAdapter.add(NoteBinding(note))

            val bitmap = getBitmapFromView(view)
            try {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdir()
                val stream = FileOutputStream("${cachePath.absolutePath}/image.png")
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }


            val imagePath = File(context.cacheDir, "images")
            val newFile = File(imagePath, "image.png")
            val contentUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                newFile
            )
            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.choose_an_app)))
            }


        }

        fun shareNote(
            note: Note,
            context: Context,
            layoutInflater: LayoutInflater
        ) {
            val markwon = Markwon.builder(context)
                .build()
            val bottomSheetTitles = listOf(
                context.resources.getString(R.string.share_as_text),
                context.resources.getString(R.string.share_as_image),
                context.resources.getString(R.string.share_as_markdown)
            )
            val bottomSheetImages =
                listOf(R.drawable.ic_title, R.drawable.ic_image, R.drawable.ic_markdown)
            val shareNoteBottomSheet = ListedBottomSheetDialog(
                bottomSheetTitles,
                bottomSheetImages,
                context,
                layoutInflater
            )
            shareNoteBottomSheet.bottomSheetTitle = context.resources.getString(R.string.share_as)
            shareNoteBottomSheet.bottomSheetTitleIcon =
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_share, context.theme)
            shareNoteBottomSheet.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    when (position) {
                        0 -> shareText(context, "${note.title}\n\n${markwon.toMarkdown(note.body)}")
                        1 -> shareNoteImage(
                            note,
                            context,
                            layoutInflater
                        )
                        2 -> shareText(context, "#${note.title}\n${note.body}")
                    }
                }
            shareNoteBottomSheet.show()
        }

        fun copyToClipboard(context: Context, string: String) {
            val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("My Note", string)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.copied_note), Toast.LENGTH_SHORT).show()
        }

        fun invertColor(myColorString: String): Int {
            val color = myColorString.toLong(16).toInt()
            val r = color shr 16 and 0xFF
            val g = color shr 8 and 0xFF
            val b = color shr 0 and 0xFF
            val invertedRed = 255 - r
            val invertedGreen = 255 - g
            val invertedBlue = 255 - b
            val invertedColor: Int = Color.rgb(invertedRed, invertedGreen, invertedBlue)
            return invertedColor
        }

        private fun saveCategory(noteCategory: NoteCategory) {
            val realm = Realm.getDefaultInstance()
            realm.use {
                it.beginTransaction()
                it.copyToRealm(noteCategory)
                it.commitTransaction()
            }
        }

        interface OnSelectedCategory {
            fun onSelected(noteCategory: NoteCategory)
        }

        interface OnSelectedCalendar {
            fun onSelected(calendar: Calendar)
        }

        fun getBitmapFromView(view: View): Bitmap? {
            if (view.height <= 0) {
                val specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                view.measure(specWidth, specWidth)
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            }
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }


    }


}