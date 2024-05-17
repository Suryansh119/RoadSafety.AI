package com.example.safezoneadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.safezoneadmin.databinding.ActivityLoginBinding
import com.example.safezoneadmin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.moveText.setOnClickListener{
            startActivity(Intent(this,Register::class.java))
            finish()
        }
        binding.login2.setOnClickListener {
            var email=binding.textInputLayout4.editText?.text.toString()
            var password=binding.textInputLayout5.editText?.text.toString()

            if(email.isNullOrEmpty()){
                binding.textInputLayout4.error="Email field is blank"
            }else if (password.isNullOrEmpty()){
                binding.textInputLayout5.error="Password fielf is blank"
            }
            else{
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"Logging-in",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login,AdminScreen::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                this,
                                task.exception?.localizedMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }

}