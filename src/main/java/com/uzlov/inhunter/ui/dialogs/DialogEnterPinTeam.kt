package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.KtGlobalProperty
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.net.sources.usecases.TeamsUseCases
import com.uzlov.inhunter.databinding.DialogEnterPinLayoutBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class DialogEnterPinTeam(private var listenerAction: OnActionListener? = null) : DialogFragment() {

    private var _viewBinding: DialogEnterPinLayoutBinding? = null
    private val viewBinding: DialogEnterPinLayoutBinding get() = _viewBinding!!

    interface OnActionListener {
        fun cancel()
        fun accept()
    }

    @Inject
    lateinit var teamsUseCases: TeamsUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
    }

    fun removeActionListener() {
        listenerAction = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBinding = DialogEnterPinLayoutBinding.inflate(layoutInflater)
        isCancelable = false

        val alert = AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

        initListeners()
        loadActivatedTeam()

        return alert
    }

    private fun initListeners() {
        with(viewBinding) {
            btnCancel.setOnClickListener {
                listenerAction?.cancel()
                dismiss()
            }

            btnAccept.setOnClickListener {
                listenerAction?.accept()
                if (TextUtils.isEmpty(etTeamPinCode.text) || etTeamPinCode.text?.length ?: 0 < KtGlobalProperty.MIN_LENGTH_PIN){
                    etTeamPinCode.error = getString(R.string.min_6_length)
                } else {
                    updatePinActivatedTeam()
                }
            }

            tvLabelDialog.setOnClickListener {
                listenerAction?.cancel()
                dismiss()
            }
        }
    }

    private fun updatePinActivatedTeam() {
        // load activated team
        teamsUseCases.getActivatedTeam()
            .subscribe({
                _viewBinding?.tvLabelDialog?.text = "Команда ${it.name}"
                teamsUseCases.updatePin(
                    id = it.id ?: 0,
                    name = it.name,
                    pin = _viewBinding?.etTeamPinCode?.text.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        dismiss()
                }, {
                        it.printStackTrace()
                        showMessage(it.message ?: "Неизвестная ошибка при обновлении PIN кода")
                })
            }, {
                it.printStackTrace()
                showMessage(it.message ?: "Неизвестная ошибка при получении активной команды")
            }, {

            })

    }

    private fun loadActivatedTeam() {
        // load activated team
        teamsUseCases.getActivatedTeam()
            .subscribe({
                _viewBinding?.tvLabelDialog?.text = "Команда ${it.name}"
            }, {
                it.printStackTrace()
            }, {
                showMessage("Активная команда не выбрана!")
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeActionListener()
        _viewBinding = null
    }
}