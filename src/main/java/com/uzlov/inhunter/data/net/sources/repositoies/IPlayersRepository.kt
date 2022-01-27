package com.uzlov.inhunter.data.net.sources.repositoies

import com.uzlov.inhunter.data.net.entities.ResponsePlayers
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import io.reactivex.rxjava3.core.Single

interface IPlayersRepository {
    fun getPlayers() : Single<List<ResponsePlayersItem>>
    fun createPlayer(user: Owner) : Single<ResponsePlayers>
}