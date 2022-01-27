package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.uzlov.inhunter.R
import com.uzlov.inhunter.cache.room.entity.MyTeamWithPassword
import com.uzlov.inhunter.databinding.DialogTeamInfoBinding

class DialogTeamInfo(private val team: MyTeamWithPassword) : DialogFragment() {

    private var viewBinding: DialogTeamInfoBinding? = null
    private val clipboardManager by lazy {
        requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogTeamInfoBinding.inflate(layoutInflater)

        val alert = AlertDialog.Builder(requireContext())
            .setView(viewBinding?.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

        viewBinding?.let {
            with(it) {
                tvNameTeam.text = team.team.name
                tvIdTeam.text = team.team.id.toString()
                tvPinTeam.text = team.teamPasswordEntity?.pin
            }
        }

        initListeners()
        return alert
    }


    private fun initListeners() {
        viewBinding?.tvLabelDialog?.setOnClickListener {
            dismiss()
        }

        viewBinding?.btnCopyData?.setOnClickListener {
            val clipdata: ClipData = ClipData.newPlainText("Team",
                " ${team.team.name} \n ${team.team.id} \n ${team.teamPasswordEntity?.pin} ")
            clipboardManager.setPrimaryClip(clipdata)
            showMessage(getString(R.string.data_copied_to_buffer))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}