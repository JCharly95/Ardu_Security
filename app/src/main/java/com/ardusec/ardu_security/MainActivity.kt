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
        // Arranque de la app
        setUp()
    }

    private fun setUp(){
        // Mensaje de bienvenida a la App
        Toast.makeText(this, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()

        // Retardo de 2 segundos para mostrar la ventana de login
        Timer().schedule(2000){
            val intentLogin = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }
}