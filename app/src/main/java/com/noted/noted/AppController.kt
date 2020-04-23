package com.noted.noted

import android.app.Application
import com.noted.noted.utils.Extensions
import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppController : Application(){

    override fun onCreate() {
        super.onCreate()
        val extensionsModule = module {
            single { Extensions() }
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


        Realm.init(this)

    }


}