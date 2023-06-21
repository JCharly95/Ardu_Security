package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class CameraGenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_gen)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.teal_700)))

        // Obteniendo el encabezado textual de la activity
        val encaCam: TextView = findViewById(R.id.lblCamera)

        // Obteniendo la estacion seleccionada por el boton
        val dataPack: Bundle? = intent.extras
        val staHead: String? = dataPack?.getString("name_station")
        // Estableciendo el encabezado de la estacion segun la seleccion hecha
        var encaAct: String = encaCam.text as String + " "
        encaCam.text = encaAct + staHead
    }
}