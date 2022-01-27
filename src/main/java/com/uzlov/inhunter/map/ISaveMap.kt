package com.uzlov.inhunter.map

interface ISaveMap {

    fun onSuccess()
    fun progressChanged(percent: Int)
    fun onError(message: String)

}