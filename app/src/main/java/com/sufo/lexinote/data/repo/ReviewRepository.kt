package com.sufo.lexinote.data.repo

import com.sufo.lexinote.data.local.db.dao.WordDao
import com.sufo.lexinote.data.local.db.entity.Word
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val wordDao: WordDao
) {

    suspend fun getDueWords(): List<Word> {
        return wordDao.getDueWords(Date())
    }

    suspend fun getDueWordsForNotebook(notebookId: Int): List<Word> {
        return wordDao.getDueWordsForNotebook(notebookId, Date())
    }

    suspend fun getDueWordsCount(): Int {
        return wordDao.getDueWordsCount(Date())
    }

    suspend fun getDueWordsCountForNotebook(notebookId: Int): Int {
        return wordDao.getDueWordsCountForNotebook(notebookId, Date())
    }

    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word)
    }
}