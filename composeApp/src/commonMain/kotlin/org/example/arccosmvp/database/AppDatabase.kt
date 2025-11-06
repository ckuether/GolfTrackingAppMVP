package org.example.arccosmvp.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.round_of_golf_domain.data.dao.RoundOfGolfEventDao
import com.example.round_of_golf_domain.data.entity.RoundOfGolfEventEntity
import com.example.shared.data.dao.ScoreCardDao
import com.example.shared.data.database.DatabaseConstants
import com.example.shared.data.entity.ScoreCardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        ScoreCardEntity::class,
        RoundOfGolfEventEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreCardDao(): ScoreCardDao
    
    abstract fun roundOfGolfEventDao(): RoundOfGolfEventDao
}

// Database constructor for Room
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun getScoreCardDao(appDatabase: AppDatabase) = appDatabase.scoreCardDao()

@Deprecated(
    message = "Use getRoundOfGolfEventDao() with LocationUpdated events instead",
    replaceWith = ReplaceWith("getRoundOfGolfEventDao(appDatabase)"),
    level = DeprecationLevel.WARNING
)

fun getRoundOfGolfEventDao(appDatabase: AppDatabase) = appDatabase.roundOfGolfEventDao()