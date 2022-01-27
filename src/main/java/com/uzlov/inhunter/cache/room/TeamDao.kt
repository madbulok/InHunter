package com.uzlov.inhunter.cache.room

import androidx.room.*
import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface TeamDao {

    @Query("SELECT * FROM ResponseMyTeamsItem")
    fun getTeams(): Single<List<ResponseMyTeamsItem>>

    @Query("SELECT * FROM ResponseMyTeamsItem WHERE id=:id")
    fun getTeamByID(id: Int): Single<ResponseMyTeamsItem>

    @Query("SELECT * FROM ResponseMyTeamsItem WHERE isActive=1")
    fun getActivatedTeam(): Maybe<ResponseMyTeamsItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(team: ResponseMyTeamsItem): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(teams: List<ResponseMyTeamsItem>): Completable

    @Delete
    fun delete(team: ResponseMyTeamsItem): Completable

    @Delete
    fun delete(vararg team: ResponseMyTeamsItem): Completable

    @Delete
    fun delete(teams: List<ResponseMyTeamsItem>): Completable

    @Query("UPDATE ResponseMyTeamsItem SET isActive=1 WHERE id=:teamId")
    fun setActiveTeam(teamId: Int): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun setActiveTeam(teamId: ResponseMyTeamsItem): Completable

    @Query("UPDATE ResponseMyTeamsItem SET isActive=0 WHERE id > 0")
    fun setInactiveAllTeam(): Completable


    // queries for password entities
    @Query("SELECT * FROM TeamPasswordEntity WHERE teamId = :id")
    fun getEntityPassword(id: Int): Maybe<TeamPasswordEntity>

    @Delete
    fun delete(entity: TeamPasswordEntity): Completable

    @Insert
    fun insert(entity: TeamPasswordEntity): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: TeamPasswordEntity): Completable

    @Transaction
    @Query("SELECT * FROM ResponseMyTeamsItem")
    fun getTeamWithPasswords(): Maybe<MyTeamWithPassword>

    @Transaction
    @Query("SELECT * FROM ResponseMyTeamsItem WHERE id = :id")
    fun getTeamWithPasswords(id: Int): Maybe<MyTeamWithPassword>

    @Transaction
    @Query("SELECT * FROM ResponseMyTeamsItem WHERE isActive=1")
    fun getActivatedTeamWithPasswords(): Maybe<MyTeamWithPassword>

    @Query("UPDATE TeamPasswordEntity SET pin=:pin WHERE teamId=:id")
    fun setPinTeam(id: Int, pin: String): Completable
}