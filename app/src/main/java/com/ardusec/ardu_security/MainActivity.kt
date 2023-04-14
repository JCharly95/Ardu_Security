package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = applicationContext
        Toast.makeText(context, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()

        Timer().schedule(2000){
            val intentLogin = Intent(context, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }
}