package com.uzlov.inhunter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.auth.IAuthListener
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.data.net.entities.ResponsePlayersItem
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import com.uzlov.inhunter.data.net.sources.usecases.PlayerUseCases
import com.uzlov.inhunter.interfaces.Constants
import com.uzlov.inhunter.utils.defaultNickname
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LoginFragment : Fragment() {

    private var loginButton: SignInButton? = null

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    @Inject
    lateinit var authService: AuthService


    @Inject
    lateinit var playerUseCases: PlayerUseCases

    private val authListener by lazy {
        object : IAuthListener<GoogleSignInAccount> {
            override fun loginSuccess(account: GoogleSignInAccount) {


                playerUseCases.createPlayer(
                    Owner(
                        email = account.email,
                        nick = account.defaultNickname(),
                        type = preferenceRepository.getActiveType(),
                    )
                ).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // запрос данных с remote source
                        playerUseCases.getSelfProfile()
                            .subscribe({
                                val localProfile = preferenceRepository.readOwner().apply {
                                    nick = it.nick
                                    type = it.type
                                    email = it.email
                                }

                                preferenceRepository.updateOwner(localProfile)
                            }, {
                                it.printStackTrace()
                                val localProfile = preferenceRepository.readOwner().apply {
                                    nick = account.defaultNickname()
                                    type = ""
                                    email = account.email
                                }

                                preferenceRepository.updateOwner(localProfile)
                            }, {

                                val localProfile = preferenceRepository.readOwner().apply {
                                    nick = account.defaultNickname()
                                    type = ""
                                    email = account.email
                                }

                                playerUseCases.createPlayer(localProfile)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({

                                    }, {

                                    })
                                preferenceRepository.updateOwner(localProfile)
                            })

                    }, {
                        it.printStackTrace()
                    })


                authService.setAccount(account)

                parentFragmentManager.setFragmentResult(Constants.ACTION_CHANGE_FRAGMENT,
                    bundleOf(Constants.ACTION_CHANGE_FRAGMENT to "login_ok"))
            }

            override fun loginFailed(e: Throwable) {
                Log.e("TAG", e.message.toString())
            }

            override fun startLogin(intent: Intent, code: Int) {
                startActivityForResult(intent, code)
            }

            override fun logout() {
                parentFragmentManager.setFragmentResult(Constants.ACTION_CHANGE_FRAGMENT,
                    bundleOf(Constants.ACTION_CHANGE_FRAGMENT to "logout"))
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.google_signin_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authService.addLoginListener(authListener)

        loginButton = view.findViewById(R.id.login)
        loginButton?.setOnClickListener {
            authService.signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RC_SIGN_IN && data != null) {
            authService.handleSignInResult(data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginButton = null
        authService.removeLoginListener()
    }
}