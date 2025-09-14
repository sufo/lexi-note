package com.sufo.lexinote.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Query
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.sufo.lexinote.data.local.db.entity.Word
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 单词本里面的单词
 */
@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<Word>>

    @RawQuery(observedEntities = [Word::class])
    fun getWordsForNotebook(query: SupportSQLiteQuery): Flow<List<Word>>

    @Insert
    suspend fun insertWord(word: Word): Long

    @Insert
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    // --- Methods for Spaced Repetition ---

    @Query("SELECT * FROM words WHERE nextReviewDate <= :currentDate ORDER BY createdAt, repetitions")
    suspend fun getDueWords(currentDate: Date): List<Word>

    @Query("SELECT * FROM words WHERE notebookId = :notebookId AND nextReviewDate <= :currentDate ORDER BY createdAt")
    suspend fun getDueWordsForNotebook(notebookId: Int, currentDate: Date): List<Word>

    @Query("SELECT COUNT(*) FROM words WHERE nextReviewDate <= :currentDate")
    suspend fun getDueWordsCount(currentDate: Date): Int

    @Query("SELECT COUNT(*) FROM words WHERE notebookId = :notebookId AND nextReviewDate <= :currentDate")
    suspend fun getDueWordsCountForNotebook(notebookId: Int, currentDate: Date): Int

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    fun getSavedWord(word: String): Flow<Word?>

    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordById(id: Int): Flow<Word?>

    @Query("SELECT * FROM words WHERE word = :word")
    fun getWordByWord(word: String): Flow<Word?>

    @Query("SELECT COUNT(*) FROM words WHERE notebookId = :notebookId")
    suspend fun getWordCountForNotebook(notebookId: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE notebookId = :notebookId AND repetitions >= 5")
    suspend fun getMasteredWordCountForNotebook(notebookId: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE repetitions >= 5")
    fun getMasteredWordCount(): Flow<Int>

    @Query("DELETE FROM words WHERE notebookId = :notebookId")
    suspend fun deleteWordsByNotebookId(notebookId: Int)
}