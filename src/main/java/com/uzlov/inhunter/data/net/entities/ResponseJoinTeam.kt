package com.uzlov.inhunter.data.net.entities

data class ResponseJoinTeam(
    val playerEmail: String?,
    val teamId: Int?,
    val teamName: String = ""
)