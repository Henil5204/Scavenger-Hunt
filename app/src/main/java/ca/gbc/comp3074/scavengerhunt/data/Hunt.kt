package ca.gbc.comp3074.scavengerhunt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One scavenger hunt.
 *
 * id auto-generates so I never have to think about it. Single tag string
 * instead of a join table — for a class project that's plenty.
 *
 * `completed` was added later when I realized it'd be way more fun if you
 * could actually mark a hunt done. Stored as a Boolean (Room maps to int 0/1).
 */
@Entity(tableName = "hunts")
data class Hunt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val tag: String,
    val address: String,
    val city: String,
    val description: String,
    val rating: Float = 0f,
    val completed: Boolean = false
)
