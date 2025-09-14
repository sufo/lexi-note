// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sufo.lexinote.data.local.db.dao.NotebookDao
import com.sufo.lexinote.data.local.db.dao.ReviewLogDao
import com.sufo.lexinote.data.local.db.dao.SearchHistoryDao
import com.sufo.lexinote.data.local.db.dao.UserDao
import com.sufo.lexinote.data.local.db.dao.WordDao
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.local.db.entity.ReviewLog
import com.sufo.lexinote.data.local.db.entity.SearchHistory
import com.sufo.lexinote.data.local.db.entity.User
import com.sufo.lexinote.data.local.db.entity.Word

@Database(entities = [Notebook::class, User::class, Word::class, SearchHistory::class, ReviewLog::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun notebookDao(): NotebookDao
    abstract fun userDao(): UserDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun reviewLogDao(): ReviewLogDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `review_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wordId` INTEGER NOT NULL, `reviewedAt` INTEGER NOT NULL, FOREIGN KEY(`wordId`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_logs_wordId` ON `review_logs`(`wordId`)")
    }
}