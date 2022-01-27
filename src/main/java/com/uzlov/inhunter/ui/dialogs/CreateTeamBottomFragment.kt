package com.uzlov.inhunter.ui.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.KtGlobalProperty
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.net.entities.ResponseTeamsItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyCreator
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import com.uzlov.inhunter.data.net.sources.usecases.TeamsUseCases
import com.uzlov.inhunter.databinding.CreateTeamLayoutBinding
import com.uzlov.inhunter.fragments.TeamsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject


class CreateTeamBottomFragment(val callbackLoadTeam: TeamsFragment.LoadTeamListener) :
    BottomSheetDialogFragment() {

    private var _viewBinding: CreateTeamLayoutBinding? = null
    private val viewBinding: CreateTeamLayoutBinding get() = _viewBinding!!
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var authService: AuthService


    @Inject
    lateinit var teamsUseCases: TeamsUseCases


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        requireContext().appComponent.inject(this)
        _viewBinding = CreateTeamLayoutBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            createBtn.setOnClickListener {
                if (TextUtils.isEmpty(etTeamUID.text) || etTeamUID.text?.length ?: 0 < KtGlobalProperty.MIN_LENGTH_NAME) {
                    etTeamUID.error = getString(R.string.min_4_length)
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(etTeamPin.text) || etTeamPin.text?.length ?: 0 < KtGlobalProperty.MIN_LENGTH_PIN) {
                    etTeamPin.error = getString(R.string.min_6_length)
                    return@setOnClickListener
                }
                val nameTeam = etTeamUID.text.toString()
                val pinTeam = etTeamPin.text.toString()
                teamsUseCases.createTeam(
                    BodyCreator(
                        name = nameTeam,
                        ownerEmail = authService.appAccount?.email ?: "",
                        pin = pinTeam
                    )
                ).doOnSubscribe {
                    compositeDisposable.add(it)
                }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ createdTeam ->
                        joinToTeam(createdTeam, pinTeam)

                    }, {
                        showMessage(getString(R.string.error_while_enter_team))
                        it.printStackTrace()
                    })
            }
        }
    }

    private fun joinToTeam(team: ResponseTeamsItem, pin: String) {
        teamsUseCases.joinTeam(
            BodyJoinUser(
                pin = pin,
                playerEmail = authService.appAccount?.email ?: "",
                teamId = team.id
            )
        ).doOnSubscribe {
            compositeDisposable.add(it)
        }.subscribe({
            callbackLoadTeam.load()
            dismiss()
        }, {
            it.printStackTrace()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        compositeDisposable.dispose()
    }
}

fun Fragment.showMessage(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}