package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StationsActivity : AppCompatActivity() {
    private lateinit var btnSta1: Button
    private lateinit var btnSta2: Button
    private lateinit var btnSta3: Button
    private lateinit var btnSta4: Button
    private lateinit var btnSta5: Button
    private lateinit var btnAlarma: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Menu Estaciones"
        // Relacionando los elementos con su objeto de la interfaz
        btnSta1 = findViewById(R.id.btnStat1)
        btnSta2 = findViewById(R.id.btnStat2)
        btnSta3 = findViewById(R.id.btnStat3)
        btnSta4 = findViewById(R.id.btnStat4)
        btnSta5 = findViewById(R.id.btnStat5)
        btnAlarma = findViewById(R.id.btnAlarma)
    }

    private fun addListeners(){
        // Agregando los listener
        btnSta1.setOnClickListener {
            val intActEst1 = Intent(this,StationGenActivity::class.java)
            intActEst1.putExtra("name_station", "estacion 1")
            startActivity(intActEst1)
        }
        btnSta2.setOnClickListener {
            val intActEst2 = Intent(this,StationGenActivity::class.java)
            intActEst2.putExtra("name_station", "estacion 2")
            startActivity(intActEst2)
        }
        btnSta3.setOnClickListener {
            val intActEst3 = Intent(this,StationGenActivity::class.java)
            intActEst3.putExtra("name_station", "estacion 3")
            startActivity(intActEst3)
        }
        btnSta4.setOnClickListener {
            val intActEst4 = Intent(this,StationGenActivity::class.java)
            intActEst4.putExtra("name_station", "estacion 4")
            startActivity(intActEst4)
        }
        btnSta5.setOnClickListener {
            val intActEst5 = Intent(this,StationGenActivity::class.java)
            intActEst5.putExtra("name_station", "estacion 5")
            startActivity(intActEst5)
        }
        btnAlarma.setOnClickListener {
            val intentAlarma = Intent(this, AlarmActivity::class.java)
            startActivity(intentAlarma)
        }
    }
}