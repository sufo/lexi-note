// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.di

import android.content.Context
import androidx.room.Room
import com.sufo.lexinote.data.local.db.AppDatabase
import com.sufo.lexinote.data.local.db.MIGRATION_1_2
import com.sufo.lexinote.data.local.db.dao.NotebookDao
import com.sufo.lexinote.data.local.db.dao.ReviewLogDao
import com.sufo.lexinote.data.local.db.dao.SearchHistoryDao
import com.sufo.lexinote.data.local.db.dao.UserDao
import com.sufo.lexinote.data.local.db.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLexiNoteDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "lexinote_database"
                )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    fun provideNotebookDao(database: AppDatabase): NotebookDao {
        return database.notebookDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideReviewLogDao(database: AppDatabase): ReviewLogDao {
        return database.reviewLogDao()
    }

}