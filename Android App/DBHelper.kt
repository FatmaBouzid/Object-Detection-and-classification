package com.example.myapplicationlast

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

class DBHelper(context: Context):SQLiteOpenHelper (context,"Userdata",null,1) {
    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("create table Userdata (Username TEXT primary key,password TEXT)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("drop table if exists Userdata")

    }
    fun insertdata(Username:String,Password:String): Boolean{
        val p0 =this.writableDatabase
        val cv = ContentValues()
        cv.put("username",Username)
        cv.put("Password",Password)
        val result = p0.insert("Userdata",null,cv)
        if (result==-1 .toLong()){
            return false
        }
        return true
    }
    fun checkuserpass(Username: String,Password: String): Boolean{
        val p0=this.writableDatabase
        val query="select * from Userdata where username='$Username' and password='$Password'"
        val cursor = p0.rawQuery(query,null)
        if (cursor.count<=0){
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }
}