
package com.sufo.lexinote.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sufo.lexinote.data.local.db.entity.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    /**
     * Inserts a search entry. If the word already exists, it will be replaced.
     * This is useful for updating the timestamp of an existing search.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    /**
     * Gets all search history entries, ordered by the most recent.
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SearchHistory>>

    /**
     * Deletes all entries from the search history.
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
