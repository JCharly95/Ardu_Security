package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class EditDataUsSpActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSp: TextView
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtValVie: TextView
    private lateinit var spNPreg: AppCompatSpinner
    private lateinit var spNSis: AppCompatSpinner
    private lateinit var btnConfCamb: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Creando el objeto GSON
    private var gson = Gson()
    // Variable del correo para la busqueda del usuario en firebase auth
    private lateinit var email: String
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var campo: String
    // Dataclases
    data class Usuario(val id_Usuario: String? = null, val nombre: String? = null, val correo: String? = null, val tipo_Usuario: String? = null, val num_Tel: Long? = null, val pregunta_Seg: String? = null, val respuesta_Seg: String? = null, val pin_Pass: Int? = null)
    data class Sistema(val id_Sistema: String? = null, val nombre_Sis: String? = null, val tipo: String? = null, val ulti_Cam_Nom: String? = null)
    data class UserSistem(val id_User_Sis: String? = null, val sistema_Nom: String? = null, val user_Email: String? = null)
    data class Pregunta(val ID_Pregunta: String? = null, val Val_Pregunta: String? = null)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_us_sp)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

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
        txtValVie = findViewById(R.id.txtValVieSp)
        spNPreg = findViewById(R.id.spUpdPregSegu)
        spNSis = findViewById(R.id.spUpdSis)
        btnConfCamb = findViewById(R.id.btnConfChgSp)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Estableciendo la variable de correo
        email = ""
        // Obteniendo el extra enviado para saber que campo actualizar
        bundle = intent.extras!!
        campo = bundle.getString("campo").toString()
        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun rellSpinPregs(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellPregs = async {
                // Obtener el arreglo de strings establecido para las preguntas
                val lstPregs = resources.getStringArray(R.array.lstSavQues)
                var arrPregs = ArrayList<String>()
                arrPregs.addAll(lstPregs)
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                ref.get().addOnSuccessListener{ taskGet ->
                    for (objPreg in taskGet.children){
                        objPreg.ref.child("Val_Pregunta").get().addOnSuccessListener { taskAdd ->
                            arrPregs.add(taskAdd.value.toString())
                        }
                    }
                    // Estableciendo el adaptador para el rellenado del spinner
                    val adapPregs = ArrayAdapter(this@EditDataUsSpActivity, android.R.layout.simple_spinner_item, arrPregs)
                    adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spNPreg.adapter = adapPregs
                }
                    .addOnFailureListener {
                        Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
            }
            rellPregs.await()
        }
    }

    private fun rellSpinSis(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                var arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children){
                            objSis.ref.child("nombre_Sis").get().addOnSuccessListener { taskGet ->
                                arrSists.add(taskGet.value.toString())
                            }
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spNSis.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellSis.await()
        }
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

    private fun retorno(){
        return this.onBackPressedDispatcher.onBackPressed()
    }

    private fun setFormulario(){
        // Establecer el encabezado y el boton, acorde al campo a actualizar
        lblHeadSp.text = lblHeadSp.text.toString()+"\n"+campo
        btnConfCamb.text = btnConfCamb.text.toString()+"\n"+campo
        lifecycleScope.launch(Dispatchers.IO) {
            val getUs = async {
                val user = Firebase.auth.currentUser!!
                val database = Firebase.database
                when (campo){
                    "Pregunta" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setPreg = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    spNPreg.isGone = false
                                    rellSpinPregs()
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                                if(resUser.correo == email){
                                                    // Si es el sistema o las preguntas a actualizar, se ocultara el campo de texto y se mostrara el Spinner
                                                    txtValVie.setText(resUser.pregunta_Seg)
                                                    break
                                                }
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                                        }
                                    })
                                }
                            }
                            setPreg.await()
                        }
                    }
                    "Sistema" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setSis = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    spNSis.isGone = false
                                    rellSpinSis()
                                    // Creando la referencia de la coleccion de usuarios/sistemas en la BD
                                    val refDB = database.getReference("User_Sistems")
                                    refDB.addValueEventListener(object: ValueEventListener{
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
                                }
                            }
                            setSis.await()
                        }
                    }
                    else -> {
                        Toast.makeText(this@EditDataUsSpActivity, "Error: El campo solicitado no esta disponible", Toast.LENGTH_SHORT).show()
                    }
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
                    when (campo) {
                        "Pregunta" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarSelPreg(spNPreg)) {
                                    actPreg(spNPreg, email, gson)
                                }
                            }
                        }
                        "Sistema" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarSelSis(spNSis)) {
                                    actSis(spNSis, email, gson)
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this@EditDataUsSpActivity, "Error: El campo seleccionado no fue encontrado",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                confChg.await()
            }
        }
    }

    // Validaciones de campos
    private fun validarSelPreg(lista: Spinner): Boolean {
        return if(lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su pregunta clave"){
            true
        }else{
            Toast.makeText(this@EditDataUsSpActivity, "Error: Favor de seleccionar una pregunta", Toast.LENGTH_SHORT).show()
            false
        }
    }
    private fun validarSelSis(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su Sistema") {
            true
        }else{
            Toast.makeText(this@EditDataUsSpActivity,"Error: Favor de seleccionar un sistema",Toast.LENGTH_SHORT).show()
            false
        }
    }

    //Actualizacion de campos
    private fun actPreg(listPregs: Spinner, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.Default){
            val updPregunta = async {
                val database = Firebase.database
                val refDB = database.getReference("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                val upPreg = refDB.child(resUser.id_Usuario!!).child("pregunta_Seg").setValue(listPregs.selectedItem.toString().trim())
                                upPreg.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Su pregunta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upPreg.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Error: Su pregunta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
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
    private fun actSis(listSis: Spinner, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.Default) {
            val updSistema = async {
                val database = Firebase.database
                val refDB = database.getReference("User_Sistems")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSisUs in dataSnapshot.children){
                            val sisUsJSON = gson.toJson(objSisUs.value)
                            val resSisUs = gson.fromJson(sisUsJSON, UserSistem::class.java)
                            if(resSisUs.user_Email == correo){
                                val upSis = refDB.child(resSisUs.id_User_Sis!!).child("sistema_Nom").setValue(listSis.selectedItem.toString().trim())
                                upSis.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Su sistema fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upSis.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Error: Su sistema no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
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