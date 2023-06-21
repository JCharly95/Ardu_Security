package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class StationActivity : AppCompatActivity() {
    private lateinit var encaSta: TextView
    private lateinit var dataPack: Bundle
    private lateinit var staHead: String
    private lateinit var encaAct: String
    private lateinit var btnCamGen: Button
    private lateinit var btnSenGasGen: Button
    private lateinit var btnSenHumGen: Button
    private lateinit var btnSenMagneGen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.teal_700)))
        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Obteniendo el encabezado textual de la activity
        encaSta = findViewById(R.id.lblStaHead)
        // Obteniendo la estacion seleccionada por el boton
        dataPack = intent.extras!!
        staHead = dataPack.getString("name_station").toString()
        // Estableciendo el encabezado de la estacion segun la seleccion hecha
        encaAct = encaSta.text as String + " "+ staHead

        // Obteniendo los botones de la actividad
        btnCamGen= findViewById(R.id.btnCam)
        btnSenGasGen= findViewById(R.id.btnSenGas)
        btnSenHumGen= findViewById(R.id.btnSenHum)
        btnSenMagneGen= findViewById(R.id.btnSenMagne)
    }

    private fun addListeners(){
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