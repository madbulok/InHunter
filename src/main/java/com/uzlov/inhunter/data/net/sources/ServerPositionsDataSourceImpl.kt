package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyRetrieveTeam
import com.uzlov.inhunter.data.net.BackendApi
import com.uzlov.inhunter.data.net.entities.bodies.PositionBody
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ServerPositionsDataSourceImpl @Inject constructor(private val api: BackendApi) :
    IServerPositionsDataSource {
    // Обновляет свои координаты на сервере
    override fun updatePositions(positionBody: PositionBody): Completable =
        api.updatePositions(positionBody)
    // Обновляет свои координаты на сервере и получает координаты других участников
    override fun updatePositions(positionBody: BodyRetrieveTeam): Single<List<ResponseTeamPositionItem>> =
        api.getTeammates(positionBody)
}