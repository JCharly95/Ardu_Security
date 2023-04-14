package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)
        // Obteniendo los botones
        val sta1: Button = findViewById(R.id.btnStat1)
        val sta2: Button = findViewById(R.id.btnStat2)
        val sta3: Button = findViewById(R.id.btnStat3)
        val sta4: Button = findViewById(R.id.btnStat4)
        val sta5: Button = findViewById(R.id.btnStat5)
        // Agregando los listener
        sta1.setOnClickListener {
            val intActEst1 = Intent(applicationContext,StationGenActivity::class.java)
            intActEst1.putExtra("name_station", "estacion 1")
            startActivity(intActEst1)
        }
        sta2.setOnClickListener {
            val intActEst2 = Intent(applicationContext,StationGenActivity::class.java)
            intActEst2.putExtra("name_station", "estacion 2")
            startActivity(intActEst2)
        }
        sta3.setOnClickListener {
            val intActEst3 = Intent(applicationContext,StationGenActivity::class.java)
            intActEst3.putExtra("name_station", "estacion 3")
            startActivity(intActEst3)
        }
        sta4.setOnClickListener {
            val intActEst4 = Intent(applicationContext,StationGenActivity::class.java)
            intActEst4.putExtra("name_station", "estacion 4")
            startActivity(intActEst4)
        }
        sta5.setOnClickListener {
            val intActEst5 = Intent(applicationContext,StationGenActivity::class.java)
            intActEst5.putExtra("name_station", "estacion 5")
            startActivity(intActEst5)
        }
    }
}