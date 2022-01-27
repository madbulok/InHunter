package com.uzlov.inhunter.data.net

import com.uzlov.inhunter.data.net.entities.*
import com.uzlov.inhunter.data.net.entities.bodies.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface BackendApi {

    // возвращает всех игроков
    @GET("Players")
    fun allPlayers(): Single<List<ResponsePlayersItem>>

    //
//    // создает переданного игрока
    @POST("Players")
    fun createPlayer(@Body user: Owner): Single<ResponsePlayers>

    @PUT("Players")
    fun putPlayer(@Body user: Owner): Completable

    @HTTP(method = "DELETE", path = "Players", hasBody = true)
    fun deletePlayer(@Body email: BodyEmail): Completable

    // обновляет координаты игрока на сервере
    @PUT("Positions")
    fun updatePositions(@Body positionBody: PositionBody): Completable

    @PUT("Positions/RetTeammates")
    fun getTeammates(@Body positionBody: BodyRetrieveTeam): Single<List<ResponseTeamPositionItem>>

    // возвращает список всех команд
    @GET("Teams")
    fun getAllTeams(): Single<List<ResponseTeamsItem>>

    //    // создание команды
    @POST("Teams")
    fun createTeam(@Body bodyCreator: BodyCreator): Single<ResponseTeamsItem>

    // возвращает список команд где игрок с указанным email состоит
    @POST("Teams/My")
    fun getMyTeams(@Body email: BodyEmail): Single<List<ResponseMyTeamsItem>>

    // метод вступления в команду
    @POST("Teams/Join")
    fun jointToTeam(@Body bodyJoinUser: BodyJoinUser): Single<ResponseJoinTeam>

    // создает команду с переданными параметрами и создатель дополнительно становится участником
    @POST("Teams/WithOwner")
    fun createAndJoinToTeam(@Body bodyCreateTeam: BodyCreateTeam): Single<ResponseTeamsItem>
}

// http://83.220.174.93:2048/Policy/PrivacyPolicy
// http://83.220.174.93:2048/Policy/TermsOfUse