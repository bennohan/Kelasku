package com.bennohan.kelasku.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.User
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.databinding.ActivityHomeBinding
import com.bennohan.kelasku.databinding.ItemFriendsBinding
import com.bennohan.kelasku.ui.detailFriends.DetailFriendsActivity
import com.bennohan.kelasku.ui.editPassword.EditPasswordActivity
import com.bennohan.kelasku.ui.favourite.FavouriteActivity
import com.bennohan.kelasku.ui.login.LoginActivity
import com.bennohan.kelasku.ui.profile.ProfileActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.adapter.ReactiveListAdapter
import com.crocodic.core.extension.createIntent
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.tos
import com.crocodic.core.helper.ImagePreviewHelper
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(R.layout.activity_home) {

    @Inject
    lateinit var session: Session

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private var listFriends = ArrayList<User?>()


    private val adapterUser by lazy {
        object : ReactiveListAdapter<ItemFriendsBinding, User>(R.layout.item_friends) {
            override fun onBindViewHolder(
                holder: ItemViewHolder<ItemFriendsBinding, User>, position: Int
            ) {

                val item = getItem(position)

                item?.let { itm ->
                    holder.binding.data = itm
                    holder.bind(itm)


                    holder.binding.btnViewProfile.setOnClickListener {
                        openActivity<DetailFriendsActivity> {
                            putExtra(Const.FRIENDS.ID, item.userId)
                        }
                    }
                }
                holder.binding.cardView.cardElevation = 0f
                holder.binding.cardView.setCardBackgroundColor(Color.TRANSPARENT)

                super.onBindViewHolder(holder, position)
            }


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerLayout = binding.drawerLayout
        navView = binding.navigationView
        binding.rvUser.adapter = adapterUser


        //Set Status Bar Text to Black
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)


        getUser()
        getListUser()
        observe()
        search()
        askNotificationPermission()

    }

    private fun initView() {

        sideMenu()
        binding.user = session.getUser()

        //Swipe Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            getUser()
            observe()
            getListUser()
        }

        binding.ivProfile.setOnClickListener {
            activityLauncher.launch(createIntent<ProfileActivity>()) {
                if (it.resultCode == Const.RELOAD) {
                    getUser()
                    observe()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?.")
            .setPositiveButton("Exit") { _, _ ->
                this@HomeActivity.finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()

        // Set the color of the positive button text
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        dialog.show()
    }


    private fun getUser() {
        viewModel.getUser()
    }

    private fun getListUser() {
        viewModel.getListUser()
    }

    @Suppress("DEPRECATION")
    private fun sideMenu() {

        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.nav_header, navView, false)
        val tvUsername = headerView.findViewById<TextView>(R.id.tvUsername)
        val tvPhone = headerView.findViewById<TextView>(R.id.tvPhone)
        val ivProfile = headerView.findViewById<ImageView>(R.id.photo)
        val btnForward = headerView.findViewById<ImageButton>(R.id.iconForward)

        if (headerView == null) {
            // inflate a new header view and add it to the navigation view
            val headersView = inflater.inflate(R.layout.nav_header, navView, false)
            navView.addHeaderView(headersView)

        }else{
            navView.removeHeaderView(navView.getHeaderView(0))
            navView.addHeaderView(headerView)
        }

//        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
//        val favourite = navView.findViewById<NavigationView>(R.id.nav_favourite)
//        favourite.itemIconTintList = colorStateList

        val user = session.getUser()
        tvUsername.text = user?.nama
        tvPhone.text = user?.nomorTelepon

        Glide.with(this)
            .load(user?.foto)
            .apply(RequestOptions.circleCropTransform())
            .into(ivProfile)


        ivProfile.setOnClickListener {
            ImagePreviewHelper(this).show(ivProfile, binding.user?.foto)
        }


        btnForward.setOnClickListener {
            activityLauncher.launch(createIntent<ProfileActivity>()) {
                if (it.resultCode == Const.RELOAD) {
                    getUser()
                    observe()
                }
            }
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //Do nothing
            }

            override fun onDrawerOpened(drawerView: View) {
                // Set the status bar color to the drawer open color when the drawer is opened
                window.statusBarColor =
                    ContextCompat.getColor(this@HomeActivity, R.color.my_hint_color)
                window.decorView.systemUiVisibility = 0
            }

            override fun onDrawerClosed(drawerView: View) {
                window.statusBarColor =
                    ContextCompat.getColor(this@HomeActivity, R.color.main_background_color)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            }

            override fun onDrawerStateChanged(newState: Int) {
                // Do nothing
            }


        })
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_editPassword -> {
                    // Handle Home item selection
                    drawerLayout.closeDrawer(GravityCompat.START)
                    // Implement your logic for Home item here
                    openActivity<EditPasswordActivity>()
                    true
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    logout()
                    true
                }
                R.id.nav_favourite -> {
                    // Handle Settings item selection
                    drawerLayout.closeDrawer(GravityCompat.START)
                    // Implement your logic for Settings item here
                    openActivity<FavouriteActivity>()
                    true
                }
                else -> false
            }
        }

        binding.btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    // Handle ActionBarDrawerToggle click event
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                initView()

                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)
                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
                launch {
                    viewModel.listUser.collect { listUser ->
                        binding.refreshLayout.isRefreshing = false
                        listFriends.clear()
                        listFriends.addAll(listUser)
                        adapterUser.submitList(listUser)

                    }
                }
            }
        }

    }

    private fun search() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val filter = listFriends.filter { it?.nama?.contains("$text", true) == true || it?.namaSekolah?.contains("$text", true) == true || it?.nomorTelepon?.contains("$text", true) == true}
                adapterUser.submitList(filter)

                if (filter.isEmpty()) {
                    binding.tvDataKosong.visibility = View.VISIBLE
                } else {
                    binding.tvDataKosong.visibility = View.GONE
                }
            } else {
                adapterUser.submitList(listFriends)
            }
        }
    }

    private fun logout() {

        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle("Log Out")
        builder.setMessage("Anda yakin ingin keluar dari aplikasi ini? Jika keluar semua data anda akan dihapus dari perangkat ini.")
            .setCancelable(false)
            .setPositiveButton("Logout") { _, _ ->
                // Delete selected note from database
                tos("logout")
                viewModel.logout()
                openActivity<LoginActivity>()
                finishAffinity()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()

        // Set the color of the positive button text
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.crocodic.core.R.color.text_red))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // Show the dialog
        dialog.show()

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            tos("Permission Granted")
        } else {
            tos("Permission Denied")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
                //Do noting
            } else
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    //Do nothing
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


}