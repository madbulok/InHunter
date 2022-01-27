package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.entities.ResponsePlayers
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


interface IServerPlayerDataSource {
    fun getPlayers() : Single<List<ResponsePlayersItem>>
    fun createPlayer(user: Owner) : Single<ResponsePlayers>
    fun putPlayer(user: Owner) : Completable
    fun removePlayer(user: BodyEmail) : Completable
}