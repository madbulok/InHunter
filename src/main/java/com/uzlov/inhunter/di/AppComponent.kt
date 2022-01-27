package com.uzlov.inhunter.di

import com.uzlov.inhunter.activities.HostActivity
import com.uzlov.inhunter.di.module.*
import com.uzlov.inhunter.fragments.*
import com.uzlov.inhunter.ui.dialogs.*
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        AuthModule::class,
        LocalModule::class,
        ViewModelModule::class,
        MapModule::class,
    ]
)
interface AppComponent {
    fun inject(repositoryDataImpl: HostActivity)
    fun inject(loginFragment: LoginFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(mapFragment: MapFragment)
    fun inject(joinTeamBottomFragment: JoinTeamBottomFragment)
    fun inject(createTeamBottomFragment: CreateTeamBottomFragment)
    fun inject(teamsFragment: TeamsFragment)
    fun inject(dialogAcceptTypeMap: DialogAcceptTypeMap)
    fun inject(dialogInputNameLoadedMap: DialogStartLoadingMap)
    fun inject(dialogEnterPinTeam: DialogEnterPinTeam)
}