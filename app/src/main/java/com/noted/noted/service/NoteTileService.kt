package com.noted.noted.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.noted.noted.view.activity.NoteAddActivity

@RequiresApi(Build.VERSION_CODES.N)
class NoteTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, NoteAddActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val closeNotiIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(closeNotiIntent)
        startActivity(intent)
    }
}
