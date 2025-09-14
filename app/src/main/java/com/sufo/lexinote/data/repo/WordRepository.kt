package com.sufo.lexinote.data.repo

import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.local.db.dao.WordDao
import com.sufo.lexinote.ui.feature.word.SortOrder
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(private val wordDao: WordDao) {

    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    fun getWordsForNotebook(notebookId: Int, query: String, limit: Int, offset: Int, sortOrder: SortOrder): Flow<List<Word>> {
        val sortClause = when (sortOrder) {
            SortOrder.A_TO_Z -> "ORDER BY word ASC"
            SortOrder.Z_TO_A -> "ORDER BY word DESC"
            SortOrder.PROFICIENCY -> "ORDER BY repetitions DESC"
            SortOrder.MODIFICATION_TIME -> "ORDER BY updatedAt DESC"
            SortOrder.STUDY_TIME -> "ORDER BY nextReviewDate ASC"
            SortOrder.RANDOM -> "ORDER BY RANDOM()"
        }
        val queryString = "SELECT * FROM words WHERE notebookId = ? AND word LIKE '%' || ? || '%' $sortClause LIMIT ? OFFSET ?"
        val sqliteQuery = SimpleSQLiteQuery(queryString, arrayOf(notebookId, query, limit, offset))
        return wordDao.getWordsForNotebook(sqliteQuery)
    }

    suspend fun addWord(word: Word): Long {
        return wordDao.insertWord(word)
    }

    suspend fun addWords(words: List<Word>) {
        wordDao.insertWords(words)
    }

    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word)
    }

    suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word)
    }

    fun getSavedWord(word: String): Flow<Word?> = wordDao.getSavedWord(word)

    fun getWordById(id: Int): Flow<Word?> = wordDao.getWordById(id)

    fun getWordByWord(word: String): Flow<Word?> = wordDao.getWordByWord(word)

    fun getMasteredWordCount(): Flow<Int> = wordDao.getMasteredWordCount()
}