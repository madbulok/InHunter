package com.uzlov.inhunter.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.uzlov.inhunter.cache.room.LocalDatabase
import com.uzlov.inhunter.cache.room.TeamDao
import com.uzlov.inhunter.data.local.database.RoomRepositoryImpl
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.interfaces.ILocalTeamRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocalModule {

    @Singleton
    @Provides
    fun getLocalName() : String = "com.uzlov.inhunter_preferences"

    @Singleton
    @Provides
    fun database(app: Context): LocalDatabase = Room.databaseBuilder(app, LocalDatabase::class.java, getLocalName())
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideTeamDao(database: LocalDatabase): TeamDao = database.teamDao

    @Singleton
    @Provides
    fun sharedPreference(app: Context) : SharedPreferences = app.getSharedPreferences(getLocalName(), Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun providePreferenceRepository(pref: SharedPreferences) : PreferenceRepository = PreferenceRepository(pref)

    @Singleton
    @Provides
    fun provideDatabase(teamDao: TeamDao) : ILocalTeamRepository = RoomRepositoryImpl(teamDao)
}