package com.bennohan.kelasku.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import com.bennohan.kelasku.R
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.databinding.ActivityMainBinding
import com.bennohan.kelasku.ui.home.HomeActivity
import com.bennohan.kelasku.ui.login.LoginActivity
import com.crocodic.core.base.activity.NoViewModelActivity
import com.crocodic.core.extension.openActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : NoViewModelActivity<ActivityMainBinding>(R.layout.activity_main) {

    @Inject
    lateinit var session: Session
    private val splashDelay: Long = 3500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            window.insetsController?.setSystemBarsAppearance(
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
//        } else {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
        window.statusBarColor = ContextCompat.getColor(this,R.color.main_background_color)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = session.getUser()
            if (user == null) {
                binding.constraint.visibility = View.VISIBLE
                binding.btnConfirm.setOnClickListener {
                    openActivity<LoginActivity> {
                        finish()
                    }
                }
            } else {
                openActivity<HomeActivity> {
                    finish()
                }
            }
        }, splashDelay)

    }
}