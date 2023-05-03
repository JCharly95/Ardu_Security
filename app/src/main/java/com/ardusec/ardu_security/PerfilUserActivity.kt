package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import com.google.firebase.database.*
import com.google.gson.Gson

class PerfilUserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblNom: TextView
    private lateinit var lblCor: TextView
    private lateinit var btnEditNom: Button
    private lateinit var btnEditEma: Button
    private lateinit var btnEditPass: Button
    private lateinit var btnEditPreg: Button
    private lateinit var btnEditResp: Button
    private lateinit var btnEditPin: Button
    private lateinit var btnEditTel: Button
    private lateinit var bundle: Bundle
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Creando el objeto GSON
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_user)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Editar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblNom = findViewById(R.id.lblNombre)
        lblCor = findViewById(R.id.lblCorreo)
        btnEditNom = findViewById(R.id.btnEditNam)
        btnEditEma = findViewById(R.id.btnEditCorr)
        btnEditPass = findViewById(R.id.btnEditContra)
        btnEditPreg = findViewById(R.id.btnEditPreg)
        btnEditResp = findViewById(R.id.btnEditResp)
        btnEditPin = findViewById(R.id.btnEditPin)
        btnEditTel = findViewById(R.id.btnEditTele)
        // Crear un bundle para los extras
        bundle = intent.extras!!
        // Saber si el usuario vera el boton de gestion o no
        val corrAcc = bundle.getString("correo")
        // Kotlin se protege en caso de que no haya extras por eso se necesita establecer el ?
        rellDatPerf(corrAcc?: "")
    }

    private fun rellDatPerf(correo: String){
        ref = database.getReference("Usuarios")
        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        lblNom.text = resUser.nombre
                        lblCor.text = resUser.correo
                        if(resUser.tipo_Usuario == "Administrador"){
                            btnEditTel.isGone = false
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun addListeners(){
        btnEditNom.setOnClickListener {

        }
        btnEditEma.setOnClickListener{

        }
        btnEditPass.setOnClickListener {

        }
        btnEditPreg.setOnClickListener {

        }
        btnEditResp.setOnClickListener {

        }
        btnEditPin.setOnClickListener {

        }
        btnEditTel.setOnClickListener {

        }
    }
}