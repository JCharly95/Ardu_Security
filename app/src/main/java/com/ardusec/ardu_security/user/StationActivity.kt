package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StationActivity : AppCompatActivity() {
    // Seccion de la camara
    private lateinit var linLayCam: LinearLayout
    private lateinit var camara: WebView
    // Seccion del sensor de gases
    private lateinit var linLaySenGas: LinearLayout
    private lateinit var senGasLplbl: TextView
    private lateinit var senGasLp: TextView
    private lateinit var senCo2lbl: TextView
    private lateinit var senCo2: TextView
    private lateinit var senProplbl: TextView
    private lateinit var senProp: TextView
    private lateinit var senHumolbl: TextView
    private lateinit var senHumo: TextView
    // Seccion de la grafica
    private lateinit var layGraf: LineChart
    // Seccion del sensor magnetico
    private lateinit var linLaySenMagne: LinearLayout
    private lateinit var rbgMagne: RadioGroup
    private lateinit var rbMagneOp: RadioButton
    private lateinit var rbMagneCl: RadioButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var estacion: String
    private lateinit var camURL: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_station)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))

        //Obteniendo los valores del usuario y estacion
        if(intent.extras != null){
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            estacion = bundle.getString("key_station").toString()
        }else{
            Toast.makeText(this@StationActivity, "Error: no se pudo obtener la informacion del sistema", Toast.LENGTH_SHORT).show()
        }

        // Configurar el arranque de la interfaz
        setUp()
    }

    private fun setUp() {
        // Titulo de la ventana
        title = estacion
        // Seccion de la camara
        linLayCam = findViewById(R.id.LinLayCam)
        camara = findViewById(R.id.vidViewCam)
        // Seccion del sensor de gases
        linLaySenGas = findViewById(R.id.LinLaySenGas)
        senGasLplbl = findViewById(R.id.lblGas)
        senGasLp = findViewById(R.id.lblGasVal)
        senCo2lbl = findViewById(R.id.lblCo)
        senCo2 = findViewById(R.id.lblCoVal)
        senProplbl = findViewById(R.id.lblPro)
        senProp = findViewById(R.id.lblProVal)
        senHumolbl = findViewById(R.id.lblHum)
        senHumo = findViewById(R.id.lblHumVal)
        // Seccion de Prueba de la Grafica
        layGraf = findViewById(R.id.linLayGrafica)
        // Seccion del sensor magnetico
        linLaySenMagne = findViewById(R.id.LinLaySenMagne)
        rbgMagne = findViewById(R.id.rbgCondMagne)
        rbMagneOp = findViewById(R.id.rbMagneAbi)
        rbMagneCl = findViewById(R.id.rbMagneCerr)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Obtener la direccion IP de la estacion y lanzar la camara segun la solicitada
        when(estacion){
            "estacion1" -> {
                camURL = "http://192.168.8.13:5010"
                establecerCamara(camURL)
            }
            "estacion2" -> {
                camURL = "http://192.168.8.14:5000"
                establecerCamara(camURL)
            }
            "estacion3" -> {
                camURL = "http://192.168.8.12:5020"
                establecerCamara(camURL)
            }
            "estacion4" -> {
                camURL = "http://192.168.1.79:5000"
                establecerCamara(camURL)
            }
            "estacion5" -> {
                camURL = "http://192.168.1.79:5000"
                establecerCamara(camURL)
            }
        }

        // Establecer el sensor segun la estacion
        setSensor()
    }

    private fun avisoStation(){
        val mensaje = "Error: No se pudieron obtener los valores solicitados"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setSensor(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setSen = async {
                // Creando la referencia de la coleccion de sensores en la BD
                ref = database.getReference("Sensores")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSen in dataSnapshot.children) {
                            val estaBus = objSen.child("estacion_Rel").value.toString()
                            val tipo = objSen.child("tipo").value.toString()

                            if(estaBus == estacion && tipo == "Humo/Gas"){
                                linLaySenGas.isGone = false
                                establecerSensorGas()
                                layGraf.isGone = false
                                esteblecerGrafica()
                            }
                            if(estaBus == estacion && tipo == "Magnetico"){
                                linLaySenMagne.isGone = false
                                establecerSensorMagne()
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoStation()
                    }
                })
            }
            setSen.await()
        }
    }

    private fun establecerCamara(dirUrl: String){
        val videoURL = dirUrl + "/video_feed"
        //val videoURL = "http://192.168.100.66:5000/video_feed"
        //camara.settings.javaScriptEnabled = true
        camara.settings.loadWithOverviewMode = true
        camara.settings.useWideViewPort = true
        camara.loadUrl(videoURL)
    }

    private fun establecerSensorGas(){
        lifecycleScope.launch(Dispatchers.IO) {
            val getGasVal = async {
                // Creando la referencia de la coleccion de sensores en la BD
                ref = database.getReference("Sensores")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSen in dataSnapshot.children) {
                            val estaBus = objSen.child("estacion_Rel").value.toString()
                            val tipo = objSen.child("tipo").value.toString()
                            if(estaBus == estacion && tipo == "Humo/Gas"){
                                val refVals = objSen.child("registros").child("08-09-2023 17:15 CST").ref
                                refVals.addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(valsSen in snapshot.children) {
                                            when(valsSen.key.toString()) {
                                                "co" -> {
                                                    val resCo = "%.4f".format(valsSen.value.toString().toFloat()) + " ppm"
                                                    senCo2.text = resCo
                                                }
                                                "lpg" -> {
                                                    val resGLP = "%.4f".format(valsSen.value.toString().toFloat()) + " ppm"
                                                    senGasLp.text = resGLP
                                                }
                                                "propane" -> {
                                                    val resProp = "%.4f".format(valsSen.value.toString().toFloat()) + " ppm"
                                                    senProp.text = resProp
                                                }
                                                "smoke" -> {
                                                    val resHumo = "%.4f".format(valsSen.value.toString().toFloat()) + " ppm"
                                                    senHumo.text = resHumo
                                                }
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        avisoStation()
                                    }
                                })
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoStation()
                    }
                })
            }
            getGasVal.await()
        }
    }

    private fun establecerSensorMagne(){
        lifecycleScope.launch(Dispatchers.IO) {
            val getMagneSta = async {
                // Creando la referencia de la coleccion de sensores en la BD
                ref = database.getReference("Sensores")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSen in dataSnapshot.children) {
                            val estaBus = objSen.child("estacion_Rel").value.toString()
                            val tipo = objSen.child("tipo").value.toString()
                            if(estaBus == estacion && tipo == "Magnetico") {
                                val estado = objSen.child("estado").value.toString().toBoolean()
                                if(estado)
                                    rbMagneOp.isChecked = true
                                else
                                    rbMagneCl.isChecked = true
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoStation()
                    }
                })
            }
            getMagneSta.await()
        }
    }

    private fun esteblecerGrafica(){
        // Creando el arraylist de los valores para la grafica
        val arrVals = ArrayList<Entry>()
        // Agregandole valores
        arrVals.add(Entry(5f, 0.0F))
        arrVals.add(Entry(10f, 3.0F))
        arrVals.add(Entry(15f, 2.0F))
        arrVals.add(Entry(20f, 1.0F))
        arrVals.add(Entry(25f, 8.0F))
        arrVals.add(Entry(30f, 10.0F))
        arrVals.add(Entry(35f, 1.0F))
        arrVals.add(Entry(40f, 2.0F))
        arrVals.add(Entry(45f, 5.0F))
        arrVals.add(Entry(50f, 1.0F))
        arrVals.add(Entry(55f, 20.0F))
        arrVals.add(Entry(60f, 40.0F))
        arrVals.add(Entry(65f, 50.0F))

        val linedataset = LineDataSet(arrVals, "Primera muestra")
        //We add features to our chart
        linedataset.color = ContextCompat.getColor(this@StationActivity,R.color.rojo)
        linedataset.setDrawFilled(true)
        linedataset.circleRadius = 10f
        linedataset.valueTextSize = 15F
        linedataset.fillColor = ContextCompat.getColor(this@StationActivity,R.color.verdeLima)
        linedataset.mode = LineDataSet.Mode.LINEAR

        val datos = LineData(linedataset)
        layGraf.data = datos
        layGraf.description.text = "Prueba de Grafica"
        layGraf.description.textSize = 20f
        layGraf.setBackgroundColor(ContextCompat.getColor(this@StationActivity,R.color.blanco))
    }
}