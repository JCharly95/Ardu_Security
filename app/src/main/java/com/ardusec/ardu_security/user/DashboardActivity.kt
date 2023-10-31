package com.ardusec.ardu_security.user

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.MainActivity
import com.ardusec.ardu_security.R
import com.ardusec.ardu_security.admin.ManageSisActivity
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
import java.util.Timer
import kotlin.concurrent.schedule


class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var linLaySelSis: LinearLayout
    private lateinit var btnShowSis: Button
    private lateinit var spSistemas: AppCompatSpinner
    private lateinit var btnMenEsta: ImageButton
    private lateinit var btnMenRepo: ImageButton
    private lateinit var btnMenAjus: ImageButton
    private lateinit var btnMenManu: ImageButton
    private lateinit var linLayGesSis: LinearLayout
    private lateinit var btnMenGesSis: ImageButton
    private lateinit var btnCerSes: ImageButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var tipo: String
    private lateinit var sistema: String
    private lateinit var sisKey: String
    // Bandera de activacion para el spínner
    private var bandeListen = false
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_dashboard)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@DashboardActivity,R.color.teal_700)))
        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            tipo = "Cliente"
            Toast.makeText(this@DashboardActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            tipo = bundle.getString("tipo").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Configurando el backPressed
        onBackPressedDispatcher.addCallback(this, presAtrasCallback)
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Dashboard"
        // Relacionando los elementos con su objeto de la interfaz
        btnAyuda = findViewById(R.id.btnInfoDash)
        linLaySelSis = findViewById(R.id.linLaySpSis)
        btnShowSis = findViewById(R.id.btnMosOcuSelSis)
        spSistemas = findViewById(R.id.spSisDash)
        btnMenEsta = findViewById(R.id.btnStats)
        btnMenRepo = findViewById(R.id.btnGenRep)
        btnMenAjus = findViewById(R.id.btnAjuste)
        btnMenManu = findViewById(R.id.btnManUs)
        linLayGesSis = findViewById(R.id.linLayBtnGesSis)
        btnMenGesSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database
        // Rellenar el spinner de los sistemas
        rellSistemas()

        // Lanzar aviso si el usuario no tiene sistemas
        if(spSistemas.adapter.count == 1){
            val msg = "Estimado usuario, en este momento no estas relacionado a ningun sistema, por lo que no podras acceder a muchas de las funcionalidades.\n" +
                    "Es necesario que se cambie esto, para ello deberas acceder a: \n\n" +
                    "Ajustes > Editar Informacion de Usuario > Cambiar Sistema"
            avisoDash(msg)
        }
    }

    private fun addListeners() {
        // Agregar los listener
        btnAyuda.setOnClickListener {
            val msg = "--Descripciones de los elementos en pantalla:\n\n" +
                    "* Mostrar/Ocultar Seleccion Sistema: Muestra la lista de sistemas a los que el usuario pertenece y se deberá seleccionar el sistema a utilizar en la sesion actual.\n" +
                    "* NOTA: Los botones Estaciones y Gestionar Sistema REQUIEREN seleccionar un sistema.\n\n" +
                    "* Boton Estaciones: Lleva al usuario a la ventana de visualización de las estaciones. \n\n" +
                    "* Boton Reportes: Lleva al usuario a la ventana de generación de reportes. \n\n" +
                    "* Boton Ajustes: Lleva al usuario a la ventana de ajustes de información y otros elementos relacionados con el usuario. \n\n" +
                    "* Boton Manual de Usuario: Lleva al usuarioa a la ventana de visualizacion del manual de usuario. \n\n" +
                    "* Boton Gestionar Sistema: Lleva a los administradores a la ventana de gestion del sistema. \n\n" +
                    "* Boton Cerrar Sesion: Cierra la sesion actual y redigire al usuario al inicio de sesion."
            avisoDash(msg)
        }
        btnShowSis.setOnClickListener {
            linLaySelSis.isGone = !linLaySelSis.isGone
        }
        spSistemas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!bandeListen) {
                    bandeListen = true
                    return
                }
                // Estableciendo el sistema de la sesion y verificando el tipo de usuario en cuestion
                sistema = parent!!.getItemAtPosition(position).toString()
                verAdmin(sistema)
            }
        }
        btnMenEsta.setOnClickListener {
            if(validarSelSis(spSistemas)){
                val statsActi = Intent(this@DashboardActivity, MenuStationsActivity::class.java).apply {
                    putExtra("username", user)
                    putExtra("sistema", sisKey)
                }
                startActivity(statsActi)
            }
        }
        btnMenRepo.setOnClickListener {
            val reportActi = Intent(this@DashboardActivity, GenReportsActivity::class.java).apply {
                putExtra("username", user)
            }
            startActivity(reportActi)
        }
        btnMenAjus.setOnClickListener {
            val settingActi = Intent(this@DashboardActivity, SettingsActivity::class.java).apply {
                putExtra("username", user)
            }
            startActivity(settingActi)
        }
        btnMenManu.setOnClickListener {

        }
        btnMenGesSis.setOnClickListener {
            if(validarSelSis(spSistemas)){
                val gesSisActi = Intent(this@DashboardActivity, ManageSisActivity::class.java).apply {
                    putExtra("username", user)
                    putExtra("sistema", sisKey)
                }
                startActivity(gesSisActi)
            }
        }
        btnCerSes.setOnClickListener {
            // Cerrar Sesion en Firebase
            FirebaseAuth.getInstance().signOut()
            // Lanzar la app hacia la primera ventana
            avisoDash("Cerrando Sesion... Espere un momento")
            Timer().schedule(1500){
                val endActi = Intent(this@DashboardActivity, MainActivity::class.java)
                startActivity(endActi)
                finish()
            }
        }
    }

    private val presAtrasCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val msg = "Boton deshabilitado.\n\n " +
                    "Si desea regresar al inicio o iniciar sesion, favor de cerrar su sesión antes."
            avisoDash(msg)
        }
    }

    private fun avisoDash(mensaje: String) {
        val aviso = AlertDialog.Builder(this@DashboardActivity)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun rellSistemas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                val arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children) {
                            val usRef = objSis.ref.child("usuarios")
                            usRef.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(objUs in snapshot.children){
                                        if(objUs.key.toString() == user){
                                            arrSists.add(objSis.child("nombre_Sis").value.toString())
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spSistemas.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellSis.await()
        }
    }

    private fun verAdmin(sisSel: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val showEditSis = async {
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children) {
                            if(objSis.child("nombre_Sis").value.toString() == sisSel){
                                val usRef = objSis.ref.child("usuarios")
                                usRef.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objUs in snapshot.children){
                                            if(objUs.key.toString() == user){
                                                sisKey = objSis.key.toString()
                                                if(objUs.value.toString() == "Administrador")
                                                    linLayGesSis.isGone = false
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            showEditSis.await()
        }
    }

    private fun validarSelSis(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su Sistema") {
            true
        }else{
            lifecycleScope.launch(Dispatchers.Main) {
                avisoDash("Error: Favor de seleccionar un sistema")
            }
            false
        }
    }
}