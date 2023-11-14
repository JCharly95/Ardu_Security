package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    private lateinit var space4: Space
    private lateinit var space5: Space
    private lateinit var rbSenGLP: RadioButton
    private lateinit var rbSenCo: RadioButton
    private lateinit var rbSenGProp: RadioButton
    private lateinit var rbSenHumo: RadioButton
    private lateinit var dateRanIni: DatePicker
    private lateinit var lblFechaIni: TextView
    private lateinit var dateRanFin: DatePicker
    private lateinit var lblFechaFin: TextView
    private lateinit var btnGenRepo: Button
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Variables de evaluacion para determinar la cantidad de botones a mostrar
    private var cantEsta: Long = 0
    private val arrEstasKey = ArrayList<String>()
    // Variables de valor de seleccion
    private var estaSel: String = ""
    private var sensoSel: String = ""
    private var valRanIni: String = ""
    private var valRanFin: String = ""

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
        space4 = findViewById(R.id.spaEsta4)
        rbSelEst4 = findViewById(R.id.rbSelEsta4)
        space5 = findViewById(R.id.spaEsta5)
        rbSelEst5 = findViewById(R.id.rbSelEsta5)
        rbSenGLP = findViewById(R.id.rbSelSenGLP)
        rbSenCo = findViewById(R.id.rbSelSenCO2)
        rbSenGProp = findViewById(R.id.rbSelSenProp)
        rbSenHumo = findViewById(R.id.rbSelSenHumo)
        dateRanIni = findViewById(R.id.calRangoIni)
        lblFechaIni = findViewById(R.id.lblFechIni)
        dateRanFin = findViewById(R.id.calRangoFin)
        lblFechaFin = findViewById(R.id.lblFechFin)
        btnGenRepo = findViewById(R.id.btnConfGenRepo)

        // Inicializando los calendarios
        iniCalens()

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        setUpBtnEsta()
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
                                        space4.isGone = true
                                        rbSelEst4.isGone = true
                                        space5.isGone = true
                                        rbSelEst5.isGone = true
                                    }
                                    5 -> {
                                        space4.isGone = false
                                        rbSelEst4.isGone = false
                                        space5.isGone = false
                                        rbSelEst5.isGone = false
                                    }
                                    else -> {
                                        Toast.makeText(this@GenReportsActivity,"Error: Cantidad del sistema inadecuada",Toast.LENGTH_SHORT).show()
                                    }
                                }
                                // Obteniendo las ID de las estaciones
                                val refEsta = refSnap.ref
                                refEsta.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objEsta in snapshot.children){
                                            arrEstasKey.add(objEsta.key.toString())
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

    private fun avisoGenRepo(mensaje: String) {
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun iniCalens() {
        // Creando la fecha del cambio, NOTA: La zona horaria se va a establecer con el acronimo CST que es el usado para la zona de la CDMX
        val calendar1 = Calendar.getInstance(TimeZone.getTimeZone("CST"))
        val calendar2 = Calendar.getInstance(TimeZone.getTimeZone("CST"))
        dateRanIni.init(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), calendar1.get(Calendar.DAY_OF_MONTH))
        {   _, year, mes, dia ->
            val mes = mes + 1
            val mesCon = if(mes < 10){
                "0$mes"
            }else{
                mes
            }
            val diaCon = if(dia < 10){
                "0$dia"
            }else{
                dia
            }
            valRanIni = "$diaCon-$mesCon-$year"
            lblFechaIni.text = valRanIni
        }
        dateRanFin.init(calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH), calendar2.get(Calendar.DAY_OF_MONTH))
        {   _, year, mes, dia ->
            val mes = mes + 1
            val mesCon = if(mes < 10){
                "0$mes"
            }else{
                mes
            }
            val diaCon = if(dia < 10){
                "0$dia"
            }else{
                dia
            }
            valRanFin = "$diaCon-$mesCon-$year"
            lblFechaFin.text = valRanFin
        }
    }

    private fun addListeners() {
        btnAyuda.setOnClickListener {
            val msg = "Consideraciones de campos: \n\n" +
                    "Nombre;\n" +
                    "* Su nombre no debe tener numeros\n" +
                    "* Su nombre debe tener al menos 10 caracteres\n\n" +
                    "Correo; Formato Aceptado:\n" +
                    "* usuario@dominio.com(.mx)\n\n" +
                    "Contraseña:\n" +
                    "* Extension minima de 8 caracteres\n" +
                    "* Por lo menos una mayuscula\n" +
                    "* Por lo menos un numero\n" +
                    "* Por lo menos  un caracter especial\n\n" +
                    "Administradores; Numero Telefonico:\n" +
                    "* Solo se permiten numeros\n" +
                    "* Lada + Numero ó Tel. Celular\n\n" +
                    "** NOTA: Para el cambio de correo o contraseña, se le " +
                    "solicitara la contraseña como confirmacion de cambio."
            avisoGenRepo(msg)
        }
        rbSelEst1.setOnClickListener {
            if(rbSelEst1.isChecked)
                estaSel = "Estacion1"
        }
        rbSelEst2.setOnClickListener {
            if(rbSelEst2.isChecked)
                estaSel = "Estacion2"
        }
        rbSelEst3.setOnClickListener {
            if(rbSelEst3.isChecked)
                estaSel = "Estacion3"
        }
        rbSelEst4.setOnClickListener {
            if(rbSelEst4.isChecked)
                estaSel = "Estacion4"
        }
        rbSelEst5.setOnClickListener {
            if(rbSelEst5.isChecked)
                estaSel = "Estacion5"
        }
        rbSenGLP.setOnClickListener {
            if(rbSenGLP.isChecked)
                sensoSel = "Gas"
        }
        rbSenCo.setOnClickListener {
            if(rbSenCo.isChecked)
                sensoSel = "Co"
        }
        rbSenGProp.setOnClickListener {
            if(rbSenGProp.isChecked)
                sensoSel = "Propano"
        }
        rbSenHumo.setOnClickListener {
            if(rbSenHumo.isChecked)
                sensoSel = "Humo"
        }
        btnGenRepo.setOnClickListener {

        }
    }
}