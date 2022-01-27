package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.BackendApi
import com.uzlov.inhunter.data.net.entities.ResponsePlayers
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ServerPlayerDataSourceImpl @Inject constructor(private val api: BackendApi) : IServerPlayerDataSource {
    override fun getPlayers(): Single<List<ResponsePlayersItem>> {
        return api.allPlayers()
    }

    override fun createPlayer(user: Owner): Single<ResponsePlayers> {
        return api.createPlayer(user)
    }

    override fun putPlayer(user: Owner): Completable {
        return api.putPlayer(user)
    }

    override fun removePlayer(user: BodyEmail): Completable {
        return api.deletePlayer(user)
    }
}