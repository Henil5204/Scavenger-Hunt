package ca.gbc.comp3074.scavengerhunt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row per registered user.
// username is the PK so login lookups are basically free.
// (Yes, password is plain text — class project, runs on-device only.
//  TODO: hash this if we ever do a real release.)
@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val password: String
)
