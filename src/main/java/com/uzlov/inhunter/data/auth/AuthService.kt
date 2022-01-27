package com.uzlov.inhunter.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.uzlov.inhunter.interfaces.Constants
import java.lang.Exception

class AuthService(private val context: Context) : IAuthState {

    private val authListeners = mutableListOf<IAuthListener<GoogleSignInAccount>?>()

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var account: GoogleSignInAccount? = null
    val appAccount get() = account
    val isAuthSuccess get() = account != null

    init {
        createAuthConnection()
        account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            loginSuccess(account!!)
        } else {
            loginFailed(Exception("Failed log in Google account!"))
        }
    }

    fun addLoginListener(_loginListener: IAuthListener<GoogleSignInAccount>) {
        authListeners.add(_loginListener)
    }

    fun startLogin(intent: Intent, code: Int = Constants.RC_SIGN_IN) {
        authListeners.forEach {
            it?.startLogin(intent, code)
        }
    }

    fun loginSuccess(account: GoogleSignInAccount) {
        setAccount(account)
        authListeners.forEach { listener ->
            listener?.loginSuccess(account)
        }
    }

    fun loginFailed(error: Throwable?) {
        authListeners.forEach { listener ->
            listener?.loginFailed(error!!)
        }
    }

    fun logout() {
        authListeners.forEach { listener ->
            listener?.logout()
        }
    }

    fun removeLoginListener() {
        authListeners.clear()
    }

    private fun createAuthConnection() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn() {
        if (account == null) {
            mGoogleSignInClient?.signInIntent?.let { signInIntent ->
                startLogin(signInIntent)
            }
        } else {
            loginSuccess(account!!)
        }
    }

    fun handleSignInResult(data: Intent?) {
        try {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener {
                    loginSuccess(it)
                }.addOnFailureListener {
                    loginFailed(it)
                }
        } catch (e: ApiException) {
            loginFailed(e)
        }
    }

    fun logoutUser() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener {
            logout()
            account = null
        }
    }


    override fun isAuth(): Boolean {
        return account != null
    }

    fun setAccount(account: GoogleSignInAccount) {
        this.account = account
    }

}