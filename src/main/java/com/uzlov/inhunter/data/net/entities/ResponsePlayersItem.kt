package com.uzlov.inhunter.data.net.entities

data class ResponsePlayersItem(
    val email: String?,
    val nick: String?,
    val position: Position?,
    val type: String?,
    val whenCreated: String?
)