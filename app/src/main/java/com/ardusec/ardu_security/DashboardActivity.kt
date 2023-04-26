package com.ardusec.ardu_security

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnEstas: Button
    private lateinit var btnGenRep: Button
    private lateinit var btnEdiPerf: Button
    private lateinit var btnMenAj: Button
    private lateinit var btnManual: Button
    private lateinit var btnManSis: Button
    private lateinit var btnCerSes: Button
    // Creando el objeto GSON
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Crear un bundle para los extras
        val bundle = intent.extras
        // Saber si el usuario vera el boton de gestion o no
        val corrAcc = bundle?.getString("correo")
        val contraAcc = bundle?.getString("contra")
        // Kotlin se protege en caso de que no haya extras por eso se necesita establecer el ?
        btnGestSis(corrAcc?: "",contraAcc?: "")
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Dashboard"
        // Relacionando los elementos con su objeto de la interfaz
        btnEstas = findViewById(R.id.btnStats)
        btnGenRep = findViewById(R.id.btnGenRep)
        btnEdiPerf = findViewById(R.id.btnPerfUs)
        btnMenAj = findViewById(R.id.btnAjuste)
        btnManual = findViewById(R.id.btnManUs)
        btnManSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)
    }

    private fun addListeners(){
        // Agregar los listener
        btnEstas.setOnClickListener {
            val statsActi = Intent(applicationContext,StationsActivity::class.java)
            startActivity(statsActi)
        }

        btnCerSes.setOnClickListener {
            // Cerrar Sesion en Firebase
            FirebaseAuth.getInstance().signOut()
            // Lanzar la app hacia la primera ventana
            val endActi = Intent(this,MainActivity::class.java)
            startActivity(endActi)
            finish()
        }
    }

    private fun btnGestSis(correo: String, contra: String){
        // Creando la referencia de la coleccion de preguntas en la BD
        val refDB = Firebase.database.getReference("Usuarios")
        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val contra: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    Log.i("FirebaseJSON", resUser.toString())
                    if(resUser.correo == correo && resUser.contra == contra){
                        if(resUser.tipo_Usuario == "Administrador"){
                            btnManSis.isGone = false
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }
}