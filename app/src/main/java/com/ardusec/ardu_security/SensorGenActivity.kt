package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SensorGenActivity : AppCompatActivity() {
    private lateinit var encaSen: TextView
    private lateinit var bundle: Bundle
    private lateinit var estacion: String
    private lateinit var nombre: String
    private lateinit var tipo: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_gen)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Obteniendo la estacion seleccionada por el boton
        bundle = intent.extras!!
        estacion = bundle.getString("name_station").toString()
        nombre = bundle.getString("nom_sensor").toString()
        tipo = bundle.getString("tipo").toString()

        // Titulo de la pantalla
        title = nombre
        encaSen = findViewById(R.id.lblSensor)
        encaSen.text = encaSen.text.toString()+" $tipo:\n"+estacion.split(";")[0]
    }

    private fun addListeners(){

    }
}