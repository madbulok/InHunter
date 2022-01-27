package com.uzlov.inhunter.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.auth.IAuthListener
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.fragments.LoginFragment
import com.uzlov.inhunter.fragments.MapFragment
import com.uzlov.inhunter.fragments.ProfileFragment
import com.uzlov.inhunter.fragments.TeamsFragment
import com.uzlov.inhunter.interfaces.Constants
import javax.inject.Inject


class HostActivity : AppCompatActivity(), FragmentResultListener,
    SharedPreferences.OnSharedPreferenceChangeListener, MapFragment.IHuntStateListener {

    private val settings: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private var bottomNavigation: BottomNavigationView? = null

    private val authListener = object : IAuthListener<GoogleSignInAccount> {
        override fun loginSuccess(account: GoogleSignInAccount) {
            showUI()
            setActiveFragment(mapFragment, R.id.menu_map)
            profileFragment.loginSuccess(account)
        }

        override fun loginFailed(e: Throwable) {
            hideUI()
            setActiveFragment(loginFragment)
        }

        override fun startLogin(intent: Intent, code: Int) {}

        override fun logout() {
            hideUI()
            setActiveFragment(loginFragment)
        }
    }

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var pref: PreferenceRepository

    // fragments
    private val loginFragment by lazy {
        LoginFragment()
    }

    private val mapFragment by lazy {
        MapFragment.newInstance()
    }
    private val teamFragment by lazy {
        TeamsFragment.newInstance()
    }

    private val profileFragment by lazy {
        ProfileFragment.newInstance()
    }

    private var currentFragment: Fragment = mapFragment

    private var bottomItemSelectListener: BottomNavigationView.OnNavigationItemSelectedListener?=
        BottomNavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_teams -> {
                    setActiveFragment(teamFragment)
                    true
                }
                R.id.menu_map -> {
                    setActiveFragment(mapFragment)
                    true
                }
                R.id.menu_profile -> {
                    setActiveFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

    private var nullItemSelectListener: BottomNavigationView.OnNavigationItemSelectedListener?=
        BottomNavigationView.OnNavigationItemSelectedListener { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        applicationContext.appComponent.inject(this)

        initUI()
        prepareFragments()
        initListeners()

        supportFragmentManager.setFragmentResultListener(
            Constants.ACTION_CHANGE_FRAGMENT,
            this,
            this
        )
    }

    private fun initListeners() {
        settings.registerOnSharedPreferenceChangeListener(this)

        authService.addLoginListener(authListener)
        bottomNavigation?.setOnNavigationItemSelectedListener(bottomItemSelectListener)
    }


    private fun initUI() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.itemIconTintList = null
    }


    private fun setActiveFragment(fragment: Fragment, menu: Int = 0) {
        if (menu != 0) bottomNavigation?.selectedItemId = menu

        supportFragmentManager.beginTransaction()
            .hide(currentFragment)
            .show(fragment)
            .commit()

        currentFragment = fragment
    }

    private fun prepareFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, teamFragment, "teamFragment").hide(teamFragment)
            .add(R.id.fragment_container, profileFragment, "profileFragment").hide(profileFragment)
            .add(R.id.fragment_container, loginFragment, "loginFragment").hide(loginFragment)
            .add(R.id.fragment_container, mapFragment, "mapFragment").hide(mapFragment)
            .setReorderingAllowed(true)
            .commit()
        if (authService.isAuthSuccess) {
            showUI()
            setActiveFragment(mapFragment, R.id.menu_map)
        } else {
            hideUI()
            currentFragment = loginFragment
            setActiveFragment(loginFragment)
        }
    }

    private fun hideUI() {
        bottomNavigation?.visibility = View.INVISIBLE
        bottomNavigation?.isEnabled = false
    }

    private fun showUI() {
        bottomNavigation?.visibility = View.VISIBLE
        bottomNavigation?.isEnabled = true
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            Constants.ACTION_CHANGE_FRAGMENT -> {
                when (result.getString(Constants.ACTION_CHANGE_FRAGMENT, "")) {
                    "mapFragment" -> setActiveFragment(mapFragment, R.id.menu_map)
                    "profileFragment" -> setActiveFragment(profileFragment, R.id.menu_profile)
                    "login_ok" -> {
                        showUI()
                        setActiveFragment(mapFragment, R.id.menu_map)
                    }
                    "logout" -> {
                        hideUI()
                        setActiveFragment(loginFragment)
                    }
                    else -> setActiveFragment(mapFragment, R.id.menu_map)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Constants.ACTION_GET_USER_UUID -> {
                val uuid = settings.getString(Constants.ACTION_GET_USER_UUID, "") ?: ""
                if (uuid.isNotEmpty()) {
                    showUI()
                    setActiveFragment(mapFragment, R.id.menu_map)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.removeLoginListener()
        bottomItemSelectListener = null
        nullItemSelectListener = null
    }

    override fun onStartHunt() {
        bottomNavigation?.setOnNavigationItemSelectedListener(nullItemSelectListener)

    }

    override fun onStopHunt() {
        bottomNavigation?.setOnNavigationItemSelectedListener(bottomItemSelectListener)
    }
}