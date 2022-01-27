package com.uzlov.inhunter.app

import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem

sealed class HuntingDataState {
    data class Result(val result: List<ResponseTeamPositionItem>) : HuntingDataState()
    data class Error(val error: Throwable) : HuntingDataState()
}