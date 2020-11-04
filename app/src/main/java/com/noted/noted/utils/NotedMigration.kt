package com.noted.noted.utils

import io.realm.DynamicRealm
import io.realm.RealmMigration

class NotedMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        val schema = realm.schema

        if (oldVersion == 1L){
            if (!schema.get("Note")!!.hasField("colorHex")){
                schema.get("Note")!!
                    .addField("colorHex", String::class.java)
            }

            oldVersion++
        }
    }


}