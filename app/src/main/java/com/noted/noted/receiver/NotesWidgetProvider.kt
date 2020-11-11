package com.noted.noted.receiver

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.noted.noted.R
import com.noted.noted.service.NotesWidgetService


class NotesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity
            val intent = Intent(context, NotesWidgetService::class.java).apply {
                // Add the app widget ID to the intent extras.
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            // Instantiate the RemoteViews object for the app widget layout.
            val rv = RemoteViews(context.packageName, R.layout.notes_widget_layout).apply {
                // Set up the RemoteViews object to use a RemoteViews adapter.
                // This adapter connects
                // to a RemoteViewsService  through the specified intent.
                // This is how you populate the data.
                setRemoteAdapter(R.id.notes_widget_grid, intent)

                // The empty view is displayed when the collection has no items.
                // It should be in the same layout used to instantiate the RemoteViews
                // object above.
                setEmptyView(R.id.notes_widget_grid, R.id.empty_view)
            }

            //
            // Do additional processing specific to this app widget...
            //

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}
