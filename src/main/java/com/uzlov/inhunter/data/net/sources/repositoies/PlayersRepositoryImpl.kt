package com.uzlov.inhunter.data.net.sources.repositoies

import com.uzlov.inhunter.data.net.sources.IServerPlayerDataSource
import com.uzlov.inhunter.data.net.entities.ResponsePlayers
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class PlayersRepositoryImpl @Inject constructor(private val dataSource: IServerPlayerDataSource) : IPlayersRepository {
    override fun getPlayers(): Single<List<ResponsePlayersItem>> {
        return dataSource.getPlayers()
    }

    override fun createPlayer(user: Owner): Single<ResponsePlayers> {
        return dataSource.createPlayer(user)
    }
}