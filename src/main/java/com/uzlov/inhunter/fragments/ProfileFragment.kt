package com.uzlov.inhunter.fragments

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.textview.MaterialTextView
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.auth.IAuthListener
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import com.uzlov.inhunter.data.net.sources.usecases.PlayerUseCases
import com.uzlov.inhunter.databinding.ProfileLayoutBinding
import com.uzlov.inhunter.interfaces.Constants
import com.uzlov.inhunter.ui.custom.SimpleExpandableRecyclerView
import com.uzlov.inhunter.utils.defaultNickname
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import javax.inject.Inject


class ProfileFragment : Fragment(), Toolbar.OnMenuItemClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var pref: PreferenceRepository

    @Inject
    lateinit var playerUseCases: PlayerUseCases

    private val mapTypes: MutableMap<String, Boolean> = mutableMapOf()
    private var _viewBinding: ProfileLayoutBinding? = null
    private val viewBinding get() = _viewBinding!!

    private val adapterTypes = AdapterTypes()
    private val profile by lazy {
        pref.readOwner()
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    interface OnActionListener {
        fun select(type: String)
    }

    private var listener: OnActionListener = object : OnActionListener {
        override fun select(type: String) {
            pref.setActiveType(type)
            mapTypes.clear()
            mapTypes.putAll(pref.getAllTypes())
            adapterTypes.notifyDataSetChanged()

            updateOwner()
        }
    }

    fun loginSuccess(account: GoogleSignInAccount) {
        playerUseCases.getSelfProfile()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val localProfile = pref.readOwner().apply {
                    nick = it.nick
                    type = it.type
                    email = it.email
                }
                pref.updateOwner(localProfile)
                loadOwnerFromLocal(account)
            }, {
                loadOwnerFromLocal(account)
                it.printStackTrace()
            }, {
                loadOwnerFromLocal(account)
            })

    }

    private fun loadOwnerFromLocal(account: GoogleSignInAccount) {
        with(viewBinding) {
            val owner = pref.readOwner()

            if (owner.nick.isNullOrEmpty()) {
                owner.nick = account.defaultNickname()
            }
            if (owner.email.isNullOrEmpty()){
                owner.email = account.email
            }

            profileName.text = account.displayName
            profileMail.text = account.email
            etProfileName.setText(owner.nick)

            Glide.with(requireContext())
                .load(account.photoUrl)
                .into(profileImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
        pref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _viewBinding = ProfileLayoutBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        authService.appAccount?.let {
            loginSuccess(it)
        }

        with(viewBinding) {


            rvTypesPlayer.adapter = adapterTypes
            mapTypes.putAll(pref.getAllTypes())
            adapterTypes.setTypes(mapTypes.keys)

            listViewMap.setItems(pref.readMapTypes())
            listViewMap.setTitleItem(pref.getActiveMapType())

            // запрос данных с remote source
            playerUseCases.getSelfProfile().subscribe({
                etProfileName.setText(it.nick)
            }, {
                it.printStackTrace()
                etProfileName.setText(profile.nick)
            }, {
                etProfileName.setText(profile.nick)
            })
        }
    }

    private fun updateOwner() {
        val owner = Owner(
            email = authService.appAccount?.email,
            nick = viewBinding.etProfileName.text.toString(),
            type = pref.getActiveType()
        )
        playerUseCases.updatePlayer(owner)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                pref.updateOwner(owner)
            }, {
                it.printStackTrace()
                Toast.makeText(requireContext(),
                    it.message ?: "Не удалось обновить данные",
                    Toast.LENGTH_SHORT).show()
            })
    }

    private fun initListeners() {
        with(viewBinding) {
            profileToolbar.setOnMenuItemClickListener(this@ProfileFragment)
            listViewMap.setStateListener(object : SimpleExpandableRecyclerView.StateListener {
                override fun expand() {
                    ObjectAnimator.ofInt(scrollViewRootS, "scrollY", 0, 700, 1500).apply {
                        startDelay = 50
                        interpolator = LinearInterpolator()
                    }.start()
                }

                override fun squeeze() {}

                override fun itemSelect(item: String) {
                    pref.setActiveMapType(item)
                    listViewMap.setTitleItem(item)
                }
            })

            etProfileName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(text: Editable?) {
                    if (text?.length ?: 0 < 5) {
                        etProfileName.error = "Ник должен быть более 5 символов"
                        return
                    } else {
                        etProfileName.error = null
                        updateOwner()
                    }
                }
            })

            btnDeleteAccount.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder
                    .setTitle(R.string.delete_account)
                    .setMessage("После удаления аккаунта все созданные Вами команды будут удалены, а также вы автоматически покинете команды в которых находитесь")
                    .setPositiveButton(R.string.delete_account
                    ) { dialog, _ ->
                        playerUseCases.removePlayer(authService.appAccount?.email ?: "")
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                authService.logoutUser()
                            }, {
                                Toast.makeText(requireContext(),
                                    "Не удалось удлаить акканут",
                                    Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            })
                    }
                    .setNegativeButton(R.string.cancel
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                // Create the AlertDialog object and return it
                builder.create()
                builder.show()

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> {
                authService.logoutUser()
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        pref.unregisterOnSharedPreferenceChangeListener(this)
        _viewBinding?.listViewMap?.onDestroy()
        _viewBinding = null
        super.onDestroyView()
    }

    private inner class AdapterTypes : RecyclerView.Adapter<TypeViewHolder>() {

        private var typesPlayer: MutableList<String> = mutableListOf()

        fun setTypes(items: Collection<String>) {
            typesPlayer.clear()
            typesPlayer.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.team_item_layout, parent, false)
            return TypeViewHolder(v)
        }

        override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
            if (mapTypes[typesPlayer[position]] == true) {
                holder.onBindActivated(typesPlayer[position])
            } else {
                holder.onBind(typesPlayer[position])
            }
            holder.onBindClickListener(typesPlayer[position])
        }

        override fun getItemCount() = typesPlayer.size
    }

    private inner class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var tvNameType = itemView.findViewById<MaterialTextView>(R.id.tvNameTeam)

        fun onBind(type: String) {
            tvNameType.text = type
            tvNameType.setTextColor(resources.getColor(R.color.black))
            tvNameType.setCompoundIconColor(resources.getColor(R.color.black))
            itemView.setBackgroundColor(resources.getColor(R.color.white))
        }

        fun onBindActivated(type: String) {
            tvNameType.text = type
            tvNameType.setTextColor(resources.getColor(R.color.white))
            itemView.setBackgroundColor(resources.getColor(R.color.main_background))
            tvNameType.setCompoundIconColor(resources.getColor(R.color.white))
        }

        fun onBindClickListener(type: String) {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.select(type)
                }
            }
        }
    }

    fun MaterialTextView.setCompoundIconColor(color: Int) {
        compoundDrawablesRelative.forEach { icon ->
            icon?.let {
                it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    override fun onSharedPreferenceChanged(sPref: SharedPreferences?, key: String?) {
        when (key) {
            "type_map_key" -> {
                viewBinding.listViewMap.setTitleItem(sPref?.getString(key, "")
                    ?: getString(R.string.no_selected_map))
            }
            "key_type_player"-> {
                mapTypes.clear()
                mapTypes.putAll(pref.getAllTypes())
                adapterTypes.setTypes(mapTypes.keys)
            }
        }
    }
}