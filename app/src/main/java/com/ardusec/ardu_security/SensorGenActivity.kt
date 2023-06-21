package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SensorGenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_gen)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.teal_700)))

        // Obteniendo el encabezado textual de la activity
        val encaSen: TextView = findViewById(R.id.lblSensor)

        // Obteniendo la estacion y el tipo de sensor seleccionados por el boton
        val dataPack: Bundle? = intent.extras
        val senHead: String? = dataPack?.getString("name_station")
        val tipSen: String? = dataPack?.getString("tip_sensor")
        // Estableciendo el encabezado de la estacion segun la seleccion hecha
        var encaAct: String = encaSen.text as String + " "
        var encaConca1: String = encaAct + tipSen + " de la "
        encaSen.text = encaConca1 + senHead
    }
}