package com.uzlov.inhunter.map


interface LoadableRegion<T> {
    fun loadRegion(regions: List<T>)
    fun onError(message : String)
}