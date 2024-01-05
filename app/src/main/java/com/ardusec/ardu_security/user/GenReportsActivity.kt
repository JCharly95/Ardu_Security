package com.ardusec.ardu_security.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import java.io.IOException
import java.util.Timer
import kotlin.concurrent.schedule

class GenReportsActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion; Seccion Formulario de Seleccion
    private lateinit var linLaySel: LinearLayout
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
    private lateinit var btnGenRepoPrev: Button
    // Estableciendo los elementos de interaccion; Seccion Formulario del Reporte
    private lateinit var linLayRepo: LinearLayout
    private lateinit var lblHeadRepo: TextView
    private lateinit var txtFechaIni: TextView
    private lateinit var txtHoraIni: TextView
    private lateinit var txtFechaFin: TextView
    private lateinit var txtHoraFin: TextView
    private lateinit var linLayRegs: LinearLayout
    private lateinit var layGraf: LineChart
    private lateinit var linlayBtnRepo: LinearLayout
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
    // Elementos del reporte
    private val codSoliRepo = 125
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
        // Relacionando los elementos con su objeto de la interfaz; Seccion Formulario de Seleccion
        linLaySel = findViewById(R.id.formSelDatosRepo)
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
        btnGenRepoPrev = findViewById(R.id.btnGenPrevRepo)
        // Relacionando los elementos con su objeto de la interfaz; Seccion Formulario del Reporte
        linLayRepo = findViewById(R.id.linLayReporte)
        lblHeadRepo = findViewById(R.id.lblHeadRepoPlanti)
        txtFechaIni = findViewById(R.id.txtFechaIniRepo)
        txtHoraIni = findViewById(R.id.txtHoraIniRepo)
        txtFechaFin = findViewById(R.id.txtFechaFinRepo)
        txtHoraFin = findViewById(R.id.txtHoraFinRepo)
        linLayRegs = findViewById(R.id.linLayRegistros)
        layGraf = findViewById(R.id.areaGrafRepo)
        linlayBtnRepo = findViewById(R.id.linLayBtnRepo)
        btnGenRepo = findViewById(R.id.btnConfGenRepo)

        // Mostrando solo el formulario de seleccion
        linLaySel.isGone = false
        linLayRepo.isGone = true
        linlayBtnRepo.isGone = true

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
                    txtFechaIni.text = valFechaIni
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
                    txtHoraIni.text = valHoraIni
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
                    txtFechaFin.text = valFechaFin
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
                    txtHoraFin.text = valHoraFin
                },
                hora,
                minuto,
                false
            )
            horaDialog.show()
        }
        btnGenRepoPrev.setOnClickListener {
            linLayRepo.isGone = false
            // Para generar la grafica, se debieron haber llenado todos los valores previos, si no, se lanzará el aviso correspondiente
            if((estaSel != "") && (sensoSel != "") && (valFechaIni != "") && (valHoraIni != "") && (valFechaFin != "") && (valHoraFin != "")){
                // Generar la grafica del reporte en base a la seleccion
                genGraficaRepo()
                // AlertDialog para ocultar el formulario de seleccion de elementos para el filtrado
                avisoRepo()

                // Dado que se almacenará el reporte, se debera verificar si se conceden los permisos de guardado, previo al boton de generacion de reporte
                if(checarPermisos()){
                    // Si se autorizaron los permisos previamente, se mostrara un aviso, sino se solicitaran
                    Toast.makeText(this@GenReportsActivity, "Permiso de Almacenamiento Concedido...", Toast.LENGTH_SHORT).show()
                }else{
                    pedirPermiso()
                }
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
            // Una vez garantizados los permisos, se procedera con la generacion del PDF
            generarReportePDF()
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

    private fun avisoRepo(){
        val avisoReporte = AlertDialog.Builder(this)
        avisoReporte.setTitle("Aviso")
        avisoReporte.setMessage("Al continuar con la generación del reporte,\nel formulario de selección se ocultará")
        avisoReporte.setPositiveButton("Generar Vista Previa") { _, _ ->
            linLaySel.isGone = true
            linLayRepo.isGone = false
            linlayBtnRepo.isGone = false
        }
        avisoReporte.setNegativeButton("Cancelar"){ dialog, _ ->
            dialog.cancel()
            linLaySel.isGone = false
            linLayRepo.isGone = true
            linlayBtnRepo.isGone = true
        }
        val popUpReporte: AlertDialog = avisoReporte.create()
        popUpReporte.show()
    }

    private fun evalFecha(fechCmp: String, datosFechIni: String, datosFechFin: String): Boolean{
        // Formato de fecha usado para comparar las fechas textuales
        val fechFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        // Transformacion de texto a fecha de las fechas de los registros y los datePicker
        val dateIni = fechFormat.parse(datosFechIni); val dateFin = fechFormat.parse(datosFechFin); val dateCmp = fechFormat.parse(fechCmp)
        // Comparaciones de la fecha obtenida, con la fecha de inicio y la fecha de fin
        val cmpIni = dateCmp?.compareTo(dateIni); val cmpFin = dateCmp?.compareTo(dateFin)
        // Retorna true si la fecha esta dentro del rango seleccionado
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
                                                // Obteniendo el nombre del sensor seleccionado para el reporte
                                                val nomSensoRepo = lblHeadRepo.text.toString() + objSen.child("nom_Sen").value.toString()
                                                lblHeadRepo.text = nomSensoRepo
                                                // Obtener el tipo del sensor de la estacion para generar grafica solo si es sensor de gases
                                                when(objSen.child("tipo").value.toString()){
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
                                                                                // Agregando el valor del registro al dataset de la grafica
                                                                                arrCo.add(Entry(contePosX, objRegi.child("co").value.toString().toFloat()))
                                                                                // Agregando el registro a la plantilla del reporte
                                                                                addRegistro(fechObte, "%.4f".format(objRegi.child("co").value.toString().toFloat()))
                                                                                // Incrementar el contador de las posiciones X para el dataset de la grafica
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
                                                                                // Agregando el valor del registro al dataset de la grafica
                                                                                arrGLP.add(Entry(contePosX, objRegi.child("lpg").value.toString().toFloat()))
                                                                                // Agregando el registro a la plantilla del reporte
                                                                                addRegistro(fechObte, "%.4f".format(objRegi.child("lpg").value.toString().toFloat()))
                                                                                // Incrementar el contador de las posiciones X para el dataset de la grafica
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
                                                                                // Agregando el valor del registro al dataset de la grafica
                                                                                arrProp.add(Entry(contePosX, objRegi.child("propane").value.toString().toFloat()))
                                                                                // Agregando el registro a la plantilla del reporte
                                                                                addRegistro(fechObte, "%.4f".format(objRegi.child("propane").value.toString().toFloat()))
                                                                                // Incrementar el contador de las posiciones X para el dataset de la grafica
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
                                                                                // Agregando el valor del registro al dataset de la grafica
                                                                                arrHumo.add(Entry(contePosX, objRegi.child("smoke").value.toString().toFloat()))
                                                                                // Agregando el registro a la plantilla del reporte
                                                                                addRegistro(fechObte, "%.4f".format(objRegi.child("smoke").value.toString().toFloat()))
                                                                                // Incrementar el contador de las posiciones X para el dataset de la grafica
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


    private fun addRegistro(fechaRegistro: String, valRegi: String){
        // Crear el contenedor linearlayout horizontal
        val nLinLay = LinearLayout(this@GenReportsActivity)
        nLinLay.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        nLinLay.orientation = LinearLayout.HORIZONTAL
        // Crear los textview, primero la fecha
        val nTextFecha = TextView(this@GenReportsActivity)
        nTextFecha.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        nTextFecha.gravity = Gravity.CENTER
        nTextFecha.setTextColor(ContextCompat.getColor(this@GenReportsActivity,R.color.negro))
        nTextFecha.textSize = spToPx(6F)
        nTextFecha.text = fechaRegistro
        // Despues el valor del registro
        val nTextRegi = TextView(this@GenReportsActivity)
        nTextRegi.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        nTextRegi.setPadding(dpToPx(60F).toInt(), 0, dpToPx(10F).toInt(), 0)
        nTextRegi.setTextColor(ContextCompat.getColor(this@GenReportsActivity,R.color.negro))
        nTextRegi.textSize = spToPx(6F)
        nTextRegi.text = valRegi
        // Agregar un espacio
        val nEspacio = Space(this@GenReportsActivity)
        nEspacio.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5F).toInt())
        // Agregar las vistas al linearLayout
        nLinLay.addView(nTextFecha)
        nLinLay.addView(nTextRegi)
        // Agregando al layout de registros el nuevo registro y el espacio
        linLayRegs.addView(nLinLay)
        linLayRegs.addView(nEspacio)
    }

    // Funciones de conversion
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return sp * resources.displayMetrics.scaledDensity
    }

    // Fecha reporte
    private fun fechaReporte(): String {
        // Creando la fecha del cambio
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("CST"))
        val dia = calendar.get(Calendar.DAY_OF_MONTH); val mes = calendar.get(Calendar.MONTH) + 1; val year = calendar.get(Calendar.YEAR)
        val hora = calendar.get(Calendar.HOUR_OF_DAY); val minuto = calendar.get(Calendar.MINUTE)

        return "${transFecha(dia)}-${transFecha(mes)}-${transFecha(year)}_${transFecha(hora)}:${transFecha(minuto)}"
    }

    // Transformacion de fecha agregando 0 si es menor de 10
    private fun transFecha(valor: Int):String {
        return if(valor < 10){
            "0$valor"
        }else{
            valor.toString()
        }
    }

    // Elementos de la creacion del reporte
    private fun checarPermisos(): Boolean {
        // Constante de evaluacion para el checar permiso de escritura en el dispositivo
        val writeStoragePermission = ContextCompat.checkSelfPermission( this@GenReportsActivity, WRITE_EXTERNAL_STORAGE )
        // Constante de evaluacion para el checar permiso de lectura en el dispositivo
        val readStoragePermission = ContextCompat.checkSelfPermission( this@GenReportsActivity, READ_EXTERNAL_STORAGE )
        // Regresa un true si se cuentan con ambos permisos
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    // Peticion de permisos, se hace un request permision, similar al startActivityResult
    private fun pedirPermiso(){
        ActivityCompat.requestPermissions(this@GenReportsActivity, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codSoliRepo )
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == codSoliRepo) {
            // Se evalua si el arreglo de los permisos no esta vacio
            if (grantResults.isNotEmpty()) {
                // Se evalua si ambos permisos estan autorizados; READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Si los permisos fueron concedidos, se mostrara el mensaje de permisos concedidos
                    Toast.makeText(this@GenReportsActivity, "Permisos Concedidos...", Toast.LENGTH_SHORT).show()
                } else {
                    // En caso contrario, se mostrara el mensaje de permisos denegados y se terminara el activity permisions
                    Toast.makeText(this@GenReportsActivity, "Permisos Denegados...", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun retorno(){
        return this.onBackPressedDispatcher.onBackPressed()
    }

    private fun generarReportePDF(){
        // "Inflar la interfaz", obteniendo las dimensiones del layout del reporte en la vista de la activity
        val vista = LayoutInflater.from(this@GenReportsActivity).inflate(R.layout.user_activity_gen_reports, linLayRepo)
        val layoutWidth = linLayRepo.width
        val layoutHeight = linLayRepo.height
        // Estableciendo el tamaño de la vista y creando el layout del documento en base a este tamaño
        vista.measure(View.MeasureSpec.makeMeasureSpec(layoutWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(layoutHeight, View.MeasureSpec.EXACTLY))
        vista.layout(0, 0, layoutWidth, layoutHeight)

        // Crear el documento PDF
        val documento = PdfDocument()
        // Obteniendo el ancho y alto de la vista
        val vistaWidth = vista.measuredWidth
        val vistaHeight = vista.measuredHeight
        // Crear el pageInfo para establecer las caracteristicas de las paginas del PDF
        val infoPagi = PdfDocument.PageInfo.Builder(vistaWidth, vistaHeight, 1).create()
        // Crear una nueva pagina
        val pagina = documento.startPage(infoPagi)
        // Crear un canvas para dibujar en la pagina
        val canvas = pagina.canvas
        // Crear un objeto paint para establecer el estilo de la vista
        val paint = Paint()
        paint.color = ContextCompat.getColor(this,R.color.blanco)
        // Dibujar la vista en el canvas
        vista.draw(canvas)
        // Finalizar la pagina
        documento.finishPage(pagina)

        // Especificar la ruta de descargas, el nombre de archivo de guardado y la ruta completa de guardado
        val rutaDescarga = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val nombre = "Reporte Ardu_Security ${fechaReporte()}.pdf"
        val rutaGuardado = File(rutaDescarga, nombre)

        try {
            // Guardando el documento; Creando el objeto de archivo de salida
            val archivo = FileOutputStream(rutaGuardado)
            // Escribiendo el PDF en el archivo de salida
            documento.writeTo(archivo)
            // Cerrando la elaboracion del documento PDF y el archivo de salida
            documento.close()
            archivo.close()
            // Parchado de muestra de formularios
            linLaySel.isGone = true
            linLayRepo.isGone = false
            linlayBtnRepo.isGone = true
            // Mensaje de descarga completada con exito
            Toast.makeText(this@GenReportsActivity, "El reporte ha sido guardado satisfactoriamente en sus descargas", Toast.LENGTH_SHORT).show()
            // Retornando al dashboard
            Timer().schedule(1500) {
                lifecycleScope.launch(Dispatchers.Main) {
                    retorno()
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    finish()
                }
            }
        }catch (exception: IOException){
            Toast.makeText(this@GenReportsActivity, "Error: No se pudo crear el reporte; razon:\n${exception.printStackTrace()}", Toast.LENGTH_SHORT).show()
        }
    }
}