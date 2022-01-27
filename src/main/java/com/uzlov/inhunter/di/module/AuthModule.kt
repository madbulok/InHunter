package com.uzlov.inhunter.di.module

import android.content.Context
import com.uzlov.inhunter.app.App
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.auth.IAuthState
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthModule {

//    @Singleton
//    @Provides
//    fun authState(app: App) : IAuthState = AuthService(app)

    @Singleton
    @Provides
    fun authService(app: Context) : AuthService = AuthService(app)

}