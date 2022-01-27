package com.uzlov.inhunter.di.module

import android.content.Context
import com.uzlov.inhunter.app.App
import dagger.Module
import dagger.Provides

@Module
class AppModule(val app: App) {

    @Provides
    fun app(): Context = app

}