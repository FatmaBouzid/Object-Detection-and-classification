package com.example.myapplicationlast

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LOGIN : AppCompatActivity() {
    private lateinit var loginbtn : Button
    private lateinit var edituser:EditText
    private lateinit var editpword:EditText
    private lateinit var dbh: DBHelper
    private lateinit var predbtn:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginbtn=findViewById(R.id.loginBtn)
        edituser=findViewById(R.id.user)
        editpword=findViewById(R.id.pas1)
        predbtn=findViewById(R.id.precBtnn)
        dbh = DBHelper(this)

        predbtn.setOnClickListener{
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        loginbtn.setOnClickListener(){
            val useredtx= edituser.text.toString()
            val passedtx= editpword.text.toString()

            if (TextUtils.isEmpty(useredtx) || TextUtils.isEmpty(passedtx)){
                Toast.makeText(this,"Add Username & Password",Toast.LENGTH_SHORT).show()
            }
            else{
                val checkuser = dbh.checkuserpass(useredtx,passedtx)
                if (checkuser==true){
                    Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                    val intent=Intent(applicationContext,success::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this,"Wrong Username & Password",Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
}