package com.ardusec.ardu_security.user

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.EditDataSpActivity
import com.ardusec.ardu_security.EditDataTxtActivity
import com.ardusec.ardu_security.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblNom: TextView
    private lateinit var lblUser: TextView
    private lateinit var btnEditNom: ImageButton
    private lateinit var btnEditEma: ImageButton
    private lateinit var btnEditPass: ImageButton
    private lateinit var btnEditPreg: ImageButton
    private lateinit var btnEditResp: ImageButton
    private lateinit var btnEditSis: ImageButton
    private lateinit var btnEditUsNom: ImageButton
    private lateinit var linLayTel: LinearLayout
    private lateinit var btnEditTel: ImageButton
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))
        //Obteniendo el usuario
        if(intent.extras == null){
            Toast.makeText(this@UserActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
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
        btnEditUsNom = findViewById(R.id.btnEditUsNam)
        linLayTel = findViewById(R.id.linLayEditTel)
        btnEditTel = findViewById(R.id.btnEditTel)
        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Establecer los valores a mostrar en la pantalla con respecto al usuario
        setFormulario()
    }
    private fun setFormulario() {
        lifecycleScope.launch(Dispatchers.IO){
            val getVals = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUs in dataSnapshot.children){
                            if(objUs.key.toString() == user){
                                lblNom.text = objUs.child("nombre").value.toString()
                                lblUser.text = objUs.child("username").value.toString()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity, "Error: Datos no obtenidos", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getVals.await()

            val getUsTipo = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children) {
                            if(objSis.key.toString() == sistema){
                                val usRef = objSis.ref.child("usuarios")
                                usRef.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objUs in snapshot.children){
                                            if(objUs.key.toString() == user && objUs.value.toString() == "Administrador"){
                                                linLayTel.isGone = false
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getUsTipo.await()
        }
    }

    private fun addListeners(){
        // Toda la edicion de campos se lanzara hacia la misma actividad,
        // solo que dependera del campo a editar, los valores que seran mostrados
        btnEditNom.setOnClickListener {
            val editNom = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Nombre")
            }
            startActivity(editNom)
        }
        btnEditEma.setOnClickListener {
            val editEma = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Correo")
            }
            startActivity(editEma)
        }
        btnEditPass.setOnClickListener {
            val editPass = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Contraseña")
            }
            startActivity(editPass)
        }
        btnEditPreg.setOnClickListener {
            val editPreg = Intent(this@UserActivity, EditDataSpActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Pregunta")
            }
            startActivity(editPreg)
        }
        btnEditResp.setOnClickListener {
            val editResp = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Respuesta")
            }
            startActivity(editResp)
        }
        btnEditSis.setOnClickListener {
            val editSis = Intent(this@UserActivity, EditDataSpActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Sistema")
            }
            startActivity(editSis)
        }
        btnEditUsNom.setOnClickListener {
            val editUser = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Username")
            }
            startActivity(editUser)
        }
        btnEditTel.setOnClickListener {
            val editTel = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("campo", "Telefono")
            }
            startActivity(editTel)
        }
    }
}