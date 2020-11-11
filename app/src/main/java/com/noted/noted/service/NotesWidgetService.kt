package com.noted.noted.service

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.res.ResourcesCompat
import com.noted.noted.R
import com.noted.noted.model.Note
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.realm.Realm
import io.realm.Sort
import org.commonmark.node.SoftLineBreak

class NotesWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {

        return GridViewFactory(this.applicationContext, intent)
    }


}

class GridViewFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
    private lateinit var notesList:List<Note>
    private lateinit var  markwon: Markwon

    override fun onCreate() {
        val realm = Realm.getDefaultInstance()
        val realmResults = realm.where(Note::class.java).sort(arrayOf("isFavorite", "date"), arrayOf(
            Sort.DESCENDING, Sort.DESCENDING)).findAll()
        notesList = realm.copyFromRealm(realmResults)


        markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(ResourcesCompat.getColor(context.resources, R.color.primary, context.theme), ResourcesCompat.getColor(context.resources, R.color.primary, context.theme), ResourcesCompat.getColor(context.resources, R.color.background, context.theme)))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _ -> visitor.forceNewLine() }
                }
            })
            .build()
    }

    override fun onDestroy() {

    }


    override fun getCount(): Int {
       return notesList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val note = notesList[position]
        val body = markwon.toMarkdown(note.body)

        return RemoteViews(context.packageName, R.layout.notes_widget_item).apply {
            setTextViewText(R.id.widget_note_title, note.title)
            setTextViewText(R.id.widget_note_body, body)
            if (note.isFavorite){
                setImageViewResource(R.id.widget_note_favorite, R.drawable.ic_favorite)
            }

        }
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notes_widget_item)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        val realm = Realm.getDefaultInstance()
        val realmResults = realm.where(Note::class.java).sort(arrayOf("isFavorite", "date"), arrayOf(
            Sort.DESCENDING, Sort.DESCENDING)).findAll()
        notesList = realm.copyFromRealm(realmResults)
    }

    override fun getItemId(position: Int): Long {
        return notesList[position].id
    }
}

