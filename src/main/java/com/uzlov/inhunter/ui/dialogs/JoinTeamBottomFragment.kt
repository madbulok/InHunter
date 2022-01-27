package com.uzlov.inhunter.ui.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.net.entities.bodies.BodyJoinUser
import com.uzlov.inhunter.data.net.sources.usecases.TeamsUseCases
import com.uzlov.inhunter.fragments.TeamsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class JoinTeamBottomFragment(val callbackLoadTeam: TeamsFragment.LoadTeamListener) : BottomSheetDialogFragment() {

    private lateinit var joinBtn: MaterialButton
    private lateinit var numberTeam: TextInputEditText
    private lateinit var pinTeam: TextInputEditText

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var teamsUseCases: TeamsUseCases


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.join_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        joinBtn = view.findViewById(R.id.joinBtn)
        pinTeam = view.findViewById(R.id.etTeamPIN)
        numberTeam = view.findViewById(R.id.etTeamPin)
        joinBtn.setOnClickListener {
            if (TextUtils.isEmpty(pinTeam.text)) {
                pinTeam.error = "Пустое поле!"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(numberTeam.text)) {
                numberTeam.error = "Пустое поле!"
                return@setOnClickListener
            }

            teamsUseCases.joinTeam(
                BodyJoinUser(
                    pin = pinTeam.text.toString(),
                    playerEmail = authService.appAccount?.email.toString(),
                    teamId = numberTeam.text.toString().toInt()
                )
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    callbackLoadTeam.load()
                    showMessage("Вы успешно вступили в команду!")
                    dismiss()
                }, {
                    showMessage(it.message ?: "Ошибка при вступлении в команду")
                })
        }
    }
}