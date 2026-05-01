package ca.gbc.comp3074.scavengerhunt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    // returns null when the username doesn't exist — login screen treats that as "wrong creds"
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    // IGNORE so a duplicate username returns -1 instead of throwing.
    // signup activity reads the return value to decide which toast to show.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long
}
