package ca.gbc.comp3074.scavengerhunt.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Sort modes the home screen offers in its toolbar menu.
 * Storing this as an enum makes the when() in the VM exhaustive — nice.
 */
enum class HuntSort { NEWEST, NAME, RATING }

@Dao
interface HuntDao {

    // ---- list queries (LiveData = auto-refresh on any DB change) ----

    @Query("SELECT * FROM hunts ORDER BY id DESC")
    fun observeNewest(): LiveData<List<Hunt>>

    @Query("SELECT * FROM hunts ORDER BY name COLLATE NOCASE ASC")
    fun observeByName(): LiveData<List<Hunt>>

    @Query("SELECT * FROM hunts ORDER BY rating DESC, name COLLATE NOCASE ASC")
    fun observeByRating(): LiveData<List<Hunt>>

    // search hits both name AND tag in one query — easier than two and union-ing
    @Query("""
        SELECT * FROM hunts
        WHERE name LIKE '%' || :q || '%' OR tag LIKE '%' || :q || '%'
        ORDER BY id DESC
    """)
    fun search(q: String): LiveData<List<Hunt>>

    // ---- one-shot ops ----

    @Query("SELECT * FROM hunts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Hunt?

    @Query("SELECT COUNT(*) FROM hunts")
    suspend fun count(): Int

    @Insert
    suspend fun insert(hunt: Hunt): Long

    @Update
    suspend fun update(hunt: Hunt)

    @Delete
    suspend fun delete(hunt: Hunt)

    // tiny convenience so we don't have to copy() the whole hunt just to flip one bit
    @Query("UPDATE hunts SET completed = :done WHERE id = :id")
    suspend fun setCompleted(id: Long, done: Boolean)
}
