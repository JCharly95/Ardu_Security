package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var btnSelTem: Button
    private lateinit var btnEditInfo: Button
    private lateinit var btnGesNoti: Button
    private lateinit var btnComent: Button
    private lateinit var btnAcerca: Button
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo el usuario
        if(intent.extras == null){
            Toast.makeText(this@SettingsActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
        }
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
        val aviso = AlertDialog.Builder(this@SettingsActivity)
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
            val intentPerf = Intent(this@SettingsActivity, UserActivity::class.java).apply {
                putExtra("username", user)
            }
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