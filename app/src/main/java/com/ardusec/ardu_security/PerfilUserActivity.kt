package com.ardusec.ardu_security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PerfilUserActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_user)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Crear un bundle para los extras
        bundle = intent.extras!!
        // Saber si el usuario vera el boton de gestion o no
        val corrAcc = bundle.getString("correo")
    }

    private fun setUp(){

    }

    private fun addListeners(){

    }
}