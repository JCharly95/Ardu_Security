package com.ardusec.ardu_security.admin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.EditDataTxtActivity
import com.ardusec.ardu_security.R
import com.ardusec.ardu_security.user.AlarmActivity
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

class ManageSisActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnSisChgNam: ImageButton
    private lateinit var btnSisChgSta: ImageButton
    private lateinit var btnAdminUs: ImageButton
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    //private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_sis)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))

        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            Toast.makeText(this@ManageSisActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            sistema = bundle.getString("sistema").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Menu del Administrador del Sistema"
        // Relacionando los elementos con su objeto de la interfaz
        btnSisChgNam = findViewById(R.id.btnSisNam)
        btnSisChgSta = findViewById(R.id.btnSisSta)
        btnAdminUs = findViewById(R.id.btnSisGesUs)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database
    }

    private fun addListeners(){
        // Agregando los listener
        btnSisChgNam.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val getSisData = async {
                    // Creando la referencia de la coleccion de sistemas en la BD
                    ref = database.getReference("Sistemas")
                    // Agregando un ValueEventListener para operar con las instancias de los sistemas
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objSis in dataSnapshot.children) {
                                if(objSis.key.toString() == sistema){
                                    val chgNamSisActi = Intent(this@ManageSisActivity, EditDataTxtActivity::class.java).apply {
                                        putExtra("sistema", sistema)
                                        putExtra("usuario", user)
                                        putExtra("campo", "Nombre Sistema")
                                    }
                                    startActivity(chgNamSisActi)
                                    break
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(this@ManageSisActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                getSisData.await()
            }
        }
        btnAdminUs.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val getSisData = async {
                    // Creando la referencia de la coleccion de sistemas en la BD
                    ref = database.getReference("Sistemas")
                    // Agregando un ValueEventListener para operar con las instancias de los sistemas
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objSis in dataSnapshot.children) {
                                if(objSis.key.toString() == sistema){
                                    val menGesUserActi = Intent(this@ManageSisActivity, ManageSisUsersActivity::class.java).apply {
                                        putExtra("sistema", sistema)
                                        putExtra("usuario", user)
                                    }
                                    startActivity(menGesUserActi)
                                    break
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(this@ManageSisActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                getSisData.await()
            }
        }
        btnSisChgSta.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val getSisData = async {
                    // Creando la referencia de la coleccion de sistemas en la BD
                    ref = database.getReference("Sistemas")
                    // Agregando un ValueEventListener para operar con las instancias de los sistemas
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objSis in dataSnapshot.children) {
                                if(objSis.key.toString() == sistema){
                                    val menGesStaActi = Intent(this@ManageSisActivity, ManageSisStationsActivity::class.java).apply {
                                        putExtra("sistema", sistema)
                                        putExtra("usuario", user)
                                    }
                                    startActivity(menGesStaActi)
                                    break
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(this@ManageSisActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                getSisData.await()
            }
        }
    }
}