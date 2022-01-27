package com.uzlov.inhunter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.uzlov.inhunter.R
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.databinding.TeamItemLayoutBinding

class TeamAdapter(private val selectListener: OnItemSelectListener?= null, private val visibilityInfo: Int = View.VISIBLE): RecyclerView.Adapter<TeamAdapter.TeamHolder>() {

    interface OnItemSelectListener {
        fun onItemSelect(team: ResponseMyTeamsItem, position: Int)
        fun showTeamInfo(team: ResponseMyTeamsItem)
    }

    private val teams = mutableListOf<ResponseMyTeamsItem>()
    private var _viewBinding: TeamItemLayoutBinding? = null
    private val viewBinding get() = _viewBinding!!

    fun setTeams(_teams: List<ResponseMyTeamsItem>) {
        teams.run {
            clear()
            addAll(_teams)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamHolder {
        val inflater = LayoutInflater.from(parent.context)
        _viewBinding = TeamItemLayoutBinding.inflate(inflater, parent, false)
        return TeamHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: TeamHolder, position: Int) {

        teams[position].also {
            if (it.isActive){
                holder.onBindActivated(it)
            } else {
                holder.onBind(it)
            }
        }
    }

    override fun getItemCount(): Int = teams.size

    inner class TeamHolder(private val binding: TeamItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(team: ResponseMyTeamsItem) {
            with(binding) {
                tvNameTeam.text = team.name
                btnShowInfo.visibility = visibilityInfo
                btnShowInfo.setOnClickListener {
                    if (adapterPosition != NO_POSITION){
                        selectListener?.showTeamInfo(team)
                    }
                }
                tvNameTeam.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(root.context,
                    R.drawable.ic_check_not_selected), null, null, null)
                tvNameTeam.setOnClickListener {
                    if (adapterPosition != NO_POSITION){
                        selectListener?.onItemSelect(team, adapterPosition)
                    }
                }
            }
        }

        fun onBindActivated(team: ResponseMyTeamsItem) {
            with(binding) {
                tvNameTeam.text = team.name
                btnShowInfo.visibility = visibilityInfo
                btnShowInfo.setOnClickListener {
                    selectListener?.showTeamInfo(team)
                }
                tvNameTeam.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(root.context,
                    R.drawable.ic_check_selected), null, null, null)
                tvNameTeam.setOnClickListener {
                    if (adapterPosition != NO_POSITION) {
                        selectListener?.onItemSelect(team, adapterPosition)
                    }
                }
            }
        }
    }
}