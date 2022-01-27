package com.uzlov.inhunter.data.net.sources.repositoies

import com.uzlov.inhunter.data.net.sources.IServerPositionsDataSource
import com.uzlov.inhunter.data.net.entities.bodies.PositionBody
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class PositionsRepositoryImpl @Inject constructor(private val dataSource: IServerPositionsDataSource) : IPositionsRepository {
    override fun updatePositions(positionBody: PositionBody): Completable {
        return dataSource.updatePositions(positionBody)
    }
}