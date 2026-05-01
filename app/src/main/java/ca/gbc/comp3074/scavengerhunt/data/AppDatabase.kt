package ca.gbc.comp3074.scavengerhunt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Bumped to v2 when I added Hunt.completed.
// Using fallbackToDestructiveMigration so I don't have to write a Migration —
// fine while developing, but if this ever shipped to real users I'd need
// to write a proper ALTER TABLE migration so their data isn't wiped.
@Database(
    entities = [User::class, Hunt::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun huntDao(): HuntDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    "scavenger_hunt.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
