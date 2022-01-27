package com.uzlov.inhunter.interfaces

import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface ILocalTeamRepository {
    fun getTeams() : Single<List<ResponseMyTeamsItem>>
    fun getTeamsWithPass() : Maybe<MyTeamWithPassword>
    fun getTeamByID(id: Int) : Single<ResponseMyTeamsItem>
    fun delete(team: ResponseMyTeamsItem) : Completable
    fun insert(team: ResponseMyTeamsItem) : Completable
    fun insert(team: TeamPasswordEntity) : Completable
    fun insert(team: List<ResponseMyTeamsItem>) : Completable
    fun setActiveTeam(team: ResponseMyTeamsItem) : Completable
    fun getActivatedTeam() : Maybe<ResponseMyTeamsItem>
    fun setInactiveAllteam() : Completable
    fun getActivatedTeamWithPassword(): Maybe<MyTeamWithPassword>
    fun getTeamsWithPass(id: Int): Maybe<MyTeamWithPassword>
    fun setTeamPinCode(id: Int, pin: String) : Completable
}