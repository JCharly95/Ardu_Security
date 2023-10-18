package com.ardusec.ardu_security.user

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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

class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var spSistemas: AppCompatSpinner
    private lateinit var btnMenEsta: ImageButton
    private lateinit var btnGenRep: ImageButton
    private lateinit var btnMenAj: ImageButton
    private lateinit var btnManual: ImageButton
    private lateinit var btnMenSis: ImageButton
    private lateinit var btnCerSes: ImageButton
    private lateinit var linLayGesSis: LinearLayout
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var tipo: String
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

    private val presAtrasCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val msg = "Boton deshabilitado.\n" +
                    "Si desea regresar al inicio o iniciar sesion, favor de cerrar su sesión antes."
            avisoDash(msg)
        }
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Dashboard"
        // Relacionando los elementos con su objeto de la interfaz
        spSistemas = findViewById(R.id.spSistemas)
        btnMenEsta = findViewById(R.id.btnStats)
        btnGenRep = findViewById(R.id.btnGenRep)
        btnMenAj = findViewById(R.id.btnAjuste)
        btnManual = findViewById(R.id.btnManUs)
        linLayGesSis = findViewById(R.id.LinBtnEsta5)
        btnMenSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        // Inicializando el Spinner
        sistemas()

        if(tipo == "Administrador"){
            linLayGesSis.isGone = false
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

    private fun sistemas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                var arrSists = ArrayList<String>()
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

    private fun addListeners() {
        // Agregar los listener
        btnMenEsta.setOnClickListener {
            if(validarSelSis(spSistemas)){
                lifecycleScope.launch(Dispatchers.IO){
                    val getSis = async {
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objSis in dataSnapshot.children) {
                                    if(objSis.child("nombre_Sis").value.toString() == spSistemas.selectedItem.toString()) {
                                        val statsActi = Intent(this@DashboardActivity, MenuStationsActivity::class.java).apply {
                                            putExtra("username", user)
                                            putExtra("sistema", objSis.key.toString())
                                        }
                                        startActivity(statsActi)
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getSis.await()
                }
            }
        }
        btnGenRep.setOnClickListener {
            if(validarSelSis(spSistemas)){
                lifecycleScope.launch(Dispatchers.IO){
                    val getSis = async {
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objSis in dataSnapshot.children) {
                                    if(objSis.child("nombre_Sis").value.toString() == spSistemas.selectedItem.toString()) {
                                        val reportActi = Intent(this@DashboardActivity, GenReportsActivity::class.java).apply {
                                            putExtra("username", user)
                                            putExtra("sistema", objSis.key.toString())
                                        }
                                        startActivity(reportActi)
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getSis.await()
                }
            }
        }
        btnMenAj.setOnClickListener {
            if(validarSelSis(spSistemas)){
                lifecycleScope.launch(Dispatchers.IO){
                    val getSis = async {
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objSis in dataSnapshot.children) {
                                    if(objSis.child("nombre_Sis").value.toString() == spSistemas.selectedItem.toString()) {
                                        val settingActi = Intent(this@DashboardActivity, SettingsActivity::class.java).apply {
                                            putExtra("username", user)
                                            putExtra("sistema", objSis.key.toString())
                                        }
                                        startActivity(settingActi)
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getSis.await()
                }
            }
        }
        btnManual.setOnClickListener {

        }
        btnMenSis.setOnClickListener {
            if(validarSelSis(spSistemas)){
                lifecycleScope.launch(Dispatchers.IO){
                    val getSis = async {
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objSis in dataSnapshot.children) {
                                    if(objSis.child("nombre_Sis").value.toString() == spSistemas.selectedItem.toString()) {
                                        val gesSisActi = Intent(this@DashboardActivity, ManageSisActivity::class.java).apply {
                                            putExtra("username", user)
                                            putExtra("sistema", objSis.key.toString())
                                        }
                                        startActivity(gesSisActi)
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DashboardActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getSis.await()
                }
            }
        }
        btnCerSes.setOnClickListener {
            // Cerrar Sesion en Firebase
            FirebaseAuth.getInstance().signOut()
            // Lanzar la app hacia la primera ventana
            val endActi = Intent(this@DashboardActivity, MainActivity::class.java)
            startActivity(endActi)
            finish()
        }
    }

    private fun validarSelSis(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su Sistema") {
            true
        }else{
            avisoDash("Error: Favor de seleccionar un sistema")
            false
        }
    }
}