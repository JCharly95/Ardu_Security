package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isGone
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class UserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblNom: TextView
    private lateinit var lblCor: TextView
    private lateinit var btnEditNom: Button
    private lateinit var btnEditEma: Button
    private lateinit var btnEditPass: Button
    private lateinit var btnEditPreg: Button
    private lateinit var btnEditResp: Button
    private lateinit var btnEditSis: Button
    private lateinit var btnEditPin: Button
    private lateinit var btnEditTel: Button
    // Creando el objeto GSON
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

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
        btnEditSis = findViewById(R.id.btnEditSisRel)
        btnEditPin = findViewById(R.id.btnEditPin)
        btnEditTel = findViewById(R.id.btnEditTele)

        // Obtener el correo del usuario desde Firebase auth y enviarlo a la funcion de la vista del boton
        val corrAcc = getEmail()
        rellDatPerf(corrAcc)
    }

    private fun getEmail(): String {
        val user = Firebase.auth.currentUser
        var email = ""
        user?.let {task ->
            email = task.email.toString()
        }
        return email
    }

    private fun rellDatPerf(correo: String){
        val ref = Firebase.database.getReference("Usuarios")
        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        lblNom.text = "Nombre: "+ resUser.nombre
                        lblCor.text = "Correo: "+ resUser.correo
                        if(resUser.tipo_Usuario == "Administrador"){
                            btnEditTel.isGone = false
                        }
                        break
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun addListeners(){
        // Toda la edicion de campos se lanzara hacia la misma actividad,
        // solo que dependera del campo a editar, los valores que seran mostrados
        btnEditNom.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Nombre")
                startActivity(this)
            }
        }
        btnEditEma.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Correo")
                startActivity(this)
            }
        }
        btnEditPass.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Contraseña")
                startActivity(this)
            }
        }
        btnEditPreg.setOnClickListener {
            Intent(this, EditDataUsSpActivity::class.java).apply {
                putExtra("campo", "Pregunta")
                startActivity(this)
            }
        }
        btnEditResp.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Respuesta")
                startActivity(this)
            }
        }
        btnEditSis.setOnClickListener {
            Intent(this, EditDataUsSpActivity::class.java).apply {
                putExtra("campo", "Sistema")
                startActivity(this)
            }
        }
        btnEditPin.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Pin")
                startActivity(this)
            }
        }
        btnEditTel.setOnClickListener {
            Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Telefono")
                startActivity(this)
            }
        }
    }
}