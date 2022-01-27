package com.uzlov.inhunter.data.net.sources

import com.uzlov.inhunter.data.net.entities.*
import com.uzlov.inhunter.data.net.BackendApi
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreateTeam
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreator
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class ServerTeamDataSourceImpl @Inject constructor(private val api: BackendApi) : IServerTeamDataSource {
    override fun getAllTeams(): Single<List<ResponseTeamsItem>> {
        return api.getAllTeams().subscribeOn(Schedulers.io())
    }

    override fun getTeamsByEmail(email: BodyEmail): Single<List<ResponseMyTeamsItem>> {
        return api.getMyTeams(email).subscribeOn(Schedulers.io())
    }

    override fun joinTeam(bodyJoinUser: BodyJoinUser): Single<ResponseJoinTeam> {
        return api.jointToTeam(bodyJoinUser).subscribeOn(Schedulers.io())
    }

    override fun createTeam(bodyCreator: BodyCreator): Single<ResponseTeamsItem> {
        return api.createTeam(bodyCreator).subscribeOn(Schedulers.io())
    }

    override fun createWithJoinTeam(bodyCreateTeam: BodyCreateTeam): Single<ResponseTeamsItem> {
        return api.createAndJoinToTeam(bodyCreateTeam).subscribeOn(Schedulers.io())
    }
}