package com.uzlov.inhunter.data.net.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ResponseMyTeamsItem(
    @PrimaryKey val id: Int?,
    val name: String ="",
    var isActive: Boolean = false
)