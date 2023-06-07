package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class EditDataUsSpActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSp: TextView
    private lateinit var btnAyuda: Button
    private lateinit var txtValVie: EditText
    private lateinit var spNPreg: Spinner
    private lateinit var spNSis: Spinner
    private lateinit var btnConfCamb: Button
    // Creando el objeto GSON
    private var gson = Gson()
    // Variable del correo para la busqueda del usuario en firebase auth
    private lateinit var email: String
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var campo: String
    // Dataclases
    data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
    data class Sistema(val id_Sistema: String, val nombre_Sis: String, val tipo: String, val ulti_Cam_Nom: String)
    data class UserSistem(val id_User_Sis: String, val sistema_Nom: String, val user_Email: String)
    data class Pregunta(val ID_Pregunta: String, val Val_Pregunta: String)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_us_sp)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Actualizar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblHeadSp = findViewById(R.id.lblHeadEditSp)
        btnAyuda = findViewById(R.id.btnInfoActuSp)
        txtValVie = findViewById(R.id.txtOldEditDatSp)
        spNPreg = findViewById(R.id.spNewPregKey)
        spNSis = findViewById(R.id.spNewSis)
        btnConfCamb = findViewById(R.id.btnConfEditDataSp)
        // Estableciendo la variable de correo
        email = ""
        // Obteniendo el extra enviado para saber que campo actualizar
        bundle = intent.extras!!
        campo = bundle.getString("campo").toString()
        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun rellSpinPregs(){
        // Obtener el arreglo de strings establecido para las preguntas
        val lstPregs = resources.getStringArray(R.array.lstSavQues)
        var arrPregs = ArrayList<String>()
        val database = Firebase.database
        arrPregs.addAll(lstPregs)
        // Creando la referencia de la coleccion de preguntas en la BD
        val ref = database.getReference("Pregunta")
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objPreg in dataSnapshot.children){
                    val pregJSON = gson.toJson(objPreg.value)
                    val resPreg = gson.fromJson(pregJSON, Pregunta::class.java)
                    arrPregs.add(resPreg.Val_Pregunta)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapPregs = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrPregs)
                adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spNPreg.adapter = adapPregs
            }
            override fun onCancelled(databaseError: DatabaseError){
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun rellSpinSis(){
        // Obtener el arreglo de strings establecido para los sistemas
        val lstSists = resources.getStringArray(R.array.lstSistems)
        var arrSists = ArrayList<String>()
        val database = Firebase.database
        arrSists.addAll(lstSists)
        // Creando la referencia de la coleccion de preguntas en la BD
        val ref = database.getReference("Sistema")
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objSis in dataSnapshot.children){
                    val sisJSON = gson.toJson(objSis.value)
                    val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                    arrSists.add(resSis.nombre_Sis)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapSis = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrSists)
                adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spNSis.adapter = adapSis
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun avisoActu(){
        val mensaje = "Consideraciones de campos: \n\n" +
                "Nombre;\n" +
                "* Su nombre no debe tener numeros\n" +
                "* Su nombre debe tener al menos 10 caracteres\n\n" +
                "Correo; Formato Aceptado:\n" +
                "* usuario@dominio.com(.mx)\n\n" +
                "Contraseña:\n" +
                "* Extension minima de 8 caracteres\n" +
                "* Por lo menos una mayuscula\n" +
                "* Por lo menos un numero\n" +
                "* Por lo menos  un caracter especial\n\n" +
                "Administradores; Numero Telefonico:\n" +
                "* Solo se permiten numeros\n" +
                "* Lada + Numero ó Tel. Celular\n\n" +
                "** NOTA: Para el cambio de correo o contraseña, se le " +
                "solicitara la contraseña como confirmacion de cambio."
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setFormulario(){
        // Establecer el encabezado y el boton, acorde al campo a actualizar
        lblHeadSp.text = lblHeadSp.text.toString()+"\n"+campo
        btnConfCamb.text = btnConfCamb.text.toString()+"\n"+campo
        lifecycleScope.launch(Dispatchers.IO) {
            val getUs = async {
                val user = Firebase.auth.currentUser!!
                val database = Firebase.database
                var ref = database.reference
                // Extraccion del correo del usuario desde Firebase Auth
                user.let { task ->
                    email = task.email.toString()
                    // Creando la referencia de la coleccion de usuarios en la BD
                    val refDB = ref.child("Usuarios")
                    refDB.addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot){
                            for (objUs in dataSnapshot.children){
                                val userJSON = gson.toJson(objUs.value)
                                val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                if(resUser.correo == email){
                                    when (campo){
                                        "Pregunta" -> {
                                            // Si es el sistema o las preguntas a actualizar, se ocultara el campo de texto y se mostrara el Spinner
                                            txtValVie.setText(resUser.preg_Seguri)
                                            spNPreg.isGone = false
                                            rellSpinPregs()
                                            break
                                        }
                                        "Sistema" -> {
                                            spNSis.isGone = false
                                            rellSpinSis()
                                            ref = database.getReference("User_Sistems")
                                            ref.addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(dataSnapshot: DataSnapshot){
                                                    for (objSisUs in dataSnapshot.children){
                                                        val sisUSJSON = gson.toJson(objSisUs.value)
                                                        val resSisUs = gson.fromJson(sisUSJSON, UserSistem::class.java)
                                                        if(resSisUs.user_Email == email) {
                                                            txtValVie.setText(resSisUs.sistema_Nom)
                                                            break
                                                        }
                                                    }
                                                }
                                                override fun onCancelled(databaseError: DatabaseError){
                                                    Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                                                }
                                            })
                                            break
                                        }else -> {
                                            Toast.makeText(applicationContext, "Error: El campo seleccionado no fue encontrado",Toast.LENGTH_SHORT).show()
                                            break
                                        }
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
            getUs.await()
        }
    }
    private fun addListeners(){
        btnAyuda.setOnClickListener {
            avisoActu()
        }
        btnConfCamb.setOnClickListener{
            lifecycleScope.launch(Dispatchers.Default) {
                val confChg = async {
                    val user = Firebase.auth.currentUser!!
                    // Extraccion del correo del usuario desde Firebase Auth
                    user.let { task ->
                        email = task.email.toString()
                        if (campo == "Pregunta") {
                            if(validarSelPreg(spNPreg)) {
                                actPreg(spNPreg, email, gson)
                            }
                        }
                        else if (campo == "Sistema") {
                            if(validarSelSis(spNSis)) {
                                actSis(spNSis, email, gson)
                            }
                        }else {
                            Toast.makeText(applicationContext, "Error: El campo seleccionado no fue encontrado",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                confChg.await()
            }
        }
    }

    // Validaciones de campos
    suspend fun validarSelPreg(lista: Spinner): Boolean {
        if(lista.selectedItemPosition == 0){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "Error: Favor de seleccionar una pregunta", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }
    suspend fun validarSelSis(lista: Spinner): Boolean {
        if (lista.selectedItemPosition == 0) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    "Error: Favor de seleccionar un sistema",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
        return true
    }

    //Actualizacion de campos
    suspend fun actPreg(listPregs: Spinner, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.Default){
            val updPregunta = async {
                Log.w("Nueva Pregunta Seleccionada", listPregs.selectedItem.toString())
                val refDB = Firebase.database.reference.child("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                refDB.child(resUser.id_Usuario).child("preg_Seguri").setValue(listPregs.selectedItem.toString().trim()).addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        Toast.makeText(applicationContext, "Su pregunta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                        Timer().schedule(2000){
                                            val endEdit = Intent(applicationContext, UserActivity::class.java)
                                            startActivity(endEdit)
                                            finish()
                                        }
                                    }else{
                                        Toast.makeText(applicationContext, "Error: Su pregunta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                        Log.w("UpdateFirebaseError:", task.exception.toString())
                                    }
                                }
                            }
                            break
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Nombre", databaseError.toException())
                    }
                })
            }
            updPregunta.await()
        }
    }
    suspend fun actSis(listSis: Spinner, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.Default) {
            val updSistema = async {
                Log.w("Nuevo Sistema Seleccionado", listSis.selectedItem.toString())
                // Creando la referencia de la coleccion de usuarios_sistemas en la BD
                val refDB = Firebase.database.reference.child("User_Sistems")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSisUs in dataSnapshot.children){
                            val sisUsJSON = gson.toJson(objSisUs.value)
                            val resSisUs = gson.fromJson(sisUsJSON, UserSistem::class.java)
                            if(resSisUs.user_Email == correo){
                                refDB.child(resSisUs.id_User_Sis).child("sistema_Nom").setValue(listSis.selectedItem.toString().trim()).addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        Toast.makeText(applicationContext, "Su sistema fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                        Timer().schedule(2000){
                                            val endEdit = Intent(applicationContext, UserActivity::class.java)
                                            startActivity(endEdit)
                                            finish()
                                        }
                                    }else{
                                        Toast.makeText(applicationContext, "Error: Su sistema no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                        Log.w("UpdateFirebaseError:", task.exception.toString())
                                    }
                                }
                            }
                            break
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Sistema", databaseError.toException())
                    }
                })
            }
            updSistema.await()
        }
    }
}