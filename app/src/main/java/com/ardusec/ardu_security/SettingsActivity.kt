package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {
    private lateinit var btnSelTem: Button
    private lateinit var btnEditInfo: Button
    private lateinit var btnGesNoti: Button
    private lateinit var btnComent: Button
    private lateinit var btnAcerca: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Configuracion"
        // Relacionando los elementos con su objeto de la interfaz
        btnSelTem = findViewById(R.id.btnTema)
        btnEditInfo = findViewById(R.id.btnActuInfo)
        btnGesNoti = findViewById(R.id.btnGesNoti)
        btnComent = findViewById(R.id.btnCommSis)
        btnAcerca = findViewById(R.id.btnAbout)
    }

    private fun acerca(){
        val mensaje = "- Version: \n" +
                "* 1.0 \n\n" +
                "- Fecha de Lanzamiento: \n" +
                "* 25/05/2023 \n\n" +
                "- Equipo Desarrollador: \n" +
                "* Manuel Deniry Santana Ayala\n" +
                "* Juan Carlos Hernandez Lopez\n\n"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Acerca de...")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        // Agregar los listener
        btnSelTem.setOnClickListener {

        }
        btnEditInfo.setOnClickListener {
            val intentPerf = Intent(this, UserActivity::class.java)
            startActivity(intentPerf)
        }
        btnGesNoti.setOnClickListener {

        }
        btnComent.setOnClickListener {

        }
        btnAcerca.setOnClickListener {
            acerca()
        }
    }
}