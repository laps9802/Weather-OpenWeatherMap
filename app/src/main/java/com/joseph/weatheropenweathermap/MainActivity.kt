package com.joseph.weatheropenweathermap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPreviousLogin()
    }

    private fun checkPreviousLogin() {      //로그인여부 분기
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) showLoginWindow()
        else moveToOpenWeatherActivity()
    }

    private fun moveToOpenWeatherActivity() {
        startActivity(Intent(this, OpenWeatherActivity::class.java))
    }

    private fun showLoginWindow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.icons8_sun_500)
                .setTheme(R.style.GreenTheme)
                .build(),
            RC_SIGN_IN
        )

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                moveToOpenWeatherActivity()
            } else {
                Toast.makeText(this, "로그인 실패, 로그인을 다시 시도해주세요", Toast.LENGTH_LONG).show()
            }
        }

    }


}

