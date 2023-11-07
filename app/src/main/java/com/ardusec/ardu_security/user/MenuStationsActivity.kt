package com.ardusec.ardu_security.user

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MenuStationsActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var linLayEsta12: LinearLayout
    private lateinit var linLayBtn1: LinearLayout
    private lateinit var btnSta1: ImageButton
    private lateinit var linLayBtn2: LinearLayout
    private lateinit var btnSta2: ImageButton
    private lateinit var linLayEsta34: LinearLayout
    private lateinit var linLayBtn31: LinearLayout
    private lateinit var btnSta31: ImageButton
    private lateinit var linLayBtn4: LinearLayout
    private lateinit var btnSta4: ImageButton
    private lateinit var linLayEsta3Ala: LinearLayout
    private lateinit var linLayBtn32: LinearLayout
    private lateinit var btnSta32: ImageButton
    private lateinit var linLayAlarm2: LinearLayout
    private lateinit var btnAlarma2: ImageButton
    private lateinit var linLayEsta5Ala: LinearLayout
    private lateinit var linLayBtn5: LinearLayout
    private lateinit var btnSta5: ImageButton
    private lateinit var linLayAlarm: LinearLayout
    private lateinit var btnAlarma: ImageButton
    // Variables de evaluacion para determinar la cantidad de botones a mostrar
    private var cantEsta: Long = 0
    private val arrEstasKey = ArrayList<String>()
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_menu_stations)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))
        //Obteniendo los valores de acceso/registro
        if(intent.extras == null){
            Toast.makeText(this@MenuStationsActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
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
        title = "Menu Estaciones"
        // Relacionando los elementos con su objeto de la interfaz
        linLayEsta12 = findViewById(R.id.linLayEsta12)
        linLayBtn1 = findViewById(R.id.LinBtnEsta1)
        btnSta1 = findViewById(R.id.btnStat1)
        linLayBtn2 = findViewById(R.id.LinBtnEsta2)
        btnSta2 = findViewById(R.id.btnStat2)
        linLayEsta34 = findViewById(R.id.linLayEsta34)
        linLayBtn31 = findViewById(R.id.LinBtnEsta31)
        btnSta31 = findViewById(R.id.btnStat31)
        linLayBtn4 = findViewById(R.id.LinBtnEsta41)
        btnSta4 = findViewById(R.id.btnStat41)
        linLayEsta3Ala = findViewById(R.id.linLayEsta3Ala)
        linLayBtn32 = findViewById(R.id.LinBtnEsta32)
        btnSta32 = findViewById(R.id.btnStat32)
        linLayAlarm2 = findViewById(R.id.LinBtnAlarm2)
        btnAlarma2 = findViewById(R.id.btnAlarma2)
        linLayEsta5Ala = findViewById(R.id.linLayEsta5Ala)
        linLayBtn5 = findViewById(R.id.LinBtnEsta5)
        btnSta5 = findViewById(R.id.btnStat5)
        linLayAlarm = findViewById(R.id.LinBtnAlarm)
        btnAlarma = findViewById(R.id.btnAlarma)

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
        // Establecer los botones a mostrar
        setupBtn()
    }

    private fun addListeners() {
        // Agregando los listener
        btnSta1.setOnClickListener {
            lanzarEstacion(0)
        }
        btnSta2.setOnClickListener {
            lanzarEstacion(1)
        }
        btnSta31.setOnClickListener {
            lanzarEstacion(2)
        }
        btnSta32.setOnClickListener {
            lanzarEstacion(2)
        }
        btnSta4.setOnClickListener {
            lanzarEstacion(3)
        }
        btnSta5.setOnClickListener {
            lanzarEstacion(4)
        }
        btnAlarma.setOnClickListener {
            lanzarAlarma()
        }
        btnAlarma2.setOnClickListener {
            lanzarAlarma()
        }
    }

    private fun setupBtn(){
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
                                        linLayEsta12.isGone = false
                                        linLayEsta3Ala.isGone = false
                                    }
                                    5 -> {
                                        linLayEsta12.isGone = false
                                        linLayEsta34.isGone = false
                                        linLayEsta5Ala.isGone = false
                                    }
                                    else -> {
                                        Toast.makeText(this@MenuStationsActivity,"Error: Cantidad del sistema inadecuada",Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(this@MenuStationsActivity,"Error: Consulta erronea",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@MenuStationsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setBtnsEstas.await()
        }
    }

    private fun lanzarEstacion(numBtn: Int){
        val staActi = Intent(this@MenuStationsActivity, StationActivity::class.java).apply {
            putExtra("username", user)
            putExtra("key_station", arrEstasKey[numBtn])
        }
        startActivity(staActi)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    private fun lanzarAlarma(){
        val alaActi = Intent(this@MenuStationsActivity, AlarmActivity::class.java).apply {
            putExtra("username", user)
            putExtra("sistema", sistema)
        }
        startActivity(alaActi)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}