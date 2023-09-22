package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MenuStationsActivity : AppCompatActivity() {
    private lateinit var btnSta1: ImageButton
    private lateinit var linLayBtn1: LinearLayout
    private lateinit var btnSta2: ImageButton
    private lateinit var linLayBtn2: LinearLayout
    private lateinit var btnSta3: ImageButton
    private lateinit var linLayBtn3: LinearLayout
    private lateinit var btnSta4: ImageButton
    private lateinit var linLayBtn4: LinearLayout
    private lateinit var btnSta5: ImageButton
    private lateinit var linLayBtn5: LinearLayout
    private lateinit var btnAlarma: ImageButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var tipo: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_stations)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo los valores de acceso/registro
        if(intent.extras == null){
            tipo = "Cliente"
            Toast.makeText(this@MenuStationsActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Menu Estaciones"
        // Relacionando los elementos con su objeto de la interfaz
        btnSta1 = findViewById(R.id.btnStat1)
        btnSta2 = findViewById(R.id.btnStat2)
        btnSta3 = findViewById(R.id.btnStat3)
        btnSta4 = findViewById(R.id.btnStat4)
        btnSta5 = findViewById(R.id.btnStat5)
        btnAlarma = findViewById(R.id.btnAlarma)
        linLayBtn1 = findViewById(R.id.LinBtnEsta1)
        linLayBtn2 = findViewById(R.id.LinBtnEsta2)
        linLayBtn3 = findViewById(R.id.LinBtnEsta3)
        linLayBtn4 = findViewById(R.id.LinBtnEsta4)
        linLayBtn5 = findViewById(R.id.LinBtnEsta5)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database

        // Habilitando solo los botones adecuados de acuerdo al plan del sistema
        setupBtn()
    }

    private fun addListeners() {
        // Agregando los listener
        btnSta1.setOnClickListener {
            lanzarEstacion(1)
        }
        btnSta2.setOnClickListener {
            lanzarEstacion(2)
        }
        btnSta3.setOnClickListener {
            lanzarEstacion(3)
        }
        btnSta4.setOnClickListener {
            lanzarEstacion(4)
        }
        btnSta5.setOnClickListener {
            lanzarEstacion(5)
        }
        btnAlarma.setOnClickListener {
            lanzarAlarma()
        }
    }

    private fun avisoMenSta(){
        val mensaje = "Error: No se pudieron obtener los valores solicitados"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setupBtn() {
        lifecycleScope.launch(Dispatchers.IO) {
            val setBtnsEstas = async {
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSis in dataSnapshot.children) {
                            val refUser = objSis.child("usuarios").ref
                            refUser.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(usSis in snapshot.children) {
                                        if(usSis.key.toString() == user) {
                                            if(objSis.child("tipo").value == "Avanzado") {
                                                linLayBtn1.isGone = false
                                                linLayBtn2.isGone = false
                                                linLayBtn3.isGone = false
                                                linLayBtn4.isGone = false
                                                linLayBtn5.isGone = false
                                                break
                                            }else{
                                                linLayBtn1.isGone = false
                                                linLayBtn2.isGone = false
                                                linLayBtn3.isGone = false
                                                linLayBtn4.isGone = true
                                                linLayBtn5.isGone = true
                                                break
                                            }
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    avisoMenSta()
                                }
                            })
                            break
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoMenSta()
                    }
                })
            }
            setBtnsEstas.await()
        }
    }
    private fun lanzarEstacion(numBtn: Int){
        lifecycleScope.launch(Dispatchers.IO) {
            val setBtnEsta5 = async {
                // Creando la referencia de la coleccion de usuarios en la BD
                ref = database.getReference("Usuarios").child(user).child("sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(sisUs in dataSnapshot.children){
                            if(dataSnapshot.childrenCount.toInt() == 1){
                                val sistema = sisUs.key.toString()
                                var contEsta = 1
                                val refEsta = database.getReference("Estaciones")
                                refEsta.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(sisRel in snapshot.children){
                                            if(sisRel.child("sistema_Rel").value == sistema && contEsta == numBtn){
                                                val intActEst5 = Intent(this@MenuStationsActivity,StationActivity::class.java).apply {
                                                    putExtra("username", user)
                                                    putExtra("name_station", sisRel.key.toString())
                                                }
                                                startActivity(intActEst5)
                                            }
                                            contEsta++
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        avisoMenSta()
                                    }
                                })
                                break
                            }else{
                                // Preparar algo aqui para cuando se tenga mas de un sistema
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoMenSta()
                    }
                })
            }
            setBtnEsta5.await()
        }
    }
    private fun lanzarAlarma(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setBtnAla = async {
                // Creando la referencia de la coleccion de usuarios en la BD
                ref = database.getReference("Usuarios").child(user).child("sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(sisUs in dataSnapshot.children){
                            if(dataSnapshot.childrenCount.toInt() == 1){
                                val sistema = sisUs.key.toString()
                                val refAla = database.getReference("Alarmas")
                                refAla.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(sisRel in snapshot.children){
                                            if(sisRel.child("sistema_Rel").value == sistema){
                                                val intentAlarma = Intent(this@MenuStationsActivity, AlarmActivity::class.java).apply {
                                                    putExtra("sistema", sistema)
                                                }
                                                startActivity(intentAlarma)
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        avisoMenSta()
                                    }
                                })
                                break
                            }else{
                                // Preparar algo aqui para cuando se tenga mas de un sistema
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoMenSta()
                    }
                })
            }
            setBtnAla.await()
        }
    }
}