package com.uzlov.inhunter.data.auth

import android.content.Intent
import com.uzlov.inhunter.interfaces.Constants

interface IAuthListener<T> {
    fun loginSuccess(account: T)
    fun loginFailed(e: Throwable)
    fun startLogin(intent: Intent, code: Int = Constants.RC_SIGN_IN)
    fun logout()
}