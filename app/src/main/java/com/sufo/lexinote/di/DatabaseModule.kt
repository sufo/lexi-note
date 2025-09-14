package com.sufo.lexinote.di

import android.content.Context
import com.sufo.lexinote.data.local.db.DictDatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDictDatabaseHelper(@ApplicationContext context: Context): DictDatabaseHelper {
        return DictDatabaseHelper(context)
    }
}
