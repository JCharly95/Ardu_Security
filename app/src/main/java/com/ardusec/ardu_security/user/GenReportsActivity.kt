package com.ardusec.ardu_security.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone


class GenReportsActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var rbSelEst1: RadioButton
    private lateinit var rbSelEst2: RadioButton
    private lateinit var rbSelEst3: RadioButton
    private lateinit var rbSelEst4: RadioButton
    private lateinit var rbSelEst5: RadioButton
    private lateinit var rbSenCo: RadioButton
    private lateinit var rbSenGLP: RadioButton
    private lateinit var rbSenProp: RadioButton
    private lateinit var rbSenHumo: RadioButton
    private lateinit var lblFechaIni: TextView
    private lateinit var btnSelFechaIni: Button
    private lateinit var lblHoraIni: TextView
    private lateinit var btnSelHoraIni: Button
    private lateinit var lblFechaFin: TextView
    private lateinit var btnSelFechaFin: Button
    private lateinit var lblHoraFin: TextView
    private lateinit var btnSelHoraFin: Button
    private lateinit var btnGenGraf: Button
    private lateinit var layGraf: LineChart
    private lateinit var wbRepoPrev: View
    private lateinit var btnGenRepo: Button
    // Arraylists de valores para los datasets de las graficas
    private val arrCo = ArrayList<Entry>()
    private val arrGLP = ArrayList<Entry>()
    private val arrProp = ArrayList<Entry>()
    private val arrHumo = ArrayList<Entry>()
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Cantidad de botones a mostrar y arreglo de keys de las estaciones del sistema
    private var cantEsta: Long = 0
    private val arrEstasKey = ArrayList<String>()
    // Variables de valor de seleccion
    private var estaSel: String = ""
    private var sensoSel: String = ""
    private var valFechaIni: String = ""
    private var valHoraIni: String = ""
    private var valFechaFin: String = ""
    private var valHoraFin: String = ""
    // Estableciendo el calendario de uso en la zona horaria de la CDMX
    private val calendar = Calendar.getInstance(TimeZone.getTimeZone("CST"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_gen_reports)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))

        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@GenReportsActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("usuario").toString()
            sistema = bundle.getString("sistema").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Reportes"
        // Relacionando los elementos con su objeto de la interfaz
        btnAyuda = findViewById(R.id.btnHelpGenRepo)
        rbSelEst1 = findViewById(R.id.rbSelEsta1)
        rbSelEst2 = findViewById(R.id.rbSelEsta2)
        rbSelEst3 = findViewById(R.id.rbSelEsta3)
        rbSelEst4 = findViewById(R.id.rbSelEsta4)
        rbSelEst5 = findViewById(R.id.rbSelEsta5)
        rbSenCo = findViewById(R.id.rbSelSenCO)
        rbSenGLP = findViewById(R.id.rbSelSenGLP)
        rbSenProp = findViewById(R.id.rbSelSenProp)
        rbSenHumo = findViewById(R.id.rbSelSenHumo)
        lblFechaIni = findViewById(R.id.lblFechIni)
        btnSelFechaIni = findViewById(R.id.btnSelFechIni)
        lblHoraIni = findViewById(R.id.lblHoraIni)
        btnSelHoraIni = findViewById(R.id.btnSelHoraIni)
        lblFechaFin = findViewById(R.id.lblFechFin)
        btnSelFechaFin = findViewById(R.id.btnSelFechFin)
        lblHoraFin = findViewById(R.id.lblHoraFin)
        btnSelHoraFin = findViewById(R.id.btnSelHoraFin)
        btnGenGraf = findViewById(R.id.btnGenGrafRepo)
        layGraf = findViewById(R.id.areaGrafRepo)
        wbRepoPrev = findViewById(R.id.webViewRepoPrev)
        btnGenRepo = findViewById(R.id.btnConfGenRepo)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database
        // Estableciendo los radioButtons a mostrar y los nombres de estos
        setUpBtnEsta()
    }

    private fun addListeners() {
        btnAyuda.setOnClickListener {
            val msg = "¿Como generar un reporte? \n\n" +
                "Estación:\n" +
                "* Primero se deberá seleccionar la estacion de la cual se desea generar un reporte.\n\n" +
                "Sensor:\n" +
                "* Posteriormente, se deberá seleccionar el sensor del que se desea obtener la información.\n\n" +
                "Fecha y Hora de Inicio:\n" +
                "* A continuación, se deberán seleccionar los valores de inicio para el rango de monitoreo en el reporte.\n\n" +
                "Fecha y Hora de Fin:\n" +
                "* Asi como, seleccionar los valores de finalización para el rango de monitoreo en el reporte.\n\n" +
                "Generar Grafica:\n" +
                "* Cuando hayan sido ingresados los datos, este boton generará la grafica que será agregada al reporte."
            avisoGenRepo(msg)
        }
        rbSelEst1.setOnClickListener {
            if(rbSelEst1.isChecked)
                estaSel = rbSelEst1.text.toString()
        }
        rbSelEst2.setOnClickListener {
            if(rbSelEst2.isChecked)
                estaSel = rbSelEst2.text.toString()
        }
        rbSelEst3.setOnClickListener {
            if(rbSelEst3.isChecked)
                estaSel = rbSelEst3.text.toString()
        }
        rbSelEst4.setOnClickListener {
            if(rbSelEst4.isChecked)
                estaSel = rbSelEst4.text.toString()
        }
        rbSelEst5.setOnClickListener {
            if(rbSelEst5.isChecked)
                estaSel = rbSelEst5.text.toString()
        }
        rbSenCo.setOnClickListener {
            if(rbSenCo.isChecked)
                sensoSel = "CO"
        }
        rbSenGLP.setOnClickListener {
            if(rbSenGLP.isChecked)
                sensoSel = "GLP"
        }
        rbSenProp.setOnClickListener {
            if(rbSenProp.isChecked)
                sensoSel = "Prop"
        }
        rbSenHumo.setOnClickListener {
            if(rbSenHumo.isChecked)
                sensoSel = "Humo"
        }
        btnSelFechaIni.setOnClickListener {
            val anio = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH)
            val dia = calendar.get(Calendar.DAY_OF_MONTH)

            val fechaDialog = DatePickerDialog(
                this@GenReportsActivity,
                {   _, year, month, day ->
                    val month = month + 1
                    val monCon = if(month < 10){
                        "0$month"
                    }else{
                        month.toString()
                    }
                    val dayCon = if(day < 10){
                        "0$day"
                    }else{
                        day.toString()
                    }
                    valFechaIni = "$dayCon-$monCon-$year"
                    lblFechaIni.text = valFechaIni
                },
                anio,
                mes,
                dia
            )
            fechaDialog.show()
        }
        btnSelHoraIni.setOnClickListener {
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)

            val horaDialog = TimePickerDialog(
                this@GenReportsActivity,
                {   _, hour, minute ->
                    val hourCon = if(hour < 10){
                        "0$hour"
                    }else{
                        hour.toString()
                    }
                    val minCon = if(minute < 10){
                        "0$minute"
                    }else{
                        minute.toString()
                    }
                    valHoraIni = "$hourCon:$minCon"
                    lblHoraIni.text = valHoraIni
                },
                hora,
                minuto,
                false
            )
            horaDialog.show()
        }
        btnSelFechaFin.setOnClickListener {
            val anio = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH)
            val dia = calendar.get(Calendar.DAY_OF_MONTH)

            val fechaDialog = DatePickerDialog(
                this@GenReportsActivity,
                {   _, year, month, day ->
                    val month = month + 1
                    val monCon = if(month < 10){
                        "0$month"
                    }else{
                        month.toString()
                    }
                    val dayCon = if(day < 10){
                        "0$day"
                    }else{
                        day.toString()
                    }
                    valFechaFin = "$dayCon-$monCon-$year"
                    lblFechaFin.text = valFechaFin
                },
                anio,
                mes,
                dia
            )
            fechaDialog.show()
        }
        btnSelHoraFin.setOnClickListener {
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)

            val horaDialog = TimePickerDialog(
                this@GenReportsActivity,
                {   _, hour, minute ->
                    val hourCon = if(hour < 10){
                        "0$hour"
                    }else{
                        hour.toString()
                    }
                    val minCon = if(minute < 10){
                        "0$minute"
                    }else{
                        minute.toString()
                    }
                    valHoraFin = "$hourCon:$minCon"
                    lblHoraFin.text = valHoraFin
                },
                hora,
                minuto,
                false
            )
            horaDialog.show()
        }
        btnGenGraf.setOnClickListener {
            // Para generar la grafica, se debieron haber llenado todos los valores previos, si no, se lanzará el aviso correspondiente
            if((estaSel != "") && (sensoSel != "") && (valFechaIni != "") && (valHoraIni != "") && (valFechaFin != "") && (valHoraFin != "")){
                genGraficaRepo()
            }else{
                if((estaSel == "") && (sensoSel == "") && (valFechaIni == "") && (valHoraIni == "") && (valFechaFin == "") && (valHoraFin == "")){
                    avisoGenRepo("Favor de seleccionar TODOS los valores solicitados")
                }else{
                    if(valHoraFin == ""){ avisoGenRepo("Favor de seleccionar una hora final") }
                    if(valFechaFin == ""){ avisoGenRepo("Favor de seleccionar un día final") }
                    if(valHoraIni == ""){ avisoGenRepo("Favor de seleccionar una hora inicial") }
                    if(valFechaIni == ""){ avisoGenRepo("Favor de seleccionar un día inicial") }
                    if(sensoSel == ""){ avisoGenRepo("Favor de seleccionar un sensor") }
                    if(estaSel == ""){ avisoGenRepo("Favor de seleccionar una estación") }
                }
            }
        }
        btnGenRepo.setOnClickListener {

        }
    }

    private fun setUpBtnEsta(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setBtnsEstas = async {
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSis in dataSnapshot.children) {
                            if(objSis.key.toString() == sistema){
                                val refSnap = objSis.child("estaciones")
                                cantEsta = refSnap.childrenCount
                                when(cantEsta.toInt()){
                                    3 -> {
                                        rbSelEst4.isGone = true
                                        rbSelEst5.isGone = true
                                    }
                                    5 -> {
                                        rbSelEst4.isGone = false
                                        rbSelEst5.isGone = false
                                    }
                                }
                                // Obteniendo las ID de las estaciones
                                val refEsta = refSnap.ref
                                refEsta.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                        for(objEsta in snapshot1.children){
                                            arrEstasKey.add(objEsta.key.toString())
                                        }
                                        for((estaIndex, estaVal) in arrEstasKey.withIndex()){
                                            val ref2 = database.getReference("Estaciones")
                                            ref2.addListenerForSingleValueEvent(object: ValueEventListener{
                                                override fun onDataChange(snapshot2: DataSnapshot) {
                                                    for(objEsta2 in snapshot2.children){
                                                        when(estaIndex){
                                                            0 -> {
                                                                if(estaVal == objEsta2.key.toString())
                                                                    rbSelEst1.text = objEsta2.child("nombre").value.toString()
                                                            }
                                                            1 -> {
                                                                if(estaVal == objEsta2.key.toString())
                                                                    rbSelEst2.text = objEsta2.child("nombre").value.toString()
                                                            }
                                                            2 -> {
                                                                if(estaVal == objEsta2.key.toString()){
                                                                    rbSelEst3.text = objEsta2.child("nombre").value.toString()
                                                                }
                                                            }
                                                            3 -> {
                                                                if(estaVal == objEsta2.key.toString())
                                                                    rbSelEst4.text = objEsta2.child("nombre").value.toString()
                                                            }
                                                            4 -> {
                                                                if(estaVal == objEsta2.key.toString())
                                                                    rbSelEst5.text = objEsta2.child("nombre").value.toString()
                                                            }
                                                        }
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(this@GenReportsActivity,"Error: Consulta incompleta",Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@GenReportsActivity,"Error: Consulta erronea",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@GenReportsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setBtnsEstas.await()
        }
    }

    private fun setGrafica(){
        // Establecer los valores de la grafica
        layGraf.setNoDataText("Favor de seleccionar el sensor que desee ver")
        layGraf.setDrawBorders(true)
        layGraf.setBorderColor(R.color.negro)
        layGraf.description.text = "Sensor de Gases: $estaSel (PPM)"
        layGraf.description.textSize = 15f
        layGraf.setBackgroundColor(ContextCompat.getColor(this@GenReportsActivity,R.color.blanco))
    }

    private fun avisoGenRepo(mensaje: String) {
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun evalFecha(fechCmp: String, datosFechIni: String, datosFechFin: String): Boolean{
        // Formato de fecha usado para comparar las fechas textuales
        val fechFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        // Transformacion de texto a fecha de las fechas de los registros y los datePicker
        val dateIni = fechFormat.parse(datosFechIni); val dateFin = fechFormat.parse(datosFechFin); val dateCmp = fechFormat.parse(fechCmp)
        // Comparaciones de la fecha obtenida, con la fecha de inicio y la fecha de fin
        val cmpIni = dateCmp?.compareTo(dateIni); val cmpFin = dateCmp?.compareTo(dateFin)

        return (cmpIni!!>=0) && (cmpFin!!<=0)
    }

    private fun genGraficaRepo(){
        lifecycleScope.launch(Dispatchers.IO){
            val getEstaKey = async {
                // Paso 1: Buscar la key de la estacion seleccionada en estaciones
                ref = database.getReference("Estaciones")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objEsta in dataSnapshot.children){
                            if(objEsta.child("nombre").value.toString() == estaSel){
                                // Si el nombre de la estacion seleccionada coincide con el nombre de la estacion, se obtiene la Key
                                val estaKey = objEsta.key.toString()
                                // Paso 2: Buscar el sensor de la estacion seleccionada
                                val ref2 = database.getReference("Sensores")
                                ref2.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                        for(objSen in snapshot1.children){
                                            if(objSen.child("estacion_Rel").value.toString() == estaKey){
                                                // Obtener el tipo del sensor de la estacion para generar grafica solo si es sensor de gases
                                                val tipo = objSen.child("tipo").value.toString()
                                                when(tipo){
                                                    "Humo/Gas" -> {
                                                        // Establecer las caracteristicas de la grafica y el contador de las posiciones X de los datos
                                                        setGrafica()
                                                        var contePosX = 0.0F
                                                        val refRegi = objSen.child("registros").ref
                                                        refRegi.addListenerForSingleValueEvent(object: ValueEventListener{
                                                            override fun onDataChange(snapshot2: DataSnapshot) {
                                                                val fechIni = "$valFechaIni $valHoraIni"
                                                                val fechFin = "$valFechaFin $valHoraFin"
                                                                when(sensoSel){
                                                                    "CO" -> {
                                                                        arrCo.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                                        for(objRegi in snapshot2.children){
                                                                            // Para cada registro, se evaluará si este fue obtenido dentro del rango de fechas establecidos
                                                                            val fechObte = objRegi.key.toString().split(" ")[0]+ " " + objRegi.key.toString().split(" ")[1]
                                                                            if(evalFecha(fechObte, fechIni, fechFin)){
                                                                                arrCo.add(Entry(contePosX, objRegi.child("co").value.toString().toFloat()))
                                                                                contePosX += 3
                                                                            }
                                                                        }
                                                                        if(arrCo.isEmpty()){
                                                                            avisoGenRepo("Error: No se encontraron registros dentro del rango solicitado, favor de revisar sus fechas y horas")
                                                                        }else{
                                                                            val lineCoDataSet = LineDataSet(arrCo, "CO")
                                                                            lineCoDataSet.color = ContextCompat.getColor(this@GenReportsActivity,R.color.darkgray)
                                                                            lineCoDataSet.setDrawFilled(true)
                                                                            lineCoDataSet.circleRadius = 5f
                                                                            lineCoDataSet.fillColor = ContextCompat.getColor(this@GenReportsActivity,R.color.darkgray)
                                                                            lineCoDataSet.mode = LineDataSet.Mode.LINEAR
                                                                            layGraf.data = LineData(lineCoDataSet)
                                                                        }
                                                                    }
                                                                    "GLP" -> {
                                                                        arrGLP.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                                        for(objRegi in snapshot2.children){
                                                                            // Para cada registro, se evaluará si este fue obtenido dentro del rango de fechas establecidos
                                                                            val fechObte = objRegi.key.toString().split(" ")[0]+ " " + objRegi.key.toString().split(" ")[1]
                                                                            if(evalFecha(fechObte, fechIni, fechFin)){
                                                                                arrGLP.add(Entry(contePosX, objRegi.child("lpg").value.toString().toFloat()))
                                                                                contePosX += 3
                                                                            }
                                                                        }
                                                                        if(arrGLP.isEmpty()){
                                                                            avisoGenRepo("Error: No se encontraron registros dentro del rango solicitado, favor de revisar sus fechas y horas")
                                                                        }else{
                                                                            val lineGLPDataSet = LineDataSet(arrGLP, "Gas LP")
                                                                            lineGLPDataSet.color = ContextCompat.getColor(this@GenReportsActivity,R.color.teal_200)
                                                                            lineGLPDataSet.setDrawFilled(true)
                                                                            lineGLPDataSet.circleRadius = 5f
                                                                            lineGLPDataSet.fillColor = ContextCompat.getColor(this@GenReportsActivity,R.color.teal_200)
                                                                            lineGLPDataSet.mode = LineDataSet.Mode.LINEAR
                                                                            layGraf.data = LineData(lineGLPDataSet)
                                                                        }
                                                                    }
                                                                    "Prop" -> {
                                                                        arrProp.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                                        for(objRegi in snapshot2.children){
                                                                            // Para cada registro, se evaluará si este fue obtenido dentro del rango de fechas establecidos
                                                                            val fechObte = objRegi.key.toString().split(" ")[0]+ " " + objRegi.key.toString().split(" ")[1]
                                                                            if(evalFecha(fechObte, fechIni, fechFin)){
                                                                                arrProp.add(Entry(contePosX, objRegi.child("propane").value.toString().toFloat()))
                                                                                contePosX += 3
                                                                            }
                                                                        }
                                                                        if(arrProp.isEmpty()){
                                                                            avisoGenRepo("Error: No se encontraron registros dentro del rango solicitado, favor de revisar sus fechas y horas")
                                                                        }else{
                                                                            val linePropDataSet = LineDataSet(arrProp, "Propano")
                                                                            linePropDataSet.color = ContextCompat.getColor(this@GenReportsActivity,R.color.naranja)
                                                                            linePropDataSet.setDrawFilled(true)
                                                                            linePropDataSet.circleRadius = 5f
                                                                            linePropDataSet.fillColor = ContextCompat.getColor(this@GenReportsActivity,R.color.naranja)
                                                                            linePropDataSet.mode = LineDataSet.Mode.LINEAR
                                                                            layGraf.data = LineData(linePropDataSet)
                                                                        }
                                                                    }
                                                                    "Humo" -> {
                                                                        arrHumo.clear()// Es necesario limpiar el arreglo cada vez que se solicite cambiar de informacion para no se generen errores en los cambios
                                                                        for(objRegi in snapshot2.children){
                                                                            // Para cada registro, se evaluará si este fue obtenido dentro del rango de fechas establecidos
                                                                            val fechObte = objRegi.key.toString().split(" ")[0]+ " " + objRegi.key.toString().split(" ")[1]
                                                                            if(evalFecha(fechObte, fechIni, fechFin)){
                                                                                arrHumo.add(Entry(contePosX, objRegi.child("smoke").value.toString().toFloat()))
                                                                                contePosX += 3
                                                                            }
                                                                        }
                                                                        if(arrHumo.isEmpty()){
                                                                            avisoGenRepo("Error: No se encontraron registros dentro del rango solicitado, favor de revisar sus fechas y horas")
                                                                        }else{
                                                                            val lineSmokeDataSet = LineDataSet(arrHumo, "Humo")
                                                                            lineSmokeDataSet.color = ContextCompat.getColor(this@GenReportsActivity,R.color.negro)
                                                                            lineSmokeDataSet.setDrawFilled(true)
                                                                            lineSmokeDataSet.circleRadius = 5f
                                                                            lineSmokeDataSet.fillColor = ContextCompat.getColor(this@GenReportsActivity,R.color.gris)
                                                                            lineSmokeDataSet.mode = LineDataSet.Mode.LINEAR
                                                                            layGraf.data = LineData(lineSmokeDataSet)
                                                                        }
                                                                    }
                                                                }
                                                                layGraf.invalidate() // Este metodo sirve para refrescar la informacion de la grafica, ese sera ejecutado en cada seleccion de informacion
                                                            }
                                                            override fun onCancelled(error: DatabaseError) {
                                                                lifecycleScope.launch(Dispatchers.Main){
                                                                    avisoGenRepo("Error: No se pudieron obtener los valores solicitados")
                                                                }
                                                            }
                                                        })
                                                    }
                                                    "Magnetico" -> {
                                                        avisoGenRepo("Error: No es posible generar una grafica para los sensores magneticos")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@GenReportsActivity,"Error: Consulta incompleta",Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@GenReportsActivity,"Error: Datos no obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getEstaKey.await()
        }
    }

    private fun getPlantilla(){
        // Instanciando la plantilla
        val inflater = LayoutInflater.from(this@GenReportsActivity)
        val view = inflater.inflate(R.layout.plantilla_reporte, null)
        val tablaRegistros = view.findViewById<TableLayout>(R.id.tblLayRegistros)

        val nFila = TableRow(this@GenReportsActivity)
        nFila.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT)
        nFila.layout(dpToPx(3),dpToPx(3),dpToPx(3),dpToPx(3))

        val nRegistro = TextView(this@GenReportsActivity)
        nRegistro.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        nRegistro.setText("Prueba de Agregado")
        nRegistro.gravity = Gravity.CENTER
        nRegistro.setTextColor(ContextCompat.getColor(this,R.color.negro))
        nRegistro.textSize = spToPx(15).toFloat()
    }

    // Funciones de Conversiones
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this@GenReportsActivity.resources.displayMetrics ).toInt()
    }

    private fun spToPx(sp: Int): Int {
        return TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), this@GenReportsActivity.resources.displayMetrics ).toInt()
    }
}