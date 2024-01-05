package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
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
    // Seccion del sensor magnetico
    private lateinit var linLaySenMagne: LinearLayout
    private lateinit var rbgMagne: RadioGroup
    private lateinit var rbMagneOp: RadioButton
    private lateinit var rbMagneCl: RadioButton
    // Seccion de la grafica
    private lateinit var linLayBtnGraf: LinearLayout
    private lateinit var btnSenCO: Button
    private lateinit var btnSenGLP: Button
    private lateinit var btnSenProp: Button
    private lateinit var btnSenHumo: Button
    private lateinit var linLayGraf: LinearLayout
    private lateinit var layGraf: LineChart
    // Arraylists de valores para los datasets de las graficas
    private val arrCo = ArrayList<Entry>()
    private val arrGLP = ArrayList<Entry>()
    private val arrProp = ArrayList<Entry>()
    private val arrHumo = ArrayList<Entry>()
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var estaKey: String
    private var nomEsta = ""
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
            estaKey = bundle.getString("key_station").toString()
        }else{
            Toast.makeText(this@StationActivity, "Error: no se pudo obtener la informacion del sistema", Toast.LENGTH_SHORT).show()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Relacionando los elementos con su objeto de la interfaz
        nomEsta = ""
        // Seccion de la camara
        linLayCam = findViewById(R.id.linLayCam)
        camara = findViewById(R.id.vidViewCam)
        // Seccion del sensor magnetico
        linLaySenMagne = findViewById(R.id.linLaySenMagne)
        rbgMagne = findViewById(R.id.rbgCondMagne)
        rbMagneOp = findViewById(R.id.rbMagneAbi)
        rbMagneCl = findViewById(R.id.rbMagneCerr)
        // Seccion de la Grafica
        linLayBtnGraf = findViewById(R.id.linLayBtnGrafica)
        btnSenCO = findViewById(R.id.btnViewCOGraf)
        btnSenGLP = findViewById(R.id.btnViewGLPGraf)
        btnSenProp = findViewById(R.id.btnViewPropGraf)
        btnSenHumo = findViewById(R.id.btnViewHumGraf)
        linLayGraf = findViewById(R.id.linLayGrafica)
        layGraf = findViewById(R.id.areaGraf)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Establecer la camara segun la estacion
        setEstacion()
        // Establecer el sensor segun la estacion
        setSensor()
    }

    private fun addListeners(){
        btnSenCO.setOnClickListener {
            genGrafSenso("CO")
        }
        btnSenGLP.setOnClickListener {
            genGrafSenso("GLP")
        }
        btnSenProp.setOnClickListener {
            genGrafSenso("Prop")
        }
        btnSenHumo.setOnClickListener {
            genGrafSenso("Humo")
        }
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

    private fun setEstacion(){
        // Variables de trabajo para la camara
        var dirIP: String
        // Obteniendo los valores desde Firebase
        lifecycleScope.launch(Dispatchers.IO){
            val getVals = async {
                ref = database.getReference("Estaciones")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objEsta in dataSnapshot.children){
                            if(objEsta.key.toString() == estaKey){
                                // Titulo de la ventana
                                nomEsta = objEsta.child("nombre").value.toString()
                                title = nomEsta
                                // Direccion IP para la camara
                                dirIP = objEsta.child("dir_IP").value.toString()
                                // Establecer la camara
                                setCamara(dirIP)
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@StationActivity, "Error: no se pudo obtener la informacion de la estacion", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getVals.await()
        }
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
                            // Determinando que sensor se encuentra en la estacion
                            if(estaBus == estaKey && tipo == "Humo/Gas"){
                                linLayBtnGraf.isGone = false
                                linLayGraf.isGone = false
                                setGrafica()
                            }
                            if(estaBus == estaKey && tipo == "Magnetico"){
                                linLaySenMagne.isGone = false
                                establecerSensorMagne()
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        lifecycleScope.launch(Dispatchers.Main){
                            avisoStation()
                        }
                    }
                })
            }
            setSen.await()
        }
    }

    private fun setGrafica(){
        // Establecer los valores de la grafica
        layGraf.setNoDataText("Favor de seleccionar el sensor que desee ver")
        layGraf.setDrawBorders(true)
        layGraf.setBorderColor(R.color.negro)
        layGraf.description.text = "Sensor de Gases: $nomEsta (PPM)"
        layGraf.description.textSize = 15f
        layGraf.setBackgroundColor(ContextCompat.getColor(this@StationActivity,R.color.blanco))
    }

    private fun setCamara(dirIP: String){
        var puerto: String
        var videoURL: String
        lifecycleScope.launch(Dispatchers.IO){
            val setCam = async {
                // Obtencion del puerto de la camara
                ref = database.getReference("Camaras")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objCam in dataSnapshot.children){
                            if(objCam.child("estacion_Rel").value.toString() == estaKey){
                                puerto = objCam.child("puerto").value.toString()
                                videoURL = "http://$dirIP:$puerto/video_feed"

                                //val videoURL = "http://192.168.100.66:5000/video_feed"
                                //camara.settings.javaScriptEnabled = true
                                camara.settings.loadWithOverviewMode = true
                                camara.settings.useWideViewPort = true
                                camara.loadUrl(videoURL)
                                Toast.makeText(this@StationActivity, videoURL, Toast.LENGTH_LONG).show()
                                Log.w("DireccionURLCamara", videoURL)
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        lifecycleScope.launch(Dispatchers.Main){
                            avisoStation()
                        }
                    }
                })
            }
            setCam.await()
        }
    }

    private fun genGrafSenso(sensor: String){
        var contePosX = 0.0F
        lifecycleScope.launch(Dispatchers.IO){
            val getGasVal = async {
                // Creando la referencia de la coleccion de sensores en la BD
                ref = database.getReference("Sensores")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSen in dataSnapshot.children) {
                            val estaBus = objSen.child("estacion_Rel").value.toString()
                            val tipo = objSen.child("tipo").value.toString()
                            if(estaBus == estaKey && tipo == "Humo/Gas"){
                                val refRegi = objSen.child("registros").ref
                                refRegi.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        when(sensor){
                                            "CO" -> {
                                                arrCo.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                for(objRegi in snapshot.children){
                                                    arrCo.add(Entry(contePosX, objRegi.child("co").value.toString().toFloat()))
                                                    contePosX += 3
                                                }
                                                val lineCoDataSet = LineDataSet(arrCo, "CO")
                                                lineCoDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.darkgray)
                                                lineCoDataSet.setDrawFilled(true)
                                                lineCoDataSet.circleRadius = 5f
                                                lineCoDataSet.fillColor = ContextCompat.getColor(this@StationActivity,R.color.darkgray)
                                                lineCoDataSet.mode = LineDataSet.Mode.LINEAR
                                                layGraf.data = LineData(lineCoDataSet)
                                            }
                                            "GLP" -> {
                                                arrGLP.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                for(objRegi in snapshot.children){
                                                    arrGLP.add(Entry(contePosX, objRegi.child("lpg").value.toString().toFloat()))
                                                    contePosX += 3
                                                }
                                                val lineGLPDataSet = LineDataSet(arrGLP, "Gas LP")
                                                lineGLPDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.teal_200)
                                                lineGLPDataSet.setDrawFilled(true)
                                                lineGLPDataSet.circleRadius = 5f
                                                lineGLPDataSet.fillColor = ContextCompat.getColor(this@StationActivity,R.color.teal_200)
                                                lineGLPDataSet.mode = LineDataSet.Mode.LINEAR
                                                layGraf.data = LineData(lineGLPDataSet)
                                            }
                                            "Prop" -> {
                                                arrProp.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                for(objRegi in snapshot.children){
                                                    arrProp.add(Entry(contePosX, objRegi.child("propane").value.toString().toFloat()))
                                                    contePosX += 3
                                                }
                                                val linePropDataSet = LineDataSet(arrProp, "Propano")
                                                linePropDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.naranja)
                                                linePropDataSet.setDrawFilled(true)
                                                linePropDataSet.circleRadius = 5f
                                                linePropDataSet.fillColor = ContextCompat.getColor(this@StationActivity,R.color.naranja)
                                                linePropDataSet.mode = LineDataSet.Mode.LINEAR
                                                layGraf.data = LineData(linePropDataSet)
                                            }
                                            "Humo" -> {
                                                arrHumo.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                for(objRegi in snapshot.children){
                                                    arrHumo.add(Entry(contePosX, objRegi.child("smoke").value.toString().toFloat()))
                                                    contePosX += 3
                                                }
                                                val lineSmokeDataSet = LineDataSet(arrHumo, "Humo")
                                                lineSmokeDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.negro)
                                                lineSmokeDataSet.setDrawFilled(true)
                                                lineSmokeDataSet.circleRadius = 5f
                                                lineSmokeDataSet.fillColor = ContextCompat.getColor(this@StationActivity,R.color.gris)
                                                lineSmokeDataSet.mode = LineDataSet.Mode.LINEAR
                                                layGraf.data = LineData(lineSmokeDataSet)
                                            }
                                        }
                                        layGraf.invalidate() // Este metodo sirve para refrescar la informacion de la grafica, ese sera ejecutado en cada seleccion de informacion
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            avisoStation()
                                        }
                                    }
                                })
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        lifecycleScope.launch(Dispatchers.Main){
                            avisoStation()
                        }
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
                            if(estaBus == estaKey && tipo == "Magnetico") {
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
                        lifecycleScope.launch(Dispatchers.Main){
                            avisoStation()
                        }
                    }
                })
            }
            getMagneSta.await()
        }
    }

    /*private fun esteblecerGrafica(){
        // Rellenar los arraylist con los valores de firebase
        establecerDataSets()

        Toast.makeText(this@StationActivity, arrCo.toString(), Toast.LENGTH_LONG).show()

        val lineCoDataSet = LineDataSet(arrCo, "Sensor de CO")
        lineCoDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.rojo)
        lineCoDataSet.setDrawFilled(true)
        lineCoDataSet.circleRadius = 10f
        lineCoDataSet.valueTextSize = 15F
        lineCoDataSet.mode = LineDataSet.Mode.LINEAR
        val lineGLPDataSet = LineDataSet(arrGLP, "Sensor de Gas LP")
        lineGLPDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.amarillo)
        lineGLPDataSet.setDrawFilled(true)
        lineGLPDataSet.circleRadius = 10f
        lineGLPDataSet.valueTextSize = 15F
        lineGLPDataSet.mode = LineDataSet.Mode.LINEAR
        val linePropDataSet = LineDataSet(arrPropane, "Sensor de Propano")
        linePropDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.darkgray)
        linePropDataSet.setDrawFilled(true)
        linePropDataSet.circleRadius = 10f
        linePropDataSet.valueTextSize = 15F
        linePropDataSet.mode = LineDataSet.Mode.LINEAR
        val lineSmokeDataSet = LineDataSet(arrSmoke, "Sensor de Humo")
        lineSmokeDataSet.color = ContextCompat.getColor(this@StationActivity,R.color.negro)
        lineSmokeDataSet.setDrawFilled(true)
        lineSmokeDataSet.circleRadius = 10f
        lineSmokeDataSet.valueTextSize = 15F
        lineSmokeDataSet.mode = LineDataSet.Mode.LINEAR
        // Creando el linedata de la grafica (es como un dataset de lineas)
        val datos = LineData()
        datos.addDataSet(lineCoDataSet)
        datos.addDataSet(lineGLPDataSet)
        datos.addDataSet(linePropDataSet)
        datos.addDataSet(lineSmokeDataSet)
        layGraf.data = datos
        layGraf.description.text = "Sensor de Gases: $nomEsta"
        layGraf.description.textSize = 20f
        layGraf.setBackgroundColor(ContextCompat.getColor(this@StationActivity,R.color.blanco))
    }*/
}