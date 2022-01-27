package com.uzlov.inhunter.cache.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity

@Database(
    entities = [
        TeamPasswordEntity::class,
        ResponseMyTeamsItem::class
    ],
    exportSchema = false,
    version = 7
)
abstract class LocalDatabase : RoomDatabase() {
    abstract val teamDao: TeamDao
}