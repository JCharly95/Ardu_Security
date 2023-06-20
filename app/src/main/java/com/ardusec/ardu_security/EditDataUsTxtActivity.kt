package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class EditDataUsTxtActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSec: TextView
    private lateinit var btnAyuda: Button
    private lateinit var txtValVie: EditText
    private lateinit var txtValNue: EditText
    private lateinit var txtValNueNum: EditText
    private lateinit var txtValNuePas: EditText
    private lateinit var lblConfChg: TextView
    private lateinit var txtConfChg: EditText
    private lateinit var chbConfChg: CheckBox
    private lateinit var btnConfCamb: Button
    // Creando el objeto GSON
    private var gson = Gson()
    // Variable del correo para la busqueda del usuario en firebase auth
    private lateinit var email: String
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var campo: String
    // Dataclases
    data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val pregunta_Seg: String, val respuesta_Seg: String, val pin_Pass: Int)
    data class UserSistem(val id_User_Sis: String, val sistema_Nom: String, val user_Email: String)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_us_txt)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Actualizar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblHeadSec = findViewById(R.id.lblHeadEditTxt)
        btnAyuda = findViewById(R.id.btnInfoActuTxt)
        txtValVie = findViewById(R.id.txtOldEditDatTxt)
        txtValNue = findViewById(R.id.txtNewEditDat)
        txtValNueNum = findViewById(R.id.txtNewEditDatNum)
        txtValNuePas = findViewById(R.id.txtNewEditDatPas)
        lblConfChg = findViewById(R.id.lblConfChg)
        txtConfChg = findViewById(R.id.txtConfChg)
        chbConfChg = findViewById(R.id.chbConfPass)
        btnConfCamb = findViewById(R.id.btnConfEditDataTxt)
        // Estableciendo la variable de correo
        email = ""
        // Obteniendo el extra enviado para saber que campo actualizar
        bundle = intent.extras!!
        campo = bundle.getString("campo").toString()
        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
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
        lblHeadSec.text = lblHeadSec.text.toString()+"\n"+campo
        btnConfCamb.text = btnConfCamb.text.toString()+"\n"+campo
        lifecycleScope.launch(Dispatchers.IO) {
            val getUs = async {
                val user = Firebase.auth.currentUser!!
                val database = Firebase.database
                when (campo){
                    "Nombre" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setNom = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    txtValVie.isGone = false
                                    txtValNue.isGone = false
                                    btnConfCamb.isGone = false
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, EditDataUsSpActivity.Usuario::class.java)
                                                if(resUser.correo == email){
                                                    txtValVie.setText(resUser.nombre)
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
                            setNom.await()
                        }
                    }
                    "Correo" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setCor = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    lblConfChg.isGone = false
                                    txtConfChg.isGone = false
                                    chbConfChg.isGone = false
                                    txtValVie.isGone = false
                                    txtValNue.isGone = false
                                    btnConfCamb.isGone = false
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, EditDataUsSpActivity.Usuario::class.java)
                                                if(resUser.correo == email){
                                                    txtValVie.setText(resUser.correo)
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
                            setCor.await()
                        }
                    }
                    "Contraseña" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setCon = async {
                                lblConfChg.isGone = false
                                txtConfChg.isGone = false
                                chbConfChg.isGone = false
                                txtValVie.isGone = false
                                txtValNuePas.isGone = false
                                btnConfCamb.isGone = false
                                txtValVie.setText("Por seguridad, no es posible mostrar la contraseña previa")
                                chbConfChg.text = "Mostrar Valores"
                            }
                            setCon.await()
                        }
                    }
                    "Respuesta" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setResp = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    txtValVie.isGone = false
                                    txtValNue.isGone = false
                                    btnConfCamb.isGone = false
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, EditDataUsSpActivity.Usuario::class.java)
                                                if(resUser.correo == email){
                                                    txtValVie.setText(resUser.respuesta_Seg)
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
                            setResp.await()
                        }
                    }
                    "Pin" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setPin = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    txtValVie.isGone = false
                                    txtValNueNum.isGone = false
                                    btnConfCamb.isGone = false
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, EditDataUsSpActivity.Usuario::class.java)
                                                if(resUser.correo == email){
                                                    txtValVie.setText(resUser.pin_Pass.toString())
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
                            setPin.await()
                        }
                    }
                    "Telefono" -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val setTel = async {
                                user.let { task ->
                                    email = task.email.toString()
                                    txtValVie.isGone = false
                                    txtValNueNum.isGone = false
                                    btnConfCamb.isGone = false
                                    // Creando la referencia de la coleccion de usuarios en la BD
                                    val refDB = database.getReference("Usuarios")
                                    refDB.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objUs in dataSnapshot.children){
                                                val userJSON = gson.toJson(objUs.value)
                                                val resUser = gson.fromJson(userJSON, EditDataUsSpActivity.Usuario::class.java)
                                                if(resUser.correo == email){
                                                    txtValVie.setText(resUser.num_Tel.toString())
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
                            setTel.await()
                        }
                    }
                    else -> {
                        Toast.makeText(this@EditDataUsTxtActivity, "Error: El campo solicitado no esta disponible", Toast.LENGTH_SHORT).show()
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
        chbConfChg.setOnClickListener {
            if(!chbConfChg.isChecked){
                txtConfChg.transformationMethod = PasswordTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtConfChg.transformationMethod = HideReturnsTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnConfCamb.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val confChg = async {
                    val user = Firebase.auth.currentUser!!
                    // Extraccion del correo del usuario desde Firebase Auth
                    when (campo){
                        "Nombre" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarNombre(txtValNue.text)) {
                                    actNombre(txtValNue.text.toString(), email, gson)
                                }
                            }
                        }
                        "Respuesta" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarResp(txtValNue.text)) {
                                    actResp(txtValNue.text.toString(), email, gson)
                                }
                            }
                        }
                        "Pin" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarPin(txtValNueNum.text)) {
                                    actPin(txtValNueNum.text.toString(), email, gson)
                                }
                            }
                        }
                        "Telefono" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarTel(txtValNueNum.text)) {
                                    actTel(txtValNueNum.text.toString(), email, gson)
                                }
                            }
                        }
                        "Correo" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarCorreo(txtValNue.text) && validarContra(txtConfChg.text)) {
                                    actCorreo(txtValNue.text.toString(), email, txtConfChg.text.toString(), gson)
                                }
                            }
                        }
                        "Contraseña" -> {
                            user.let { task ->
                                email = task.email.toString()
                                if(validarContra(txtValNuePas.text) && validarContra(txtConfChg.text)) {
                                    actContra(txtValNuePas.text.toString(), email, txtConfChg.text.toString())
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this@EditDataUsTxtActivity, "Error: El campo solicitado no esta disponible", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                confChg.await()
            }
        }
    }

    // Validaciones de campos
    private fun validarNombre(nombre: Editable): Boolean{
        when{
            // Si el nombre esta vacio
            TextUtils.isEmpty(nombre) -> {
                Toast.makeText(this, "Error: Favor de introducir un nombre", Toast.LENGTH_SHORT).show()
            }
            // Si se encuentra algun numero
            (Regex("""\d+""").containsMatchIn(nombre)) -> {
                Toast.makeText(this, "Error: Su nombre no puede contener numeros", Toast.LENGTH_SHORT).show()
            }
            // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
            (nombre.length < 10) -> {
                Toast.makeText(this, "Error: Su nombre es muy corto, favor de agregar su nombre completo", Toast.LENGTH_SHORT).show()
            }
            // Si se encuentran caracteres especiales
            (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> {
                Toast.makeText(this, "Error: Su nombre no puede contener caracteres especiales", Toast.LENGTH_SHORT).show()
            }
            else -> {
                return true
            }
        }
        return false
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        if(Regex("""\s+""").containsMatchIn(correo)){
            val correoFil = correo.replace("\\s".toRegex(), "")
            when{
                // Si el correo esta vacio
                TextUtils.isEmpty(correoFil) -> {
                    Toast.makeText(this, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                }
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil).matches() -> {
                    Toast.makeText(this, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    return true
                }
            }
        }else{
            when{
                // Si el correo esta vacio
                TextUtils.isEmpty(correo) -> {
                    Toast.makeText(this, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                }
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                    Toast.makeText(this, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    return true
                }
            }
        }
        return false
    }
    private fun validarContra(contra: Editable): Boolean{
        // Si se detectan espacios en la contraseña, estos seran removidos
        if(Regex("""\s+""").containsMatchIn(contra)) {
            val contraFil = contra.replace("\\s".toRegex(), "")
            when {
                // Si la contraseña esta vacia
                TextUtils.isEmpty(contraFil) -> {
                    Toast.makeText(this, "Error: Favor de introducir una contraseña", Toast.LENGTH_SHORT).show()
                }
                // Extension minima de 8 caracteres
                (contraFil.length < 8) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener una extension minima de 8 caracteres", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos una mayuscula
                (!Regex("[A-Z]+").containsMatchIn(contraFil)) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos un numero
                (!Regex("""\d""").containsMatchIn(contraFil)) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener al menos un numero", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos un caracter especial
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil)) -> {
                    Toast.makeText(this, "Error: Favor de incluir al menos un caracter especial en su contraseña", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    return true
                }
            }
        }else{
            when {
                // Si la contraseña esta vacia
                TextUtils.isEmpty(contra) -> {
                    Toast.makeText(this, "Error: Favor de introducir una contraseña", Toast.LENGTH_SHORT).show()
                }
                // Extension minima de 8 caracteres
                (contra.length < 8) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener una extension minima de 8 caracteres", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos una mayuscula
                (!Regex("[A-Z]+").containsMatchIn(contra)) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos un numero
                (!Regex("""\d""").containsMatchIn(contra)) -> {
                    Toast.makeText(this, "Error: La contraseña debera tener al menos un numero", Toast.LENGTH_SHORT).show()
                }
                // No se tiene al menos un caracter especial
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contra)) -> {
                    Toast.makeText(this, "Error: Favor de incluir al menos un caracter especial en su contraseña", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    return true
                }
            }
        }
        return false
    }
    private fun validarResp(respuesta: Editable): Boolean {
        return if(!TextUtils.isEmpty(respuesta)){
            true
        }else{
            Toast.makeText(this, "Error: Favor de introducir una respuesta para su pregunta", Toast.LENGTH_SHORT).show()
            false
        }
    }
    private fun validarPin(Pin: Editable): Boolean {
        when {
            TextUtils.isEmpty(Pin) -> {
                Toast.makeText(this, "Error: Favor de introducir un pin", Toast.LENGTH_SHORT).show()
            }
            (Regex("""\D""").containsMatchIn(Pin)) -> {
                Toast.makeText(this, "Error: El pin de acceso solo puede contener digitos", Toast.LENGTH_SHORT).show()
            }
            (Pin.length < 4) -> {
                Toast.makeText(this, "Advertencia: Se recomienda un pin numerico de al menos 4 digitos", Toast.LENGTH_SHORT).show()
            }
            else -> {
                return true
            }
        }
        return false
    }
    private fun validarTel(numTel: Editable): Boolean {
        when {
            // Si el telefono esta vacio
            TextUtils.isEmpty(numTel) -> {
                Toast.makeText(this, "Error: Favor de introducir un numero telefonico", Toast.LENGTH_SHORT).show()
            }
            // Si se encuentra algun caracter ademas de numeros
            (Regex("""\D""").containsMatchIn(numTel)) -> {
                Toast.makeText(this, "Error: El numero de telefono solo puede contener digitos", Toast.LENGTH_SHORT).show()
            }
            // Contemplando numeros fijos con lada y celulares; estos deberan ser de 10 caracteres
            (numTel.length < 10) -> {
                Toast.makeText(this, "Advertencia: Favor de introducir su numero telefonico fijo con lada o su celular", Toast.LENGTH_SHORT).show()
            }
            else -> {
                return true
            }
        }
        return false
    }

    //Actualizacion de campos
    private fun actNombre(nombre: String, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.IO) {
            val updNombre = async {
                val user = Firebase.auth.currentUser!!
                val actPerfil = userProfileChangeRequest { displayName = nombre }
                val database = Firebase.database
                // Actualizar el nombre del usuario visible en la lista de Firebase Auth
                user.updateProfile(actPerfil)
                //Actualizar el nombre del usuario en la BD; Paso 1: Creando la referencia de la coleccion de usuarios en la BD
                val refDB = database.getReference("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                val upNom = refDB.child(resUser.id_Usuario).child("nombre").setValue(nombre.trim())
                                upNom.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Su nombre fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upNom.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su nombre no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Nombre", databaseError.toException())
                    }
                })
            }
            updNombre.await()
        }
    }
    private fun actCorreo(nCorreo: String, correo: String, contra: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.IO) {
            val updCorreo = async {
                val user = Firebase.auth.currentUser!!
                val database = Firebase.database
                val refDB = database.getReference("Usuarios")
                val refDB2 = database.getReference("User_Sistems")
                // Para poder actualizar el correo, es necesario renovar las credenciales de acceso
                val credential = EmailAuthProvider.getCredential(correo, contra)
                val reAuth = user.reauthenticate(credential)
                reAuth.addOnSuccessListener {
                    val upEmaAuth = user.updateEmail(nCorreo)
                    upEmaAuth.addOnSuccessListener {
                        refDB.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for (objUs in dataSnapshot.children){
                                    val userJSON = gson.toJson(objUs.value)
                                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                    if(resUser.correo == correo){
                                        val upEmaUs = refDB.child(resUser.id_Usuario).child("correo").setValue(nCorreo.trim())
                                        upEmaUs.addOnSuccessListener {
                                            refDB2.addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(dataSnapshot: DataSnapshot){
                                                    for (objSisUs in dataSnapshot.children){
                                                        val sisUsJSON = gson.toJson(objSisUs.value)
                                                        val resSisUs = gson.fromJson(sisUsJSON, UserSistem::class.java)
                                                        if(resSisUs.user_Email == correo){
                                                            val upEmaSis = refDB2.child(resSisUs.id_User_Sis).child("user_Email").setValue(nCorreo.trim())
                                                            upEmaSis.addOnSuccessListener {
                                                                Toast.makeText(this@EditDataUsTxtActivity,"Su correo fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                                                Timer().schedule(2000) {
                                                                    FirebaseAuth.getInstance().signOut()
                                                                    lifecycleScope.launch(Dispatchers.Main){
                                                                        Intent(this@EditDataUsTxtActivity, MainActivity::class.java).apply {
                                                                            startActivity(this)
                                                                            finish()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            upEmaSis.addOnFailureListener {
                                                                Toast.makeText(this@EditDataUsTxtActivity, "Error: Su correo no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                                                Log.w("UpdateFirebaseError:", it.cause.toString())
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                                override fun onCancelled(databaseError: DatabaseError) {
                                                    Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Correo: UserSis", databaseError.toException())
                                                }
                                            })
                                        }
                                        upEmaUs.addOnFailureListener {
                                            Toast.makeText(this@EditDataUsTxtActivity, "Error: Su correo no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                            Log.w("UpdateFirebaseError:", it.cause.toString())
                                        }
                                    }
                                    break
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError","Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Correo", databaseError.toException())
                            }
                        })
                    }
                    upEmaAuth.addOnFailureListener {
                        Toast.makeText(this@EditDataUsTxtActivity, "Error: Su correo no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                        Log.w("UpdateFirebaseError:", it.cause.toString())
                    }
                }
            }
            updCorreo.await()
        }
    }
    private fun actContra(nContra: String, correo: String, contra: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updContra = async {
                val user = Firebase.auth.currentUser!!
                // Para poder actualizar la contraseña, es necesario renovar las credenciales de acceso
                val credential = EmailAuthProvider.getCredential(correo, contra)
                val reAuth = user.reauthenticate(credential)
                reAuth.addOnSuccessListener {
                    val upPas = user.updatePassword(nContra)
                    upPas.addOnSuccessListener {
                        Toast.makeText(this@EditDataUsTxtActivity,"Su contraseña fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                        Timer().schedule(2000){
                            FirebaseAuth.getInstance().signOut()
                            lifecycleScope.launch(Dispatchers.Main){
                                Intent(this@EditDataUsTxtActivity, MainActivity::class.java).apply {
                                    startActivity(this)
                                    finish()
                                }
                            }
                        }
                    }
                    upPas.addOnFailureListener {
                        Toast.makeText(this@EditDataUsTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                        Log.w("UpdateFirebaseError:", it.cause.toString())
                    }
                }
                reAuth.addOnFailureListener {
                    Toast.makeText(this@EditDataUsTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                    Log.w("UpdateFirebaseError:", it.cause.toString())
                }
            }
            updContra.await()
        }
    }
    private fun actResp(resp: String, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.IO) {
            val updResp = async {
                // Creando la referencia de la coleccion de usuarios en la BD
                val database = Firebase.database
                val refDB = database.getReference("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                val upResp = refDB.child(resUser.id_Usuario).child("respuesta_Seg").setValue(resp.trim())
                                upResp.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Su respuesta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upResp.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Error: Su respuesta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
                                }
                            }
                            break
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Respuesta", databaseError.toException())
                    }
                })
            }
            updResp.await()
        }
    }
    private fun actPin(pin: String, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.IO) {
            val updPin = async {
                val database = Firebase.database
                //Actualizar el nombre del usuario en la BD; Paso 1: Creando la referencia de la coleccion de usuarios en la BD
                val refDB = database.getReference("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                val upPin = refDB.child(resUser.id_Usuario).child("pin_Pass").setValue(pin.trim())
                                upPin.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Su pin fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upPin.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su pin no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Pin", databaseError.toException())
                    }
                })
            }
            updPin.await()
        }
    }
    private fun actTel(tel: String, correo: String, gson: Gson){
        lifecycleScope.launch(Dispatchers.IO) {
            val updTel = async {
                val database = Firebase.database
                //Actualizar el nombre del usuario en la BD; Paso 1: Creando la referencia de la coleccion de usuarios en la BD
                val refDB = database.getReference("Usuarios")
                refDB.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objUs in dataSnapshot.children){
                            val userJSON = gson.toJson(objUs.value)
                            val resUser = gson.fromJson(userJSON, Usuario::class.java)
                            if(resUser.correo == correo){
                                val upTel = refDB.child(resUser.id_Usuario).child("num_Tel").setValue(tel.toLong())
                                upTel.addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Su telefono fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(2000){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                upTel.addOnFailureListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su telefono no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Telefono", databaseError.toException())
                    }
                })
            }
            updTel.await()
        }
    }
}