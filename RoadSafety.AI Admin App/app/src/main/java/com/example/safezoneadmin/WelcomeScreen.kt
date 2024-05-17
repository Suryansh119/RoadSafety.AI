package com.example.safezoneadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WelcomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)
        findViewById<Button>(R.id.Register).setOnClickListener {
            startActivity(Intent(this,Register::class.java))
            finish()
        }
        findViewById<Button>(R.id.login2).setOnClickListener {
            startActivity(Intent(this,Login::class.java))
            finish()
        }
    }
    override fun onStart(){
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this,AdminScreen::class.java))
            finish()
        }
    }
}