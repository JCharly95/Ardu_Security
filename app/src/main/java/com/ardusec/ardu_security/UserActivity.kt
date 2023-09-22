package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class UserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblNom: TextView
    private lateinit var lblUser: TextView
    private lateinit var btnEditNom: Button
    private lateinit var btnEditEma: Button
    private lateinit var btnEditPass: Button
    private lateinit var btnEditPreg: Button
    private lateinit var btnEditResp: Button
    private lateinit var btnEditSis: Button
    private lateinit var btnEditPin: Button
    private lateinit var btnEditTel: Button
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo el usuario
        if(intent.extras == null){
            Toast.makeText(this@UserActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
        }
        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Editar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblNom = findViewById(R.id.lblNomVal)
        lblUser = findViewById(R.id.lblUserVal)
        btnEditNom = findViewById(R.id.btnEditNam)
        btnEditEma = findViewById(R.id.btnEditCorr)
        btnEditPass = findViewById(R.id.btnEditContra)
        btnEditPreg = findViewById(R.id.btnEditPreg)
        btnEditResp = findViewById(R.id.btnEditResp)
        btnEditSis = findViewById(R.id.btnEditSisRel)
        btnEditPin = findViewById(R.id.btnEditPin)
        btnEditTel = findViewById(R.id.btnEditTele)
        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Establecer los valores a mostrar en la pantalla con respecto al usuario
        setFormulario()
    }
    private fun setFormulario(){
        val userAuth = auth.currentUser
        userAuth?.let {
            lblNom.text = userAuth.displayName
            lblUser.text = user
        }
    }

    private fun addListeners(){
        // Toda la edicion de campos se lanzara hacia la misma actividad,
        // solo que dependera del campo a editar, los valores que seran mostrados
        btnEditNom.setOnClickListener {
            val editNom = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Nombre")
            }
            startActivity(editNom)
        }
        btnEditEma.setOnClickListener {
            val editEma = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Correo")
            }
            startActivity(editEma)
        }
        btnEditPass.setOnClickListener {
            val editPass = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Contraseña")
            }
            startActivity(editPass)
        }
        btnEditPreg.setOnClickListener {
            val editPreg = Intent(this, EditDataUsSpActivity::class.java).apply {
                putExtra("campo", "Pregunta")
            }
            startActivity(editPreg)
        }
        btnEditResp.setOnClickListener {
            val editResp = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Respuesta")
            }
            startActivity(editResp)
        }
        btnEditSis.setOnClickListener {
            val editSis = Intent(this, EditDataUsSpActivity::class.java).apply {
                putExtra("campo", "Sistema")
            }
            startActivity(editSis)
        }
        btnEditPin.setOnClickListener {
            val editPin = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Pin")
            }
            startActivity(editPin)
        }
        btnEditTel.setOnClickListener {
            val editTel = Intent(this, EditDataUsTxtActivity::class.java).apply {
                putExtra("campo", "Telefono")
            }
            startActivity(editTel)
        }
    }
}