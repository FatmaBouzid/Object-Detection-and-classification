package com.example.myapplicationlast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var btnregister: Button
    private lateinit var btnlogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnregister = findViewById(R.id.btn)
        btnlogin = findViewById(R.id.btn2)
        // Gestion du clic sur le bouton "Register"
        btnregister.setOnClickListener {
            val intent = Intent(this, REGISTER::class.java)
            startActivity(intent)

        }
        // Gestion du clic sur le bouton "Login"
        btnlogin.setOnClickListener {
            val intent = Intent(this, LOGIN::class.java)
            startActivity(intent)

        }
    }
}