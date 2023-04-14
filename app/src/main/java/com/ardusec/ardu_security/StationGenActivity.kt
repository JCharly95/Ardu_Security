package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class StationGenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_gen)

        // Obteniendo el encabezado textual de la activity
        val encaSta: TextView = findViewById(R.id.lblStaHead)
        // Obteniendo la estacion seleccionada por el boton
        val dataPack: Bundle? = intent.extras
        val staHead: String? = dataPack?.getString("name_station")
        // Estableciendo el encabezado de la estacion segun la seleccion hecha
        var encaAct: String = encaSta.text as String + " "
        encaSta.text = encaAct + staHead

        // Obteniendo los botones de la actividad
        val btnCamGen: Button = findViewById(R.id.btnCam)
        val btnSenGasGen: Button = findViewById(R.id.btnSenGas)
        val btnSenHumGen: Button = findViewById(R.id.btnSenHum)
        val btnSenMagneGen: Button = findViewById(R.id.btnSenMagne)
        // Agregando los listener
        btnCamGen.setOnClickListener {
            val intActCam = Intent(applicationContext,CameraGenActivity::class.java)
            intActCam.putExtra("name_station", staHead)
            startActivity(intActCam)
        }
        btnSenGasGen.setOnClickListener {
            val intActSenGas = Intent(applicationContext,SensorGenActivity::class.java)
            intActSenGas.putExtra("name_station", staHead)
            intActSenGas.putExtra("tip_sensor", "de gas")
            startActivity(intActSenGas)
        }
        btnSenHumGen.setOnClickListener {
            val intActSenHum = Intent(applicationContext,SensorGenActivity::class.java)
            intActSenHum.putExtra("name_station", staHead)
            intActSenHum.putExtra("tip_sensor", "de humo")
            startActivity(intActSenHum)
        }
        btnSenMagneGen.setOnClickListener {
            val intActSenMagne = Intent(applicationContext,SensorGenActivity::class.java)
            intActSenMagne.putExtra("name_station", staHead)
            intActSenMagne.putExtra("tip_sensor", "magnetico")
            startActivity(intActSenMagne)
        }
    }
}