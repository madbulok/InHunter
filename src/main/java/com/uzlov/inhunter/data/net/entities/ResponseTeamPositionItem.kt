package com.uzlov.inhunter.data.net.entities

data class ResponseTeamPositionItem(
    var email: String = "",
    var lat: Double?,
    var lng: Double?,
    var player: String = "",
    var whenUpdated: String = ""
)