package com.ardusec.ardu_security.admin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.EditDataSpActivity
import com.ardusec.ardu_security.R
import com.ardusec.ardu_security.user.DashboardActivity
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

class ManageSisUsersActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var spUsuarios: AppCompatSpinner
    private lateinit var txtNombre: EditText
    private lateinit var txtUsername: EditText
    private lateinit var txtTipo: EditText
    private lateinit var btnEditTipUser: ImageButton
    private lateinit var btnDelUserSis: ImageButton
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Bandera de activacion para el spínner
    private var bandeListen = false
    private var selUserKey = ""
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_sis_users)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))

        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            Toast.makeText(this@ManageSisUsersActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
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
        title = "Menu Usuarios del Sistema"
        // Relacionando los elementos con su objeto de la interfaz
        spUsuarios = findViewById(R.id.spSistemUsers)
        txtNombre = findViewById(R.id.txtAdminNomSel)
        txtUsername = findViewById(R.id.txtAdminUserSel)
        txtTipo = findViewById(R.id.txtAdminTipoSel)
        btnEditTipUser = findViewById(R.id.btnEditTipUser)
        btnDelUserSis = findViewById(R.id.btnDelSisUs)
        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Rellenar el spinner de los usuarios
        rellUsuarios()
    }

    private fun addListeners(){
        //Agregar los listener
        spUsuarios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!bandeListen) {
                    bandeListen = true
                    return
                }
                // Estableciendo el usuario a gestionar y la informacion a mostrar
                selUserKey = parent!!.getItemAtPosition(position).toString()
                setInfo(selUserKey)
            }
        }
        btnEditTipUser.setOnClickListener {
            val editTipo = Intent(this@ManageSisUsersActivity, EditDataSpActivity::class.java).apply {
                putExtra("usuario", selUserKey)
                putExtra("sistema", sistema)
                putExtra("campo", "Tipo")
            }
            startActivity(editTipo)
        }
        btnDelUserSis.setOnClickListener {
            delUsSistem(selUserKey, sistema)
        }
    }

    private fun rellUsuarios() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rellUs = async {
                val lstUsers = resources.getStringArray(R.array.lstUsersSis)
                val arrUsers = ArrayList<String>()
                arrUsers.addAll(lstUsers)
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children) {
                            val usRef = objSis.ref.child("usuarios")
                            usRef.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(objUs in snapshot.children){
                                        arrUsers.add(objUs.key.toString())
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapUs = ArrayAdapter(this@ManageSisUsersActivity, android.R.layout.simple_spinner_item, arrUsers)
                        adapUs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spUsuarios.adapter = adapUs
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellUs.await()
        }
    }

    private fun avisoManSis(mensaje: String) {
        val aviso = AlertDialog.Builder(this@ManageSisUsersActivity)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setInfo(username: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val setInfo = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if (objUser.key.toString() == username){
                                txtNombre.setText(objUser.child("nombre").value.toString())
                                txtUsername.setText(objUser.child("username").value.toString())
                                txtTipo.setText(objUser.child("tipo_Usuario").value.toString())
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setInfo.await()
        }
    }

    private fun delUsSistem(userKey: String, sisKey: String){
        lifecycleScope.launch(Dispatchers.IO){
            val delSisUs = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == userKey){
                                val refSisUs = objUser.ref.child("sistemas")
                                refSisUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objSis in snapshot.children){
                                            if(objSis.key.toString() == sisKey){
                                                objSis.ref.removeValue()
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delSisUs.await()
            val delUsSis = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children){
                            if(objSis.key.toString() == sisKey){
                                val refUsSis = objSis.ref.child("usuarios")
                                refUsSis.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objUser in snapshot.children){
                                            if(objUser.key.toString() == userKey){
                                                objUser.ref.removeValue().addOnSuccessListener {
                                                        lifecycleScope.launch(Dispatchers.Main){
                                                            val msg = "El usuario $userKey fue eliminado del sistema ${objSis.child("nombre_Sis").value} \n\n" +
                                                                    "Favor de notificar al usuario por medios externos a la aplicacion en caso de que no tenga otro sistema relacionado"
                                                            avisoManSis(msg)
                                                        }
                                                        Timer().schedule(1500){
                                                            lifecycleScope.launch(Dispatchers.Main){
                                                                Intent(this@ManageSisUsersActivity, DashboardActivity::class.java).apply {
                                                                    putExtra("username", userKey)
                                                                    putExtra("tipo", txtTipo.text.toString())
                                                                    startActivity(this)
                                                                    finish()
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(this@ManageSisUsersActivity,"Error: No se pudo remover al usuario del sistema",Toast.LENGTH_SHORT).show()
                                                    }
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ManageSisUsersActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delUsSis.await()
        }
    }
}