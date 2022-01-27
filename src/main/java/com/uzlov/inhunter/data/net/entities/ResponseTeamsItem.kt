package com.uzlov.inhunter.data.net.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ResponseTeamsItem(
    @PrimaryKey val id: Int?,
    val name: String = "",
    val ownerEmail: String?,
    val whenCreated: String?,
    var isActive: Boolean = false
)