package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ardusec.ardu_security.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar


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
    private lateinit var dateRanFin: DatePicker
    private lateinit var btnGenRepo: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    //private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Variables de valor de seleccion
    private var estaSel = ""
    private var sensoSel = ""
    private var valRanIni = ""
    private var valRanFin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_gen_reports)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))

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
        dateRanFin = findViewById(R.id.calRangoFin)
        btnGenRepo = findViewById(R.id.btnConfGenRepo)

        // Inicializando los calendarios
        iniCalens()

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database
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
        val calendar = Calendar.getInstance()
        dateRanIni.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        {   _, year, mes, dia ->
            val mes = mes + 1
            val mesCon = if(mes < 10){
                "0$mes"
            }else{
                mes + 1
            }
            val diaCon = if(dia < 10){
                "0$dia"
            }else{
                dia
            }
            valRanIni = "$diaCon/$mesCon/$year"
        }
        dateRanFin.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        {   _, year, mes, dia ->
            val mes = mes + 1
            val mesCon = if(mes < 10){
                "0$mes"
            }else{
                mes + 1
            }
            val diaCon = if(dia < 10){
                "0$dia"
            }else{
                dia
            }
            valRanFin = "$diaCon/$mesCon/$year"
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