package com.example.safezoneadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.safezoneadmin.databinding.ActivityAdminImageViewBinding
import com.example.safezoneadmin.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Register : AppCompatActivity() {
    val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.RegisterText.setOnClickListener{
            startActivity(Intent(this,Login::class.java))
            finish()
        }
        binding.button2.setOnClickListener {
            var email=binding.textInputLayout.editText?.text.toString()
            var password=binding.textInputLayout3.editText?.text.toString()

            if(email.isNullOrEmpty()){
                binding.textInputLayout.error="Email field is blank"
            }else if (password.isNullOrEmpty()){
                binding.textInputLayout3.error="Password fielf is blank"
            }
            else{
                Firebase.auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        Firebase.auth.signOut()
                        Toast.makeText(this,"User Created Successfully",Toast.LENGTH_SHORT).show()
                       startActivity(Intent(this,Login::class.java))
                        finish()
                    }
                    else{
                        Toast.makeText(this,it.exception?.localizedMessage.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }
}