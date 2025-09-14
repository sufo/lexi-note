
package com.sufo.lexinote.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import com.sufo.lexinote.data.local.db.entity.DictWord
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A helper class to manage the pre-packaged stardict.db database.
 * This class is responsible for copying the database from the assets folder to the app's
 * internal storage and providing a method to query it.
 */
@Singleton
class DictDatabaseHelper @Inject constructor (private val context: Context) {

    companion object {
        private const val DATABASE_NAME = "stardict.db"
        private const val TAG = "DictDatabaseHelper"
    }

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).path

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    fun createDatabaseIfNotExists() {
        if (checkDatabase()) {
            Log.i(TAG, "Database already exists.")
        } else {
            Log.i(TAG, "Database does not exist. Copying from assets...")
            // By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
            try {
                context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null).close()
                copyDatabase()
                Log.i(TAG, "Database copied successfully.")
            } catch (e: IOException) {
                Log.e(TAG, "Error copying database", e)
                throw Error("Error copying database")
            }
        }
    }

    /**
     * Checks if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private fun checkDatabase(): Boolean {
        val dbFile = File(dbPath)
        return dbFile.exists()
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     */
    private fun copyDatabase() {
        // Open your local db as the input stream
        val myInput = context.assets.open("dicts/$DATABASE_NAME")

        // Path to the just created empty db
        val outFileName = dbPath

        // Open the empty db as the output stream
        val myOutput = FileOutputStream(outFileName)

        // transfer bytes from the inputfile to the outputfile
        myInput.copyTo(myOutput)

        // Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    /**
     * Searches for a word in the stardict.db database.
     *
     * @param word The word to search for.
     * @return A WordEntry object if found, otherwise null.
     */
    fun searchWord(word: String): DictWord? {
        if (!checkDatabase()) {
            Log.w(TAG, "Database not found, cannot search.")
            return null
        }

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            // IMPORTANT: You must change "stardict" and the column names ("word", "definition")
            // to match the actual table and column names in your stardict.db file.
            val cursor = db.query(
                "stardict", // Table name
                arrayOf("id","word", "phonetic", "translation","frq","exchange","tag","image_url"), // Columns to return
                "word = ?", // WHERE clause
                arrayOf(word), // WHERE arguments
                null, null, null, "1"
            )

            cursor.use { c ->
                if (c.moveToFirst()) {
                    return c.toDictWord(DATABASE_NAME)
//                    val idIndex = c.getColumnIndex("id")
//                    val pIndex = c.getColumnIndex("phonetic")
//                    val tIndex = c.getColumnIndex("translation")
//                    val frqIndex = c.getColumnIndex("frq")
//                    val excIndex = c.getColumnIndex("exchange")
//                    val phonetic = if (pIndex != -1) c.getString(pIndex) else ""
//                    val id = c.getInt(idIndex)
//                    val translation = c.getString(tIndex)
//                    val frq = if (frqIndex != -1) c.getInt(frqIndex) else null
//                    val exchange = if (excIndex != -1) c.getString(excIndex) else ""
//
//                    return DictWord(
//                        id = id,
//                        word = word,
//                        phonetic = phonetic,
//                        translation = translation,
//                        frq = frq,
//                        exchange = exchange,
//                        sourceDictionary = "stardict.db" // Mark the source
//                    )
                }
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error while searching for word: $word", e)
        } finally {
            db?.close()
        }
        return null
    }

    /**
     * Gets a list of word suggestions based on a prefix.
     *
     * @param prefix The prefix to search for.
     * @return A list of matching WordEntry objects.
     */
    fun getSuggestions(prefix: String, limit: Int = 30): List<DictWord> {
        if (!checkDatabase() || prefix.isBlank()) {
            return emptyList()
        }

        val suggestions = mutableListOf<DictWord>()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.query(
                "stardict", // Table name
                arrayOf("word","translation"), // We only need the word for suggestions
                "word LIKE ?", // WHERE clause
                arrayOf("$prefix%"), // WHERE arguments
                null, null, "id,word,frq", limit.toString()
            )

            cursor.use { c ->
                while (c.moveToNext()) {
                    val wordIndex = c.getColumnIndex("word")
                    val tIndex = c.getColumnIndex("translation")
                    if (wordIndex != -1) {
                        suggestions.add(
                            DictWord(
                                word = c.getString(wordIndex),
                                translation = c.getString(tIndex)
                            )
                        )
                    }
                }
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error while getting suggestions for: $prefix", e)
        } finally {
            db?.close()
        }
        return suggestions
    }

    /**
     * Gets a list of words by a specific tag with pagination.
     *
     * @param tag The tag to search for.
     * @param limit The number of words to return per page.
     * @param offset The starting position of the query.
     * @return A list of matching WordEntry objects.
     */
    fun getWordsByTag(tag: String, limit: Int, offset: Int): List<DictWord> {
        if (!checkDatabase()) {
            return emptyList()
        }

        val words = mutableListOf<DictWord>()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            // Assumes a 'tag' column exists and tags are stored as comma-separated strings.
            // The query uses LIKE to find words containing the specified tag.
            val cursor = db.query(
                "stardict", // Table name
                arrayOf("id", "word", "phonetic", "translation"), // Columns to return
                "tag LIKE ?", // WHERE clause
                arrayOf("%${tag}%"), // WHERE arguments
                null, null, "frq", "$offset,$limit"
            )

            cursor.use { c ->
                while (c.moveToNext()) {
                    words.add(c.toDictWord(DATABASE_NAME))
                }
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error while getting words by tag: $tag", e)
        } finally {
            db?.close()
        }
        return words
    }

    fun getAllWordsByTag(tag: String): List<DictWord> {
        val allWords = mutableListOf<DictWord>()
        var offset = 0
        val limit = 1000 // Batch size

        while (true) {
            val batch = getWordsByTag(tag, limit, offset)
            if (batch.isEmpty()) {
                break
            }
            allWords.addAll(batch)
            offset += limit
        }
        return allWords
    }

    fun getCountByTag(tag: String): Int {
        if (!checkDatabase()) {
            return 0
        }
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery("SELECT COUNT(*) FROM stardict WHERE tag LIKE ?", arrayOf("%${tag}%"))
            cursor.use { c ->
                if (c.moveToFirst()) {
                    return c.getInt(0)
                }
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error while getting count for tag: $tag", e)
        } finally {
            db?.close()
        }
        return 0
    }
}
