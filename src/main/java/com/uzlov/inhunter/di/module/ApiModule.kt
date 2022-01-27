package com.uzlov.inhunter.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.data.net.*
import com.uzlov.inhunter.data.net.sources.*
import com.uzlov.inhunter.data.net.sources.repositoies.*
import com.uzlov.inhunter.data.net.sources.usecases.PlayerUseCases
import com.uzlov.inhunter.data.net.sources.usecases.PositionsUseCases
import com.uzlov.inhunter.data.net.sources.usecases.TeamsUseCases
import com.uzlov.inhunter.interfaces.ILocalTeamRepository
import com.uzlov.inhunter.utils.AndroidNetworkStatus
import com.uzlov.inhunter.utils.INetworkStatus
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApiModule {

    @Provides
    fun port(): String = "2048"

    @Named("baseUrl")
    @Provides
    fun baseUrl(): String = "http://83.220.174.93:${port()}/"


    // data sources
    @Singleton
    @Provides
    fun providePositionsDataSource(api: BackendApi): IServerPositionsDataSource =
        ServerPositionsDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideTeamDataSource(api: BackendApi): IServerTeamDataSource =
        ServerTeamDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideTeamUseCases(
        remoteTeamDataSource: IServerTeamDataSource,
        localTeamInteractor: ILocalTeamRepository,
        networkStatus: INetworkStatus,
    ): TeamsUseCases = TeamsUseCases(remoteTeamDataSource,
        localTeamInteractor,
        networkStatus)

    @Singleton
    @Provides
    fun providePositionsTeamUseCases(
        serverPositionDataSource: IServerPositionsDataSource,
        teamUseCases: TeamsUseCases,
    ): PositionsUseCases = PositionsUseCases(
        serverPositionDataSource,
        teamUseCases
    )

    @Singleton
    @Provides
    fun providePlayerUseCases(
        serverPositionDataSource: IServerPlayerDataSource,
        prefRepo: PreferenceRepository,
        authService: AuthService
    ): PlayerUseCases = PlayerUseCases(
        serverPositionDataSource,
        prefRepo,
        authService
    )


    @Singleton
    @Provides
    fun providePlayersDataSource(api: BackendApi): IServerPlayerDataSource =
        ServerPlayerDataSourceImpl(api)

    // repositories
    @Singleton
    @Provides
    fun providePositionsRepository(dataSource: IServerPositionsDataSource): IPositionsRepository =
        PositionsRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideTeamRepository(dataSource: IServerTeamDataSource): ITeamRepository =
        TeamsRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun providePlayersRepository(dataSource: IServerPlayerDataSource): IPlayersRepository =
        PlayersRepositoryImpl(dataSource)


    @Singleton
    @Provides
    fun api(@Named("baseUrl") baseUrl: String, gson: Gson): BackendApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client())
            .build()
            .create(BackendApi::class.java)
    }

    @Singleton
    @Provides
    fun client(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    @Singleton
    @Provides
    fun gson(): Gson = GsonBuilder()
        .create()


    @Singleton
    @Provides
    fun networkStatus(app: Context): INetworkStatus = AndroidNetworkStatus(app)
}