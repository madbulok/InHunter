package com.uzlov.inhunter.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.auth.IAuthListener
import com.uzlov.inhunter.data.net.entities.ResponseMyTeamsItem
import com.uzlov.inhunter.data.net.entities.bodies.BodyEmail
import com.uzlov.inhunter.data.net.sources.usecases.TeamsUseCases
import com.uzlov.inhunter.databinding.TeamFragmentBinding
import com.uzlov.inhunter.ui.TeamAdapter
import com.uzlov.inhunter.ui.dialogs.CreateTeamBottomFragment
import com.uzlov.inhunter.ui.dialogs.DialogTeamInfo
import com.uzlov.inhunter.ui.dialogs.JoinTeamBottomFragment
import com.uzlov.inhunter.ui.dialogs.showMessage
import com.uzlov.inhunter.utils.toMyTeamWithPassword
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject


class TeamsFragment : Fragment(), Toolbar.OnMenuItemClickListener {

    private var _viewBinding: TeamFragmentBinding? = null
    private val viewBinding get() = _viewBinding!!
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val teamSelectListener: TeamAdapter.OnItemSelectListener =
        object : TeamAdapter.OnItemSelectListener {
            override fun onItemSelect(team: ResponseMyTeamsItem, position: Int) {
                changeActiveTeam(team)
            }

            override fun showTeamInfo(team: ResponseMyTeamsItem) {
                teamsUseCases.getTeamWithPassById(team.id).subscribe({
                    DialogTeamInfo(it).show(childFragmentManager, "dialogInfo")
                }, {
                    DialogTeamInfo(
                        team.toMyTeamWithPassword()
                    ).show(childFragmentManager, "dialogInfo")
                }, {
                    DialogTeamInfo(
                        team.toMyTeamWithPassword()
                    ).show(childFragmentManager, "dialogInfo")
                })

            }
        }

    private val teamAdapter by lazy { TeamAdapter(teamSelectListener) }

    @Inject
    lateinit var teamsUseCases: TeamsUseCases

    @Inject
    lateinit var authService: AuthService

    companion object {
        fun newInstance(): TeamsFragment {
            return TeamsFragment()
        }
    }

    private val callbackAuthState = object : IAuthListener<GoogleSignInAccount> {
        override fun loginSuccess(account: GoogleSignInAccount) {
            loadTeams()
        }

        override fun loginFailed(e: Throwable) {}

        override fun startLogin(intent: Intent, code: Int) {}

        override fun logout() {
            teamAdapter.setTeams(emptyList())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
        authService.addLoginListener(callbackAuthState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.team_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _viewBinding = TeamFragmentBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        loadTeams()
    }

    private fun initListeners() {
        with(viewBinding) {

            teamsToolbar.setOnMenuItemClickListener(this@TeamsFragment)
            recyclerTeamView.adapter = teamAdapter
            refreshView.setOnRefreshListener {
                loadTeams()
            }

            btnJoinToTeam.setOnClickListener {
                showJoinToTeamFragment()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.app_bar_register -> {
                showCreateTeamFragment()
                return true
            }
        }
        return false
    }


    interface LoadTeamListener{
        fun load()
    }

    private val callbackLoadTeam =  object :LoadTeamListener {
        override fun load() {
            loadTeams()
        }
    }

    private fun showJoinToTeamFragment() =
        JoinTeamBottomFragment(callbackLoadTeam).show(childFragmentManager, "join")


    private fun showCreateTeamFragment() =
        CreateTeamBottomFragment(callbackLoadTeam).show(childFragmentManager, "create")

    private fun loadTeams() {
        authService.appAccount?.email?.let { email ->
            teamsUseCases.getTeamsByEmail(BodyEmail(email))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isEmpty()) {
                        showEmptyUI()
                    } else {
                        showNotEmptyUI()
                        teamAdapter.setTeams(it)
                    }
                    viewBinding.refreshView.isRefreshing = false
                }, {
                    viewBinding.refreshView.isRefreshing = false
                    it.printStackTrace()
                })
        }
    }

    private fun changeActiveTeam(team: ResponseMyTeamsItem) {
        teamsUseCases.changeActivatedTeam(team)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { compositeDisposable.add(it) }
            .subscribe({
                loadTeams()
            }, {
                showMessage(it.message ?: getString(R.string.unknown_error))
            })
    }


    private fun showEmptyUI() {
        with(viewBinding) {
            recyclerTeamView.visibility = View.GONE
            refreshView.visibility = View.INVISIBLE
            labelMessage.visibility = View.VISIBLE
        }
    }

    private fun showNotEmptyUI() {
        with(viewBinding) {
            recyclerTeamView.visibility = View.VISIBLE
            refreshView.visibility = View.VISIBLE
            labelMessage.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        compositeDisposable.dispose()
    }
}
