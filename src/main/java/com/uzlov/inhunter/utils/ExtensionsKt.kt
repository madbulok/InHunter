package com.uzlov.inhunter.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.snackbar.Snackbar
import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.cache.room.entity.TeamPasswordEntity
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem

fun Fragment.showShack(
    mainTextStringId: Int, actionStringId: Int,
    listener: View.OnClickListener,
) {
    Snackbar.make(
        view?.findViewById(android.R.id.content)!!,
        resources.getString(mainTextStringId),
        Snackbar.LENGTH_INDEFINITE
    ).setAction(getString(actionStringId), listener).show()
}

fun ResponseTeamPositionItem.nickToMarker(): String {
    return email.split("@").firstOrNull() ?: email ?: "Без имени!"
}


fun ResponseTeamPositionItem.isNotNull(): Boolean {
    return lat != null && lng != null
}

fun ResponseTeamPositionItem.colorTrack(alpha: Double = 0.7) : String{
    if (email.length < 3) return "rgba(0, 0, 0, $alpha)"
    return try {
        val i = Integer.decode(email.hashCode().toString()).toInt()
        "rgba(${i shr 16 and 0xFF}, ${i shr 8 and 0xFF}, ${i and 0xFF}, $alpha)"
    } catch (e: java.lang.NumberFormatException){
        "rgba(0, 0, 0, $alpha)"
    }
}

fun GoogleSignInAccount.defaultNickname() : String = email?.split("@")?.first() ?: "Player"

fun ResponseMyTeamsItem.toMyTeamWithPassword(): MyTeamWithPassword =
    MyTeamWithPassword(
        team = ResponseMyTeamsItem(
            id = id,
            name = name,
        ),
        teamPasswordEntity = TeamPasswordEntity(
            id_pass = id ?: 0 + 1,
            name = name,
            pin = "******",
            teamId = id ?: 0
        )
    )