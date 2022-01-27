package com.uzlov.inhunter.data.net.sources.repositoies

import com.uzlov.inhunter.data.net.entities.*
import com.uzlov.inhunter.data.net.sources.IServerTeamDataSource
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreateTeam
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreator
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class TeamsRepositoryImpl @Inject constructor(private val dataSource: IServerTeamDataSource) : ITeamRepository {
    override fun getAllTeams(): Single<List<ResponseTeamsItem>> {
        return dataSource.getAllTeams()
    }

    override fun getTeamsByEmail(email: BodyEmail): Single<List<ResponseMyTeamsItem>> {
        return dataSource.getTeamsByEmail(email)
    }

    override fun joinTeam(bodyJoinUser: BodyJoinUser): Single<ResponseJoinTeam> {
        return dataSource.joinTeam(bodyJoinUser)
    }

    override fun createTeam(bodyCreator: BodyCreator): Single<ResponseTeamsItem> {
        return dataSource.createTeam(bodyCreator)
    }

    override fun createWithJoinTeam(bodyCreateTeam: BodyCreateTeam): Single<ResponseTeamsItem> {
        return dataSource.createWithJoinTeam(bodyCreateTeam)
    }
}