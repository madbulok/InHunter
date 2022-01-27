package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.entities.*
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreateTeam
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreator
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import io.reactivex.rxjava3.core.Single

interface IServerTeamDataSource {
    fun getAllTeams() : Single<List<ResponseTeamsItem>>
    fun getTeamsByEmail(email: BodyEmail) : Single<List<ResponseMyTeamsItem>>
    fun joinTeam(bodyJoinUser: BodyJoinUser) : Single<ResponseJoinTeam>
    fun createTeam(bodyCreator: BodyCreator) : Single<ResponseTeamsItem>
    fun createWithJoinTeam(bodyCreateTeam: BodyCreateTeam) : Single<ResponseTeamsItem>
}