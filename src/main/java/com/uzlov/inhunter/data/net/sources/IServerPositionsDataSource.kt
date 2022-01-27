package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyRetrieveTeam
import com.uzlov.inhunter.data.net.entities.bodies.PositionBody
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.Response

interface IServerPositionsDataSource {
    fun updatePositions(positionBody: PositionBody) : Completable
    fun updatePositions(positionBody: BodyRetrieveTeam) : Single<List<ResponseTeamPositionItem>>
}