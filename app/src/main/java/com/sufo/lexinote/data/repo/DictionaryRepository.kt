package com.sufo.lexinote.data.repo

import android.app.Application
import com.sufo.lexinote.data.local.db.DictDatabaseHelper
import com.sufo.lexinote.data.local.db.dao.SearchHistoryDao
import com.sufo.lexinote.data.local.db.entity.DictWord
import com.sufo.lexinote.data.local.db.entity.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for the dictionary. Its sole responsibility is to fetch data from the local
 * pre-packaged stardict.db database and manage search history.
 */
@Singleton
class DictionaryRepository @Inject constructor(
    private val application: Application,
    private val searchHistoryDao: SearchHistoryDao,
    private val dbHelper : DictDatabaseHelper
) {
//    private val dbHelper = DictDatabaseHelper(application)

    /**
     * Finds a word by querying the local stardict.db database.
     */
    suspend fun findWordLocally(word: String): DictWord? {
        return withContext(Dispatchers.IO) {
            dbHelper.searchWord(word)
        }
    }

    /**
     * Finds a list of words starting with a given prefix from the local database.
     */
    suspend fun findWordsStartingWith(prefix: String): List<DictWord> {
        return withContext(Dispatchers.IO) {
            dbHelper.getSuggestions(prefix)
        }
    }

    /**
     * Gets the phonetic for a given word.
     */
    suspend fun getPhonetic(word: String): String? {
        return findWordLocally(word)?.phonetic
    }

    // --- Search History Methods ---

    fun getSearchHistory(): Flow<List<SearchHistory>> {
        return searchHistoryDao.getAll()
    }

    suspend fun addSearchToHistory(word: String, transition:String?) {
        withContext(Dispatchers.IO) {
            searchHistoryDao.insert(SearchHistory(word = word,transition = transition))
        }
    }

    suspend fun clearSearchHistory() {
        withContext(Dispatchers.IO) {
            searchHistoryDao.clearAll()
        }
    }
}

