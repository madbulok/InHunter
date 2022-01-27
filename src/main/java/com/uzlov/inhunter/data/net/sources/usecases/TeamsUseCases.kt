package com.uzlov.inhunter.data.net.sources.usecases

import android.util.Log
import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity
import com.uzlov.inhunter.data.net.entities.*
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreateTeam
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreator
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import com.uzlov.inhunter.utils.INetworkStatus
import com.uzlov.inhunter.data.net.sources.IServerTeamDataSource
import com.uzlov.inhunter.interfaces.ILocalTeamRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.RuntimeException
import javax.inject.Inject

class TeamsUseCases @Inject constructor(
    private val remoteTeamDataSource: IServerTeamDataSource,
    private val localTeamInteractor: ILocalTeamRepository,
    private val networkStatus: INetworkStatus,
) {
    private val TAG = javaClass.simpleName

    private val compositeDisposable = CompositeDisposable()

    fun getAllTeams(): Single<List<ResponseTeamsItem>> =
        networkStatus.isOnlineSingle().flatMap { isOnline ->
            if (isOnline) {
                remoteTeamDataSource.getAllTeams().subscribeOn(Schedulers.io())
            } else {
                localTeamInteractor.getTeams()
                    .subscribeOn(Schedulers.io())
                    .map {
                        it.map { cachedTeam ->
                            ResponseTeamsItem(
                                id = cachedTeam.id,
                                name = cachedTeam.name,
                                ownerEmail = "",
                                whenCreated = "",
                                isActive = cachedTeam.isActive
                            )
                        }
                    }
            }
        }.subscribeOn(Schedulers.io())

    fun getTeamsByEmail(email: BodyEmail): Single<List<ResponseMyTeamsItem>> {
        return networkStatus.isOnlineSingle().flatMap { isOnline ->
            if (isOnline) {
                // загружаем команды с сервера
                remoteTeamDataSource.getTeamsByEmail(email)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { remoteTeams ->
                        // получили
                        // берем активную команду из БД
                        localTeamInteractor.getActivatedTeam()
                            .subscribeOn(Schedulers.io())
                            .doOnError { throw RuntimeException(it) }
                            .doOnSuccess { activatedTeam ->
                                // есть активный
                                remoteTeams.filter {
                                    // находим эту команду с сервера
                                    it.id == activatedTeam.id
                                }.forEach {
                                    // ставим её активной
                                    it.isActive = true
                                }
                            }.subscribe({

                            }, {
                                it.printStackTrace()
                            }, {
                                localTeamInteractor.insert(remoteTeams).subscribeOn(Schedulers.io())
                                    .subscribe()
                            })
                    }
            } else {
                localTeamInteractor.getTeams()
                    .subscribeOn(Schedulers.io())
            }
        }
    }

    fun changeActivatedTeam(team: ResponseMyTeamsItem): Completable {
        team.isActive = !team.isActive
        return localTeamInteractor.setInactiveAllteam()
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                localTeamInteractor.setActiveTeam(team).subscribe()
            }.doOnError {
                throw RuntimeException(it)
            }
    }

    fun getActivatedTeam(): Maybe<ResponseMyTeamsItem> {
        return localTeamInteractor.getActivatedTeam()
            .subscribeOn(Schedulers.io())
    }

    fun joinTeam(bodyJoinUser: BodyJoinUser): Single<ResponseJoinTeam> {
        return remoteTeamDataSource.joinTeam(bodyJoinUser)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                // сохраняем команду в БД
                saveTeamLocal(ResponseMyTeamsItem(it.teamId, it.teamName))
                savePasswordLocal(TeamPasswordEntity(
                    id_pass = it.teamId ?: 0 + 1,
                    name = it.teamName,
                    pin = bodyJoinUser.pin ?: "",
                    teamId = it.teamId ?: 0
                )).subscribe({
                    Log.e(TAG, "saveTeamLocal: ")
                }, {
                    it.printStackTrace()
                })
            }
    }

    fun createTeam(bodyCreator: BodyCreator): Single<ResponseTeamsItem> {
        return remoteTeamDataSource.createTeam(bodyCreator)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                // сохраняем команду в БД
                saveTeamLocal(ResponseMyTeamsItem(it.id, it.name))

                // сохраняем пароль в БД
                savePasswordLocal(TeamPasswordEntity(
                    id_pass = it.id ?: 0 + 1,
                    name = bodyCreator.name ?: "",
                    pin = bodyCreator.pin ?: "",
                    teamId = it.id ?: 0
                )).subscribe({
                    Log.e(TAG, "saveTeamLocal: ")
                }, {
                    it.printStackTrace()
                })
            }
    }

    private fun saveTeamLocal(team: ResponseMyTeamsItem) {
        localTeamInteractor.insert(team)
            .subscribeOn(Schedulers.io())
            .doOnError {
                throw RuntimeException(it)
            }.subscribe({
                Log.e(TAG, "saveTeamLocal: ")
            }, {
                it.printStackTrace()
            })
    }

    private fun savePasswordLocal(team: TeamPasswordEntity) :Completable {
        return localTeamInteractor.insert(team)
            .subscribeOn(Schedulers.io())
            .doOnError {
                throw RuntimeException(it)
            }
    }

    fun createWithJoinTeam(bodyCreateTeam: BodyCreateTeam): Single<ResponseTeamsItem> {
        return remoteTeamDataSource.createWithJoinTeam(bodyCreateTeam)
            .subscribeOn(Schedulers.io())
    }

    fun getActivatedTeamWithPass() = localTeamInteractor.getActivatedTeamWithPassword()

    fun getTeamWithPassById(id: Int?): Maybe<MyTeamWithPassword> {
        id?.let {
            return localTeamInteractor.getTeamsWithPass(it)
                .observeOn(AndroidSchedulers.mainThread())
        }
        return Maybe.empty()
    }

    fun updatePin(id: Int,name: String, pin: String) : Completable {
        return savePasswordLocal(TeamPasswordEntity(
                id_pass = id + 1,
                name = name,
                pin = pin,
                teamId = id
            )).subscribeOn(Schedulers.io())
    }

    fun clear() {
        compositeDisposable.dispose()
    }
}