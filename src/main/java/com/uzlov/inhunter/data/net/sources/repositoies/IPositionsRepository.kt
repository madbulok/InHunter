package com.uzlov.inhunter.data.net.sources.repositoies

import com.uzlov.inhunter.data.net.entities.bodies.PositionBody
import io.reactivex.rxjava3.core.Completable

interface IPositionsRepository {
    fun updatePositions(positionBody: PositionBody) : Completable
}