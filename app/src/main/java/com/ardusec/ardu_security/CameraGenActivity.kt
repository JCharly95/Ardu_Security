package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class CameraGenActivity : AppCompatActivity() {
    private lateinit var encaCam: TextView
    private lateinit var bundle: Bundle
    private lateinit var estacion: String
    private lateinit var nombre: String
    private lateinit var camara: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_gen)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Obteniendo la estacion seleccionada por el boton
        bundle = intent.extras!!
        estacion = bundle.getString("name_station").toString()
        nombre = bundle.getString("nom_cam").toString()
        camara = findViewById(R.id.vidViewCam)
        val videoURL = "localhost"

        // Titulo de la pantalla
        title = nombre
        encaCam = findViewById(R.id.lblCamera)
        encaCam.text = encaCam.text.toString()+":\n"+estacion.split(";")[0]

        Glide.with(this).load(Uri.parse(videoURL)).into(camara)
    }

    private fun addListeners(){

    }
}