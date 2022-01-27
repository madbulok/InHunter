package com.uzlov.inhunter.cache.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TeamPasswordEntity(
    @PrimaryKey var id_pass: Int,
                var name: String = "",
                var pin: String = "",
                var teamId: Int
)