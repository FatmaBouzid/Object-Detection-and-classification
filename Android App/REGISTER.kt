package com.example.myapplicationlast

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class REGISTER : AppCompatActivity() {
    private lateinit var uname:EditText
    private lateinit var pword:EditText
    private lateinit var cpword:EditText
    private lateinit var predbtn:ImageButton
    private lateinit var registerBtn:Button
    private lateinit var emailEditText:EditText
    private lateinit var db:DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        uname=findViewById(R.id.username)
        pword=findViewById(R.id.pass)
        cpword=findViewById(R.id.cpass)
        emailEditText=findViewById(R.id.em)
        predbtn=findViewById(R.id.precBtn)
        registerBtn=findViewById(R.id.registerBtn)
        db= DBHelper(this)

        fun isEmailValid(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        predbtn.setOnClickListener{
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        registerBtn.setOnClickListener{
            val unametext = uname.text.toString()
            val pwordtext = pword.text.toString()
            val cpwordtext = cpword.text.toString()
            val email = emailEditText.text.toString()
            val savedata = db.insertdata(unametext,pwordtext)

            if (TextUtils.isEmpty(unametext) || TextUtils.isEmpty(pwordtext) || TextUtils.isEmpty(cpwordtext) || TextUtils.isEmpty(email)){
                Toast.makeText(this,"Please fill in all fields.",Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid(email)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            } else {
                if (pwordtext == cpwordtext){
                    if (savedata){
                        Toast.makeText(this,"Register Successful",Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, LOGIN::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this,"User Exists",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}