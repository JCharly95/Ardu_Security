package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
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

class SensorGenActivity : AppCompatActivity() {
    private lateinit var encaSen: TextView
    private lateinit var bundle: Bundle
    private lateinit var staHead: String
    private lateinit var staFire: String
    private lateinit var nombre: String
    private lateinit var tipo: String
    // Escalas de medicion de gases
    private lateinit var senGasLplbl: TextView
    private lateinit var senGasLp: TextView
    private lateinit var senCo2lbl: TextView
    private lateinit var senCo2: TextView
    private lateinit var senProplbl: TextView
    private lateinit var senProp: TextView
    private lateinit var senHumolbl: TextView
    private lateinit var senHumo: TextView
    // Elementos del magnetico
    private lateinit var rbgMagne: RadioGroup
    private lateinit var rbMagneOp: RadioButton
    private lateinit var rbMagneCl: RadioButton
    // Objeto gson
    var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_gen)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Obteniendo la estacion seleccionada por el boton
        bundle = intent.extras!!
        staHead = bundle.getString("name_station").toString().split(";")[0]
        staFire = bundle.getString("name_station").toString().split(";")[1]
        nombre = bundle.getString("nom_sensor").toString()
        tipo = bundle.getString("tipo").toString()

        // Titulo de la pantalla
        title = nombre
        // Encabezado de la ventana
        encaSen = findViewById(R.id.lblSensor)
        // Valor establecido a mostrar (nombre/tipo sensor)
        encaSen.text = encaSen.text.toString()+" $tipo:\n"+staHead
        // Elementos de la ventana
        senGasLplbl = findViewById(R.id.lblGas)
        senGasLp = findViewById(R.id.lblGasVal)
        senCo2lbl = findViewById(R.id.lblCo)
        senCo2 = findViewById(R.id.lblCoVal)
        senProplbl = findViewById(R.id.lblPro)
        senProp = findViewById(R.id.lblProVal)
        senHumolbl = findViewById(R.id.lblHum)
        senHumo = findViewById(R.id.lblHumVal)
        rbgMagne = findViewById(R.id.rbgCondMagne)
        rbMagneOp = findViewById(R.id.rbMagneAbi)
        rbMagneCl = findViewById(R.id.rbMagneCerr)

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
        // Determinar que elementos estaran visibles acorde a la relacion con la estacion
        setElems()
    }

    private fun addListeners(){

    }

    private fun setElems(){
        // Preparando el boton acorde al sensor relacionado con la estacion
        lifecycleScope.launch(Dispatchers.IO) {
            val setElemsSen = async {
                val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                refDB.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSens in dataSnapshot.children){
                            val sensorJSON = gson.toJson(objSens.value)
                            if(sensorJSON.contains("Magnetico")){
                                rbgMagne.isGone = false
                                getState()
                            }else{
                                senGasLplbl.isGone = false
                                senGasLp.isGone = false
                                senCo2lbl.isGone = false
                                senCo2.isGone = false
                                senProplbl.isGone = false
                                senProp.isGone = false
                                senHumolbl.isGone = false
                                senHumo.isGone = false
                                getValues()
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            setElemsSen.await()
        }
    }

    private fun getValues(){
        lifecycleScope.launch(Dispatchers.IO) {
            val getGasVal = async {
                data class SensorHG(val id_Sensor: String, val nom_Sen: String, val tipo: String, val co: Double, val lpg: Double, val propane: Double, val smoke: Double, val estacion_Nom: String)
                val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                refDB.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSens in dataSnapshot.children){
                            val sensorJSON = gson.toJson(objSens.value)
                            val resSenHG = gson.fromJson(sensorJSON, SensorHG::class.java)
                            if(resSenHG.nom_Sen == nombre){
                                senGasLp.text = "%.4f".format(resSenHG.lpg) + " ppm"
                                senCo2.text = "%.4f".format(resSenHG.co) + " ppm"
                                senProp.text = "%.4f".format(resSenHG.propane) + " ppm"
                                senHumo.text = "%.4f".format(resSenHG.smoke) + " ppm"
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            getGasVal.await()
        }
    }

    private fun getState(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setSenMagne = async {
                data class SensorMagne(val id_Sensor: String, val nom_Sen: String, val tipo: String, val estado: Boolean, val estacion_Nom: String)
                val refDB = Firebase.database.getReference("Sensor").orderByChild("estacion_Nom").equalTo(staFire)
                refDB.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSens in dataSnapshot.children){
                            val sensorJSON = gson.toJson(objSens.value)
                            val resSenMagne = gson.fromJson(sensorJSON, SensorMagne::class.java)
                            if(!resSenMagne.estado){
                                rbMagneCl.isChecked = true
                            }else{
                                rbMagneOp.isChecked = true
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            setSenMagne.await()
        }
    }
}