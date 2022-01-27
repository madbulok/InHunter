package com.uzlov.inhunter.app

import android.app.Application
import android.content.Context
import com.uzlov.inhunter.di.*
import com.uzlov.inhunter.di.module.AppModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        // а это оставь
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}

val Context.appComponent: AppComponent
    get() = when (this) {
        is App -> appComponent
        else -> this.applicationContext.appComponent
    }