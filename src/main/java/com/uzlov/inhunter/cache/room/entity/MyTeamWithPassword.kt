package com.uzlov.inhunter.cache.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem

data class MyTeamWithPassword(
    @Embedded val team: ResponseMyTeamsItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "teamId"
    )
    var teamPasswordEntity: TeamPasswordEntity? = TeamPasswordEntity(id_pass = 0, teamId = 0)
)