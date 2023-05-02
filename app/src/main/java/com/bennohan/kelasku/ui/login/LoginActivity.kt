package com.bennohan.kelasku.ui.login

import android.content.ContentValues
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.kelasku.R
import com.bennohan.kelasku.base.BaseActivity
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.databinding.ActivityLoginBinding
import com.bennohan.kelasku.ui.home.HomeActivity
import com.bennohan.kelasku.ui.register.RegisterActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding,LoginViewModel>(R.layout.activity_login) {

    @Inject
    lateinit var session: Session


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        observe()
        tvRegister()

        binding.btnLogin.setOnClickListener {
            generateFcmToken {
                if (it){
                    login()
                }else{
                    binding.root.snacked("Tidak ada Device Token")
                }
            }
        }





    }

    private fun login(){
        val phone = binding.etPhone.textOf()
        val password = binding.etPassword.textOf()
        val deviceToken = session.getString(Const.TOKEN.DEVICE_TOKEN)

        if (binding.etPhone.isEmptyRequired(R.string.mustFill)|| binding.etPassword.isEmptyRequired(R.string.mustFill)){
            return
        }

        if (password.length >= 6) {
            // password meets minimum length requirement, continue with setting the password
            viewModel.login(phone,password,deviceToken)
        } else {
            // password does not meet minimum length requirement, show error message
            tos("password tidak sama")
            TODO()
        }

    }

    private fun tvRegister(){
        val spannableString = SpannableString("Don't Have an Account Register Now")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
               openActivity<RegisterActivity>()
            }
        }
        spannableString.setSpan(clickableSpan, 22, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvDontHaveAccount.text = spannableString
        binding.tvDontHaveAccount.movementMethod = LinkMovementMethod.getInstance() // Required for clickable spans to work

    }

    private fun observe(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        //when(it.message){}
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("login...in")
                            ApiStatus.SUCCESS -> {
                                loadingDialog.dismiss()
                                openActivity<HomeActivity>()
                                finish()
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }
                    }
                }
            }
        }
    }

    private fun generateFcmToken ( result:( Boolean )-> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                result(false)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            //todo: menerima hasil tugas fcmnya
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token) //todo:untuk menegecek aja
            Log.d(ContentValues.TAG, msg)
            session.setValue(Const.TOKEN.DEVICE_TOKEN, token)
            result(true)
        })
    }

}