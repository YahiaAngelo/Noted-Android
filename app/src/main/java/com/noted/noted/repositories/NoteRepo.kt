package com.noted.noted.repositories

import android.content.Context
import androidx.work.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.noted.noted.model.Note
import com.noted.noted.model.NoteCategory
import com.noted.noted.utils.LiveRealmData
import io.realm.*
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.CountDownLatch

class NoteRepo {
    var realm: Realm = Realm.getDefaultInstance()

    fun deleteNote(note: Note) {
        realm = Realm.getDefaultInstance()
        NotesWorker.deleteNote(note.id)
        realm.use {
            it.beginTransaction()
            note.deleteFromRealm()
            it.commitTransaction()
            realm.close()
        }
    }

    fun deleteNote(id: Long) {
        realm = Realm.getDefaultInstance()
        NotesWorker.deleteNote(id)
        realm.use {
            val note = realm.where(Note::class.java).equalTo("id", id).findFirst()
            realm.beginTransaction()
            note!!.deleteFromRealm()
            realm.commitTransaction()
            realm.close()
        }
    }

    fun getNotes(): LiveRealmData<Note> {
        realm = Realm.getDefaultInstance()
        return realm.where(Note::class.java).sort("date", Sort.DESCENDING).findAllAsync()
            .asLiveData()
    }

    fun addNote(note: Note) {
        realm = Realm.getDefaultInstance()
        realm.use {
            it.beginTransaction()
            it.copyToRealmOrUpdate(note)
            it.commitTransaction()
            it.close()
        }
        NotesWorker.uploadNote(note)
    }

    fun getCategories(): RealmResults<NoteCategory> {
        realm = Realm.getDefaultInstance()
        return realm.where(NoteCategory::class.java).findAll()
    }

    fun saveCategory(noteCategory: NoteCategory) {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            realm.beginTransaction()
            realm.copyToRealm(noteCategory)
            realm.commitTransaction()
            realm.close()
        }
    }

    fun deleteCategory(noteCategory: NoteCategory) {
        realm = Realm.getDefaultInstance()
        realm.use { realm ->
            realm.beginTransaction()
            noteCategory.deleteFromRealm()
            realm.commitTransaction()
            realm.close()
        }
    }

    private fun <T : RealmModel> RealmResults<T>.asLiveData() = LiveRealmData<T>(this)

    class NotesWorker(appContext: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(appContext, workerParameters) {

        companion object{
            private val auth = Firebase.auth
            private val db = Firebase.firestore
            private var realm = Realm.getDefaultInstance()


            fun uploadNote(note: Note) {
                if (auth.currentUser == null){
                    return
                }
                val noteData = note.toHashMap()
                db.collection("users").document(auth.currentUser!!.uid).collection("notes")
                    .document("${note.id}").set(noteData)
                    .addOnSuccessListener {
                        Timber.e("I'm uploading this note ${note.id}")
                    }
                    .addOnFailureListener {
                        Timber.e("Failed uploading this note ${note.id} because $it")
                        startWorker(note.id, false)
                    }

            }

            fun downloadNotes(){
                if (auth.currentUser == null){
                    return
                }
                db.collection("users").document(auth.currentUser!!.uid).collection("notes").get()
                    .addOnSuccessListener { querySnapshot ->
                        realm = Realm.getDefaultInstance()
                        realm.use {
                            it.beginTransaction()
                            it.copyToRealmOrUpdate(querySnapshot.toNote())
                            it.commitTransaction()
                        }

                    }
            }

            fun uploadNotes() {
                if (auth.currentUser == null){
                    return
                }
                Timber.e("I'm uploading notes")
                realm = Realm.getDefaultInstance()
                val notes = realm.where(Note::class.java).findAll()
                val batch = db.batch()

                for (note in notes) {
                    val noteData = note.toHashMap()
                    val noteRef =
                        db.collection("users").document(auth.currentUser!!.uid).collection("notes")
                            .document("${note.id}")
                    batch.set(noteRef, noteData)
                }
                batch.commit().addOnSuccessListener {
                    downloadNotes()
                }
            }

            fun deleteNote(noteId: Long){
                if (auth.currentUser == null){
                    return
                }
                db.collection("users").document(auth.currentUser!!.uid).collection("notes")
                    .document(noteId.toString()).delete()
                    .addOnSuccessListener {  }
                    .addOnFailureListener {
                        startWorker(noteId, true)
                    }

            }

            private fun startWorker(noteId: Long, delete: Boolean) {
                val data = workDataOf("noteId" to noteId, "delete" to delete)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<NotesWorker>()
                    .setConstraints(constraints)
                    .setInputData(data)
                    .build()

                val workManager = WorkManager.getInstance()
                workManager.enqueue(workRequest)
            }

            fun Note.toHashMap(): HashMap<String, Any> {
                return hashMapOf(
                    "id" to this.id,
                    "title" to this.title,
                    "body" to this.body,
                    "date" to this.date,
                    "color" to this.color,
                    "categories" to categories.toHashMapArray()

                )
            }

            private fun RealmList<NoteCategory>.toHashMapArray(): MutableList<HashMap<String, Any>> {
                val array: MutableList<HashMap<String, Any>> = mutableListOf()
                this.forEach {
                    array.add(
                        hashMapOf(
                            "id" to it.id,
                            "title" to it.title
                        )
                    )
                }
                return array
            }

            fun QuerySnapshot.toNote():List<Note>{
                val notesList: MutableList<Note> = mutableListOf()
                for(query in this){
                    val id = query.getLong("id")
                    if (id != null){
                        val title = query.getString("title")
                        val body = query.getString("body")
                        val date = query.getLong("date")
                        val color = query.getLong("color")!!.toInt()
                        val categoriesList = query.get("categories") as List<Map<String, Any>>
                        val categories = RealmList<NoteCategory>()
                        for(category in categoriesList){
                            categories.add(NoteCategory(category["id"] as Long, category["title"].toString()))
                        }
                        notesList.add(Note(id, title, body, date!!, color, categories))
                    }
                }

                return notesList
            }
        }

        override suspend fun doWork(): Result = coroutineScope {
            val noteId = inputData.getLong("notedId", 0)
            val delete = inputData.getBoolean("delete", false)
            val realm = Realm.getDefaultInstance()
            val note = realm.where(Note::class.java).equalTo("id", noteId).findFirst()
            try {
                val latch = CountDownLatch(1)
                if (delete){
                    db.collection("users").document(auth.currentUser!!.uid).collection("notes")
                        .document(noteId.toString()).delete()
                        .addOnSuccessListener {
                            latch.countDown()
                        }
                        .addOnFailureListener {
                            latch.countDown()
                        }
                }else{
                    Timber.e("I'm uploading this note $noteId")
                    db.collection("users").document(auth.currentUser!!.uid).collection("notes")
                        .document("${note?.id}")
                        .set(note!!.toHashMap())
                        .addOnSuccessListener {
                            Timber.e("Finished uploading this note $noteId")
                            latch.countDown()
                        }.addOnFailureListener {
                            Timber.e("Failed uploading this note $noteId because $it")
                            latch.countDown()
                        }
                }
                latch.await()
             return@coroutineScope Result.success()
            } catch (e: Exception) {
                if (runAttemptCount > 3) {
                    return@coroutineScope Result.success()
                }
                Result.retry()
            }

        }
    }
}