package com.uzlov.inhunter.data.local.database

import com.uzlov.inhunter.cache.room.TeamDao
import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.interfaces.ILocalTeamRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class RoomRepositoryImpl(private val teamDao: TeamDao) : ILocalTeamRepository {
    override fun getTeams(): Single<List<ResponseMyTeamsItem>> {
        return teamDao.getTeams().subscribeOn(Schedulers.io())
    }

    override fun getTeamsWithPass(): Maybe<MyTeamWithPassword> {
        return teamDao.getTeamWithPasswords()
    }

    override fun getTeamsWithPass(id: Int): Maybe<MyTeamWithPassword> {
        return teamDao.getTeamWithPasswords(id).subscribeOn(Schedulers.io())
    }

    override fun getTeamByID(id: Int): Single<ResponseMyTeamsItem> {
        return teamDao.getTeamByID(id).subscribeOn(Schedulers.io())
    }

    override fun delete(team: ResponseMyTeamsItem): Completable {
        return teamDao.delete(team).subscribeOn(Schedulers.io())
    }

    override fun insert(team: ResponseMyTeamsItem): Completable {
        return teamDao.insert(team).subscribeOn(Schedulers.io())
    }

    override fun insert(team: TeamPasswordEntity): Completable {
        return teamDao.insert(team).subscribeOn(Schedulers.io())
    }

    override fun insert(team: List<ResponseMyTeamsItem>): Completable {
        return teamDao.insert(team).subscribeOn(Schedulers.io())
    }

    override fun setActiveTeam(team: ResponseMyTeamsItem): Completable {
        return teamDao.setActiveTeam(team).subscribeOn(Schedulers.io())
    }

    override fun getActivatedTeam(): Maybe<ResponseMyTeamsItem> {
        return teamDao.getActivatedTeam().subscribeOn(Schedulers.io())
    }

    override fun setInactiveAllteam(): Completable {
        return teamDao.setInactiveAllTeam().subscribeOn(Schedulers.io())
    }

    override fun getActivatedTeamWithPassword(): Maybe<MyTeamWithPassword> {
        return teamDao.getActivatedTeamWithPasswords().subscribeOn(Schedulers.io())
    }

    override fun setTeamPinCode(id: Int, pin: String): Completable {
        return teamDao.setPinTeam(id, pin).subscribeOn(Schedulers.io())
    }
}