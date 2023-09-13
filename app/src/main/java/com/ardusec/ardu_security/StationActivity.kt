package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StationActivity : AppCompatActivity() {
    private lateinit var encaSta: TextView
    private lateinit var bundle: Bundle
    private lateinit var staHead: String
    private lateinit var staFire: String
    private lateinit var btnCam: Button
    private lateinit var btnSenGH: Button
    private lateinit var btnSenMagne: Button
    // Objeto gson
    var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Obteniendo el encabezado textual de la activity
        encaSta = findViewById(R.id.lblStaHead)
        // Obteniendo la estacion seleccionada por el boton
        bundle = intent.extras!!
        staHead = bundle.getString("name_station").toString()
        // Estableciendo el encabezado de la estacion segun la seleccion hecha
        encaSta.text = "Accesos de la \n"+ staHead
        // Obteniendo los botones de la actividad
        btnCam = findViewById(R.id.btnCam)
        btnSenGH = findViewById(R.id.btnSenGH)
        btnSenMagne = findViewById(R.id.btnSenMagne)
        title = staHead

        // Determinar el nombre de la estacion a buscar en firebase
        staFire = when (staHead){
            "Estacion 1" -> {
                "EstGen1"
            }
            "Estacion 2" -> {
                "EstGen2"
            }
            "Estacion 3" -> {
                "EstGen3"
            }
            "Estacion 4" -> {
                "EstGen4"
            }
            else -> {
                "EstGen5"
            }
        }
        // Determinar que botones estaran visibles acorde a la relacion con la estacion
        btnCam.isGone = false
        setBtnSen()
    }

    private fun setBtnSen(){
        // Preparando el boton acorde al sensor relacionado con la estacion
        lifecycleScope.launch(Dispatchers.IO) {
            val setBtnEsta = async {
                val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                refDB.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSens in dataSnapshot.children){
                            val sensorJSON = gson.toJson(objSens.value)
                            if(sensorJSON.contains("Magnetico")){
                                btnSenMagne.isGone = false
                            }else{
                                btnSenGH.isGone = false
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            setBtnEsta.await()
        }
    }

    private fun addListeners(){
        // Agregando los listener
        btnCam.setOnClickListener {
            // Preparando el boton de la camara
            lifecycleScope.launch(Dispatchers.IO) {
                val setBtnEsta = async {
                    data class Camara(val id_Camara: String, val nom_Cam: String, val ip_Transmi: String, val estacion_Nom: String)
                    val refDB = Firebase.database.getReference("Camara").orderByChild("estacion_Nom").equalTo(staFire)
                    refDB.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot){
                            for (objCam in dataSnapshot.children){
                                val camJSON = gson.toJson(objCam.value)
                                val resCam = gson.fromJson(camJSON, Camara::class.java)
                                Intent(this@StationActivity, CameraGenActivity::class.java).apply {
                                    this.putExtra("name_station", staHead +";"+staFire)
                                    this.putExtra("nom_cam", resCam.nom_Cam)
                                    startActivity(this)
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                        }
                    })
                }
                setBtnEsta.await()
            }
        }
        btnSenGH.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val setBtnEsta = async {
                    data class SensorHG(val id_Sensor: String, val nom_Sen: String, val tipo: String, val co: Double, val lpg: Double, val propane: Double, val smoke: Double, val estacion_Nom: String)
                    val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                    refDB.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot){
                            for (objSens in dataSnapshot.children){
                                val sensorJSON = gson.toJson(objSens.value)
                                val resSenHG = gson.fromJson(sensorJSON, SensorHG::class.java)
                                Intent(this@StationActivity, SensorGenActivity::class.java).apply {
                                    this.putExtra("name_station", staHead +";"+staFire)
                                    this.putExtra("nom_sensor", resSenHG.nom_Sen)
                                    this.putExtra("tipo", resSenHG.tipo)
                                    startActivity(this)
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                        }
                    })
                }
                setBtnEsta.await()
            }
        }
        btnSenMagne.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val setBtnEsta = async {
                    data class SensorMagne(val id_Sensor: String, val nom_Sen: String, val tipo: String, val estado: Boolean, val estacion_Nom: String)
                    val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                    refDB.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot){
                            for (objSens in dataSnapshot.children){
                                val sensorJSON = gson.toJson(objSens.value)
                                val resSenMagne = gson.fromJson(sensorJSON, SensorMagne::class.java)
                                Intent(this@StationActivity, SensorGenActivity::class.java).apply {
                                    this.putExtra("name_station", staHead +";"+staFire)
                                    this.putExtra("nom_sensor", resSenMagne.nom_Sen)
                                    this.putExtra("tipo", resSenMagne.tipo)
                                    startActivity(this)
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                        }
                    })
                }
                setBtnEsta.await()
            }
        }
    }
}