package com.uzlov.inhunter.di.module

import android.content.Context
import com.uzlov.inhunter.map.MapService
import dagger.Module
import dagger.Provides

@Module
class MapModule {

    @Provides
    fun provideMapService(context: Context) : MapService = MapService(context)

//    @Provides
//    fun provideLocationReceiver() : LocaRe
}