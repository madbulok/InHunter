package com.uzlov.inhunter.data.net.sources.usecases

import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyRetrieveTeam
import com.uzlov.inhunter.data.net.sources.IServerPositionsDataSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class PositionsUseCases @Inject constructor(
    private var positionsDataSource: IServerPositionsDataSource,
    private var cacheTeamsUseCases: TeamsUseCases,
) {
    private val compositeDisposable = CompositeDisposable()

    fun updatePositionsWithRetrieveTeam(
        email: String?,
        lat: Double?,
        lng: Double?,
    ): Single<List<ResponseTeamPositionItem>> {
        return  cacheTeamsUseCases.getActivatedTeamWithPass()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { compositeDisposable.add(it) }
            .flatMap {
                val s = it.teamPasswordEntity?.let { team ->
                    positionsDataSource.updatePositions(
                        BodyRetrieveTeam(
                            email = email,
                            lat = lat,
                            lng = lng,
                            pin = team.pin,
                            teamId = team.teamId,
                        )
                    )
                }
                s?.toMaybe()
            }.toSingle()
    }

    fun clear(){
        compositeDisposable.dispose()
    }
}