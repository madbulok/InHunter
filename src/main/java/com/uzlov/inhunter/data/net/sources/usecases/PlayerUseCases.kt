package com.uzlov.inhunter.data.net.sources.usecases

import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.data.net.entities.ResponsePlayers
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import com.uzlov.inhunter.data.net.sources.IServerPlayerDataSource
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class PlayerUseCases @Inject constructor(
    private val playerSource: IServerPlayerDataSource,
    private val pref: PreferenceRepository,
    private val authService: AuthService,
) {

    private val ioSchedulers = Schedulers.io()

    fun getPlayers(): Single<List<ResponsePlayersItem>> = playerSource.getPlayers().subscribeOn(ioSchedulers)

    fun createPlayer(user: Owner): Single<ResponsePlayers> = playerSource.createPlayer(user).subscribeOn(ioSchedulers)

    fun getSelfProfile(): Maybe<ResponsePlayersItem> {
        val email = authService.appAccount?.email
        return Maybe.create<ResponsePlayersItem> { emmiter ->
            getPlayers().subscribeOn(Schedulers.io())
                .subscribe({ players ->
                    val self = players.firstOrNull { player ->
                        player.email == email
                    }
                    if (self != null) {
                        emmiter.onSuccess(self)
                    } else {
                        emmiter.onComplete()
                    }
                }, {
                    emmiter.onError(it)
                })
        }.subscribeOn(ioSchedulers)
    }

    fun updatePlayer(user: Owner) = playerSource.putPlayer(user)

    fun removePlayer(email: String): Completable {
        return playerSource.removePlayer(BodyEmail(email)).subscribeOn(ioSchedulers)
    }

}