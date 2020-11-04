package com.noted.noted

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.noted.noted.receiver.NotesWidgetProvider
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.repositories.TaskRepo
import com.noted.noted.utils.Extensions
import com.noted.noted.utils.NotedMigration
import com.noted.noted.viewmodel.NotesFragmentViewModel
import com.noted.noted.viewmodel.TasksFragmentViewModel
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import java.io.File
import java.security.SecureRandom

class AppController : Application(){

    private  val notiChannelIds = arrayListOf("alarm")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        val extensionsModule = module {
            single { Extensions() }
            viewModel { TasksFragmentViewModel(get())}
            viewModel { NotesFragmentViewModel(get()) }
            single { TaskRepo() }
            single { NoteRepo() }
        }
        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@AppController)

            // load properties from assets/koin.properties file
            androidFileProperties()

            // module list
            modules(listOf(extensionsModule))
        }

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        initRealm()
        createNotificationChannel()

    }



    private fun initRealm(){
        Realm.init(this)

        val newRealmConfig = RealmConfiguration.Builder()
            .name("encryptedNoted.realm")
            .schemaVersion(2)
            .encryptionKey(loadKey())
            .migration(NotedMigration())
            .build()

        val newRealmFile = File(newRealmConfig.path)
        if (!newRealmFile.exists()){
            val oldConfig = RealmConfiguration.Builder().name("noted.realm").schemaVersion(1).build()
             val realm = Realm.getInstance(oldConfig)
            realm.writeEncryptedCopyTo(newRealmFile, loadKey())
            realm.close()
            Realm.deleteRealm(oldConfig)
            Realm.setDefaultConfiguration(newRealmConfig)
        }else{
            Realm.setDefaultConfiguration(newRealmConfig)
        }

    }

    private fun loadKey(): ByteArray{
        val sharedPrefs = EncryptedSharedPreferences.create(
            "secret_preferences",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this ,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val key = sharedPrefs.getString("realmdb", "")
        return if (key!!.isNotEmpty()){
            Base64.decode(key, Base64.NO_WRAP)
        }else{
            val newKey = ByteArray(64)
            SecureRandom().nextBytes(newKey)
            sharedPrefs.edit().putString("realmdb", Base64.encodeToString(newKey, Base64.NO_WRAP)).apply()
            newKey
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notiChannelIds.forEach {
                val name = "Reminder notification"
                val descriptionText = "Reminder notification"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(it, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWidgetPin(){
        val appWidgetManager: AppWidgetManager = getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(this, NotesWidgetProvider::class.java)

        val successCallback: PendingIntent? = if (appWidgetManager.isRequestPinAppWidgetSupported) {
            Intent().let { intent ->
                // Configure the intent so that your app's broadcast receiver gets
                // the callback successfully. This callback receives the ID of the
                // newly-pinned widget (EXTRA_APPWIDGET_ID).
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        } else {
            null
        }

        successCallback?.also { pendingIntent ->
            appWidgetManager.requestPinAppWidget(myProvider, null, pendingIntent)
        }

    }

}