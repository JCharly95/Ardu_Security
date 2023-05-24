package com.ardusec.ardu_security

import android.content.Context
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
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isGone
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class EditDataUserActivity : AppCompatActivity(){
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSec: TextView
    private lateinit var btnAyuda: Button
    private lateinit var txtValVie: EditText
    private lateinit var txtValNue: EditText
    private lateinit var spNPreg: Spinner
    private lateinit var spNSis: Spinner
    private lateinit var lblConfChg: TextView
    private lateinit var txtConfChg: EditText
    private lateinit var chbConfChg: CheckBox
    private lateinit var btnConfCamb: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var user: FirebaseUser
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Creando el objeto GSON
    private var gson = Gson()
    // Variable del correo para la busqueda del usuario en firebase auth
    private lateinit var email: String
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var campo: String
    // Contexto
    private lateinit var contexto: Context
    // Dataclases
    data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
    data class Sistema(val id_Sistema: String, val nombre_Sis: String, val tipo: String, val ulti_Cam_Nom: String)
    data class UserSistem(val id_User_Sis: String, val sistema_Nom: String, val user_Email: String)
    data class Pregunta(val ID_Pregunta: String, val Val_Pregunta: String)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_user)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Actualizar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblHeadSec = findViewById(R.id.lblHeadEdit)
        btnAyuda = findViewById(R.id.btnInfoActu)
        txtValVie = findViewById(R.id.txtOldEditDat)
        txtValNue = findViewById(R.id.txtNewEditDat)
        spNPreg = findViewById(R.id.spNewPregKey)
        spNSis = findViewById(R.id.spNewSis)
        lblConfChg = findViewById(R.id.lblConfChg)
        txtConfChg = findViewById(R.id.txtConfChg)
        chbConfChg = findViewById(R.id.chbConfPass)
        btnConfCamb = findViewById(R.id.btnConfEditData)
        // Inicializando instancia hacia el nodo raiz de la BD
        user = Firebase.auth.currentUser!!
        database = Firebase.database
        ref = database.reference
        contexto = applicationContext
        // Estableciendo la variable de correo
        email = ""
        // Obteniendo el extra enviado para saber que campo actualizar
        bundle = intent.extras!!
        campo = bundle.getString("campo").toString()
        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun setFormulario(){
        // Establecer el encabezado y el boton, acorde al campo a actualizar
        lblHeadSec.text = lblHeadSec.text.toString()+" "+campo
        btnConfCamb.text = btnConfCamb.text.toString()+" "+campo
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
                                "Nombre" -> { txtValVie.setText(resUser.nombre) }
                                "Correo" -> {
                                    lblConfChg.isGone = false
                                    txtConfChg.isGone = false
                                    chbConfChg.isGone = false
                                    txtValVie.setText(resUser.correo)
                                }
                                "Contraseña" -> {
                                    lblConfChg.isGone = false
                                    txtConfChg.isGone = false
                                    chbConfChg.isGone = false
                                    txtValVie.setText("Por seguridad, no es posible mostrar la contraseña previa")
                                }
                                "Pregunta" -> {
                                    // Si es el sistema o las preguntas a actualizar, se ocultara el campo de texto y se mostrara el Spinner
                                    txtValVie.setText(resUser.preg_Seguri)
                                    txtValNue.isGone = true
                                    spNPreg.isGone = false
                                    rellSpinPregs()
                                }
                                "Respuesta" -> { txtValVie.setText(resUser.resp_Seguri) }
                                "Sistema" -> {
                                    txtValNue.isGone = true
                                    spNSis.isGone = false
                                    rellSpinSis()
                                    ref = database.getReference("User_Sistems")
                                    ref.addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(dataSnapshot: DataSnapshot){
                                            for (objSisUs in dataSnapshot.children){
                                                val sisUSJSON = gson.toJson(objSisUs.value)
                                                val resSisUs = gson.fromJson(sisUSJSON, UserSistem::class.java)
                                                if(resSisUs.user_Email == email)
                                                    txtValVie.setText(resSisUs.sistema_Nom)
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError){
                                            Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                                        }
                                    })
                                }
                                "Pin" -> { txtValVie.setText(resUser.pin_Pass.toString()) }
                                "Telefono" -> { txtValVie.setText(resUser.num_Tel.toString()) }
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

    private fun rellSpinPregs(){
        // Obtener el arreglo de strings establecido para las preguntas
        val lstPregs = resources.getStringArray(R.array.lstSavQues)
        var arrPregs = ArrayList<String>()
        arrPregs.addAll(lstPregs)
        // Creando la referencia de la coleccion de preguntas en la BD
        ref = database.getReference("Pregunta")
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objPreg in dataSnapshot.children){
                    val pregJSON = gson.toJson(objPreg.value)
                    val resPreg = gson.fromJson(pregJSON, Pregunta::class.java)
                    arrPregs.add(resPreg.Val_Pregunta)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapPregs = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, arrPregs)
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
        arrSists.addAll(lstSists)
        // Creando la referencia de la coleccion de preguntas en la BD
        ref = database.getReference("Sistema")
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objSis in dataSnapshot.children){
                    val sisJSON = gson.toJson(objSis.value)
                    val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                    arrSists.add(resSis.nombre_Sis)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapSis = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, arrSists)
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

    private fun addListeners(){
        btnAyuda.setOnClickListener {
            avisoActu()
        }
        chbConfChg.setOnClickListener{
            if(!chbConfChg.isChecked){
                txtConfChg.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtConfChg.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnConfCamb.setOnClickListener{
            // Extraccion del correo del usuario desde Firebase Auth
            user.let { task ->
                email = task.email.toString()
                when (campo){
                    "Nombre" -> {
                        if(validarNombre(txtValNue.text))
                            actNombre(txtValNue.text.toString(), email, user, ref, gson)
                    }
                    "Correo" -> {
                        if(validarCorreo(txtValNue.text) && validarContra(txtConfChg.text))
                            actCorreo(txtValNue.text.toString(), email, txtConfChg.text.toString(), user, ref, gson)
                    }
                    "Contraseña" -> {
                        if(validarContra(txtValNue.text) && validarContra(txtConfChg.text))
                            actContra(txtValNue.text.toString(), email, txtConfChg.text.toString(), user)
                    }
                    "Pregunta" -> {
                        if(validarSelPreg(spNPreg))
                            actPreg(spNPreg.selectedItem.toString(), email, ref, gson)
                    }
                    "Respuesta" -> {
                        if(validarResp(txtValNue.text))
                            actResp(txtValNue.text.toString(), email, ref, gson)
                    }
                    "Sistema" -> {
                        if(validarSelSis(spNSis))
                            actSis(spNSis.selectedItem.toString(), email, ref, gson)
                    }
                    "Pin" -> {
                        if(validarPin(txtValNue.text))
                            actPin(txtValNue.text.toString(), email, ref, gson)
                    }
                    "Telefono" -> {
                        if(validarTel(txtValNue.text))
                            actTel(txtValNue.text.toString(), email, ref, gson)
                    }
                }
            }
        }
    }

    // Validaciones de campos
    private fun validarNombre(nombre: Editable): Boolean{
        when{
            // Si el nombre esta vacio
            TextUtils.isEmpty(nombre) -> Toast.makeText(contexto, "Error: Favor de introducir un nombre", Toast.LENGTH_SHORT).show()
            // Si se encuentra algun numero
            (Regex("""\d+""").containsMatchIn(nombre)) -> Toast.makeText(contexto, "Error: Su nombre no puede contener numeros", Toast.LENGTH_SHORT).show()
            // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
            (nombre.length < 10) -> Toast.makeText(contexto, "Error: Su nombre es muy corto, favor de agregar su nombre completo", Toast.LENGTH_SHORT).show()
            // Si se encuentran caracteres especiales
            (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> Toast.makeText(contexto, "Error: Su nombre no puede contener caracteres especiales", Toast.LENGTH_SHORT).show()
            else -> return true
        }
        return false
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        if(Regex("""\s+""").containsMatchIn(correo)){
            val correoFil = correo.replace("\\s".toRegex(), "")
            when{
                // Si el correo esta vacio
                TextUtils.isEmpty(correoFil) -> Toast.makeText(contexto, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil).matches() -> Toast.makeText(contexto, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                else -> return true
            }
        }else{
            when{
                // Si el correo esta vacio
                TextUtils.isEmpty(correo) -> Toast.makeText(contexto, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> Toast.makeText(contexto, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                else -> return true
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
                TextUtils.isEmpty(contraFil) -> Toast.makeText(contexto, "Error: Favor de introducir una contraseña", Toast.LENGTH_SHORT).show()
                // Extension minima de 8 caracteres
                (contraFil.length < 8) -> Toast.makeText(contexto, "Error: La contraseña debera tener una extension minima de 8 caracteres", Toast.LENGTH_SHORT).show()
                // No se tiene al menos una mayuscula
                (!Regex("[A-Z]+").containsMatchIn(contraFil)) -> Toast.makeText(contexto, "Error: La contraseña debera tener al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un numero
                (!Regex("""\d""").containsMatchIn(contraFil)) -> Toast.makeText(contexto, "Error: La contraseña debera tener al menos un numero", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un caracter especial
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil)) -> Toast.makeText(contexto, "Error: Favor de incluir al menos un caracter especial en su contraseña", Toast.LENGTH_SHORT).show()
                else -> return true
            }
        }else{
            when {
                // Si la contraseña esta vacia
                TextUtils.isEmpty(contra) -> Toast.makeText(contexto, "Error: Favor de introducir una contraseña", Toast.LENGTH_SHORT).show()
                // Extension minima de 8 caracteres
                (contra.length < 8) -> Toast.makeText(contexto, "Error: La contraseña debera tener una extension minima de 8 caracteres", Toast.LENGTH_SHORT).show()
                // No se tiene al menos una mayuscula
                (!Regex("[A-Z]+").containsMatchIn(contra)) -> Toast.makeText(contexto, "Error: La contraseña debera tener al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un numero
                (!Regex("""\d""").containsMatchIn(contra)) -> Toast.makeText(contexto, "Error: La contraseña debera tener al menos un numero", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un caracter especial
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contra)) -> Toast.makeText(contexto, "Error: Favor de incluir al menos un caracter especial en su contraseña", Toast.LENGTH_SHORT).show()
                else -> return true
            }
        }
        return false
    }
    private fun validarSelPreg(lista: Spinner): Boolean {
        if(lista.selectedItemPosition == 0){
            Toast.makeText(contexto, "Error: Favor de seleccionar una pregunta", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun validarResp(respuesta: Editable): Boolean {
        if(TextUtils.isEmpty(respuesta)){
            Toast.makeText(contexto, "Error: Favor de introducir una respuesta para su pregunta", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun validarSelSis(lista: Spinner): Boolean {
        if(lista.selectedItemPosition == 0){
            Toast.makeText(contexto, "Error: Favor de seleccionar un sistema", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun validarPin(Pin: Editable): Boolean {
        when {
            TextUtils.isEmpty(Pin) -> Toast.makeText(contexto, "Error: Favor de introducir un pin", Toast.LENGTH_SHORT).show()
            (Regex("""\D""").containsMatchIn(Pin)) -> Toast.makeText(contexto, "Error: El pin de acceso solo puede contener digitos", Toast.LENGTH_SHORT).show()
            (Pin.length < 4) -> Toast.makeText(contexto, "Advertencia: Se recomienda un pin numerico de al menos 4 digitos", Toast.LENGTH_SHORT).show()
            else -> return true
        }
        return false
    }
    private fun validarTel(numTel: Editable): Boolean {
        when {
            // Si el telefono esta vacio
            TextUtils.isEmpty(numTel) -> Toast.makeText(contexto, "Error: Favor de introducir un numero telefonico", Toast.LENGTH_SHORT).show()
            // Si se encuentra algun caracter ademas de numeros
            (Regex("""\D""").containsMatchIn(numTel)) -> Toast.makeText(contexto, "Error: El numero de telefono solo puede contener digitos", Toast.LENGTH_SHORT).show()
            // Contemplando numeros fijos con lada y celulares; estos deberan ser de 10 caracteres
            (numTel.length < 10) -> Toast.makeText(contexto, "Advertencia: Favor de introducir su numero telefonico fijo con lada o su celular", Toast.LENGTH_SHORT).show()
            else -> return true
        }
        return false
    }

    //Actualizacion de campos
    private fun actNombre(nombre: String, correo: String, user: FirebaseUser, genRef: DatabaseReference, gson: Gson){
        // Actualizar el nombre del usuario visible en la lista de Firebase Auth
        val actPerfil = userProfileChangeRequest { displayName = nombre }
        user.updateProfile(actPerfil)
        //Actualizar el nombre del usuario en la BD; Paso 1: Creando la referencia de la coleccion de usuarios en la BD
        val refDB = genRef.child("Usuarios")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        refDB.child(resUser.id_Usuario).child("nombre").setValue(nombre).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto,"Su nombre fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else {
                                Toast.makeText(contexto,"Error: Su nombre no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Nombre", databaseError.toException())
            }
        })
    }
    private fun actCorreo(nCorreo: String, correo: String, contra: String, user: FirebaseUser, genRef: DatabaseReference, gson: Gson){
        val refDB = genRef.child("Usuarios")
        val refDB2 = genRef.child("User_Sistems")

        // Para poder actualizar el correo, es necesario renovar las credenciales de acceso
        val credential = EmailAuthProvider.getCredential(correo, contra)
        user.reauthenticate(credential).addOnCompleteListener { taskReAuth ->
            if(taskReAuth.isSuccessful){
                // Primero se actualizara el correo en la autenticacion
                user.updateEmail(nCorreo).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        // Y luego se actualizara el valor en la base de datos en la entidad de usuarios
                        refDB.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for (objUs in dataSnapshot.children){
                                    val userJSON = gson.toJson(objUs.value)
                                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                    if(resUser.correo == correo){
                                        refDB.child(resUser.id_Usuario).child("correo").setValue(nCorreo.trim()).addOnCompleteListener { task2 ->
                                            if(task2.isSuccessful){
                                                // Y finalmente en la entidad intermedia de User_Sistems se actualizara el valor del correo
                                                refDB2.addValueEventListener(object: ValueEventListener{
                                                    override fun onDataChange(dataSnapshot: DataSnapshot){
                                                        for (objSisUs in dataSnapshot.children){
                                                            val sisUsJSON = gson.toJson(objSisUs.value)
                                                            val resSisUs = gson.fromJson(sisUsJSON, UserSistem::class.java)
                                                            if(resSisUs.user_Email == correo){
                                                                refDB2.child(resSisUs.id_User_Sis).child("user_Email").setValue(nCorreo.trim()).addOnCompleteListener { task3 ->
                                                                    if(task3.isSuccessful){
                                                                        Toast.makeText(contexto, "Su correo fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                                                        FirebaseAuth.getInstance().signOut()
                                                                        val endEditAcc = Intent(contexto, MainActivity::class.java)
                                                                        finish()
                                                                        startActivity(contexto, endEditAcc, null)
                                                                    }else{
                                                                        Toast.makeText(contexto, "Error: Su correo no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                                                        Log.w("UpdateFirebaseError:", task3.exception.toString())
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Correo: UserSis", databaseError.toException())
                                                    }
                                                })
                                            }else {
                                                Toast.makeText(contexto,"Error: Su correo no pudo ser actualizado en la BD",Toast.LENGTH_SHORT).show()
                                                Log.w("UpdateFirebaseErrorDB:", task2.exception.toString())
                                            }
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Correo", databaseError.toException())
                            }
                        })
                    }else{
                        Toast.makeText(contexto,"Error: Su correo no pudo ser actualizado en la autenticacion",Toast.LENGTH_SHORT).show()
                        Log.w("UpdateFirebaseError:", task.exception.toString())
                    }
                }
            }else{
                Toast.makeText(contexto, "Error: No se pudieron reautenticar las credenciales de acceso", Toast.LENGTH_SHORT).show()
                Log.w("UpdateFirebaseError:", taskReAuth.exception.toString())
            }
        }
    }
    private fun actContra(nContra: String, correo: String, contra: String, user: FirebaseUser){
        // Para poder actualizar la contraseña, es necesario renovar las credenciales de acceso
        val credential = EmailAuthProvider.getCredential(correo, contra)
        user.reauthenticate(credential)
        // Ya que la contraseña solo la guarda Firebase Auth y no se guarda en la BD, solo se ejecuta el metodo de auth
        user.updatePassword(nContra).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(contexto,"Su contraseña fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                val endEditAcc = Intent(contexto, MainActivity::class.java)
                finish()
                startActivity(contexto, endEditAcc, null)
            }else{
                Toast.makeText(contexto, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                Log.w("UpdateFirebaseError:", task.exception.toString())
            }
        }
    }
    private fun actPreg(selPreg: String, correo: String, genRef: DatabaseReference, gson: Gson){
        // Creando la referencia de la coleccion de usuarios en la BD
        val refDB = genRef.child("Usuarios")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        refDB.child(resUser.id_Usuario).child("preg_Seguri").setValue(selPreg).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto, "Su pregunta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else{
                                Toast.makeText(contexto, "Error: Su pregunta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Pregunta", databaseError.toException())
            }
        })
    }
    private fun actResp(resp: String, correo: String, genRef: DatabaseReference, gson: Gson){
        // Creando la referencia de la coleccion de usuarios en la BD
        val refDB = genRef.child("Usuarios")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        refDB.child(resUser.id_Usuario).child("resp_Seguri").setValue(resp).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto, "Su respuesta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else{
                                Toast.makeText(contexto, "Error: Su respuesta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Respuesta", databaseError.toException())
            }
        })
    }
    private fun actSis(selSis: String, correo: String, genRef: DatabaseReference, gson: Gson){
        // Creando la referencia de la coleccion de usuarios_sistemas en la BD
        val refDB = genRef.child("User_Sistems")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objSisUs in dataSnapshot.children){
                    val sisUsJSON = gson.toJson(objSisUs.value)
                    val resSisUs = gson.fromJson(sisUsJSON, UserSistem::class.java)
                    if(resSisUs.user_Email == correo){
                        refDB.child(resSisUs.id_User_Sis).child("sistema_Nom").setValue(selSis.trim()).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto, "Su sistema fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else{
                                Toast.makeText(contexto, "Error: Su sistema no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Sistema", databaseError.toException())
            }
        })
    }
    private fun actPin(pin: String, correo: String, genRef: DatabaseReference, gson: Gson){
        // Creando la referencia de la coleccion de usuarios en la BD
        val refDB = genRef.child("Usuarios")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        refDB.child(resUser.id_Usuario).child("pin_Pass").setValue(pin).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto, "Su pin fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else{
                                Toast.makeText(contexto, "Error: Su pin no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados: Pin", databaseError.toException())
            }
        })
    }
    private fun actTel(tel: String, correo: String, genRef: DatabaseReference, gson: Gson){
        // Creando la referencia de la coleccion de preguntas en la BD
        val refDB = genRef.child("Usuarios")
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        refDB.child(resUser.id_Usuario).child("num_Tel").setValue(tel.toLong()).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(contexto, "Su telefono fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                val endEdit = Intent(contexto, UserActivity::class.java)
                                finish()
                                startActivity(contexto, endEdit, null)
                            }else{
                                Toast.makeText(contexto, "Error: Su telefono no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                Log.w("UpdateFirebaseError:", task.exception.toString())
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados; Telefono", databaseError.toException())
            }
        })
    }
}