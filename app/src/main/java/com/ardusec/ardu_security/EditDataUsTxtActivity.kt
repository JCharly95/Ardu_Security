package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtValVie: TextView
    private lateinit var linLaySelEma: LinearLayout
    private lateinit var rbValNueEma: RadioButton
    private lateinit var rbValNueGoo: RadioButton
    private lateinit var txtValNueGen: EditText
    private lateinit var txtValNueEma: EditText
    private lateinit var txtValNueNum: EditText
    private lateinit var txtValNuePas: EditText
    private lateinit var linLayConfChg: LinearLayout
    private lateinit var rbSelEmaConf: RadioButton
    private lateinit var rbSelGooConf: RadioButton
    private lateinit var txtConfEmaChg: EditText
    private lateinit var txtConfPassChg: EditText
    private lateinit var chbConfChg: CheckBox
    private lateinit var btnConfCamb: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var campo: String
    private lateinit var user: String
    // Variables de acceso para google
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
    private val GoogleAcces = 195
    // Dataclases
    data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val pregunta_Seg: String, val respuesta_Seg: String, val pin_Pass: Int)

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_us_txt)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@EditDataUsTxtActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            campo = bundle.getString("campo").toString()
            user = bundle.getString("usuario").toString()
        }

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
        linLaySelEma = findViewById(R.id.linLayActuEma)
        rbValNueEma = findViewById(R.id.rbSelEmaActu)
        rbValNueGoo = findViewById(R.id.rbSelGooActu)
        txtValNueGen = findViewById(R.id.txtNewDatGen)
        txtValNueEma = findViewById(R.id.txtEmailActu)
        txtValNueNum = findViewById(R.id.txtNewDatNum)
        txtValNuePas = findViewById(R.id.txtNewDatPass)
        linLayConfChg = findViewById(R.id.linLayConfChg)
        rbSelEmaConf = findViewById(R.id.rbSelProvEma)
        rbSelGooConf = findViewById(R.id.rbSelProvGoo)
        txtConfEmaChg = findViewById(R.id.txtConfEmail)
        txtConfPassChg = findViewById(R.id.txtConfPass)
        chbConfChg = findViewById(R.id.chbConfPass)
        btnConfCamb = findViewById(R.id.btnConfEditDataTxt)

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        // Establecer el encabezado y el boton, acorde al campo a actualizar
        lblHeadSec.text = lblHeadSec.text.toString()+"\n"+campo
        btnConfCamb.text = btnConfCamb.text.toString()+"\n"+campo

        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun avisoActu(mensaje: String){
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
        when(campo){
            "Nombre" -> {
                lifecycleScope.launch(Dispatchers.IO){
                    val getNombre = async {
                        ref = database.getReference("Usuarios")
                        ref.get().addOnSuccessListener { taskGetNom ->
                            for(objUs in taskGetNom.children) {
                                if(objUs.key.toString() == user) {
                                    txtValVie.text = objUs.child("nombre").value.toString()
                                    txtValNueGen.isGone = false
                                    break
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getNombre.await()
                }
            }
            "Correo" -> {
                txtValVie.text = "Seleccione el correo de acceso que desea actualizar"
                linLaySelEma.isGone = false
                txtValNueEma.isGone = false
                linLayConfChg.isGone = false
            }
            "Username" -> {
                txtValVie.text = user
                txtValNueGen.isGone = false
            }
            "Contraseña" -> {
                txtValVie.text = "Por seguridad no se puede mostrar este valor, ingrese su nueva contraseña"
                txtValNuePas.isGone = false
                linLayConfChg.isGone = false
            }
            "Respuesta" -> {
                lifecycleScope.launch(Dispatchers.IO){
                    val getResp = async {
                        ref = database.getReference("Usuarios")
                        ref.get().addOnSuccessListener { taskGetNom ->
                            for(objUs in taskGetNom.children){
                                if(objUs.key.toString() == user){
                                    txtValVie.text = objUs.child("resp_Seguri").value.toString()
                                    txtValNueGen.isGone = false
                                    break
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getResp.await()
                }
            }
            "Telefono" -> {
                lifecycleScope.launch(Dispatchers.IO){
                    val getTel = async {
                        ref = database.getReference("Usuarios")
                        ref.get().addOnSuccessListener { taskGetTel ->
                            for(objUs in taskGetTel.children) {
                                if(objUs.key.toString() == user){
                                    txtValVie.text = objUs.child("num_Tel").value.toString()
                                    txtValNueNum.isGone = false
                                    break
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getTel.await()
                }
            }
        }
    }
    private fun addListeners() {
        /*
        private lateinit var btnConfCamb: Button*/
        btnAyuda.setOnClickListener {
            val msg = "Consideraciones de campos: \n\n" +
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
            avisoActu(msg)
        }
        rbValNueEma.setOnClickListener {
            if(rbValNueEma.isChecked) {
                avisoActu("El correo es un dato sensible a modificar, por lo que es necesario confirmar el cambio mediante el uso de su contraseña")
                lifecycleScope.launch(Dispatchers.IO) {
                    val getEma = async {
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children) {
                                    if(objUs.key.toString() == user) {
                                        txtValVie.text = objUs.child("accesos").child("correo").value.toString()
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getEma.await()
                }
            }
        }
        rbValNueGoo.setOnClickListener {
            if(rbValNueGoo.isChecked) {
                avisoActu("El correo es un dato sensible a modificar, por lo que es necesario confirmar el cambio mediante el uso de su contraseña")
                lifecycleScope.launch(Dispatchers.IO) {
                    val getGoo = async {
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children) {
                                    if(objUs.key.toString() == user) {
                                        txtValVie.text = objUs.child("accesos").child("google").value.toString()
                                        // Preparar la peticion de google
                                        crearPeticionGoogle()
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getGoo.await()
                }
            }
        }
        rbSelEmaConf.setOnClickListener {
            if(rbSelEmaConf.isChecked){
                txtConfEmaChg.isGone = false
            }
        }
        rbSelGooConf.setOnClickListener {
            if(rbSelGooConf.isChecked){
                txtConfEmaChg.isGone = true
            }
        }
        chbConfChg.setOnClickListener {
            if(!chbConfChg.isChecked){
                txtConfPassChg.transformationMethod = PasswordTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtConfPassChg.transformationMethod = HideReturnsTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnConfCamb.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val confChg = async {
                    when(campo){
                        "Nombre" -> {
                            if(validarNombre(txtValNueGen.text)){
                                actNombre(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Correo" -> {
                            if(rbValNueEma.isChecked){
                                if(validarCorreo(txtValNueEma.text) && validarContra(txtConfPassChg.text)) {
                                    actEma(txtValNueEma.text.toString(), txtValVie.text.toString(), txtConfPassChg.text.toString(), user)
                                }
                            }else if(rbValNueGoo.isChecked){
                                if(validarCorreo(txtValNueEma.text) && validarContra(txtConfPassChg.text)) {
                                    actEmaGoo(txtValNueEma.text.toString(), txtValVie.text.toString(), txtConfPassChg.text.toString(), user)
                                }
                            }
                        }
                        "Username" -> {
                            if(validarUsuario(txtValNueGen.text)){
                                actUsername(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Contraseña" -> {
                            if(validarContra(txtValNuePas.text) && validarContra(txtConfPassChg.text)) {
                                actContra(txtValNuePas.text.toString(), user, txtConfPassChg.text.toString())
                            }
                        }
                        "Respuesta" -> {
                            if(validarResp(txtValNueGen.text)){
                                actResp(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Telefono" -> {
                            if(validarTel(txtValNueNum.text)){
                                actTel(txtValNueNum.text.toString().toLong(), user)
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
            TextUtils.isEmpty(nombre) -> avisoActu("Error: Favor de introducir un nombre")
            // Si se encuentra algun numero
            (Regex("""\d+""").containsMatchIn(nombre)) -> avisoActu("Error: Su nombre no puede contener numeros")
            // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
            (nombre.length < 10) -> avisoActu("Error: Su nombre es muy corto, favor de agregar su nombre completo")
            // Si se encuentran caracteres especiales
            (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> avisoActu("Error: Su nombre no puede contener caracteres especiales")
            else -> return true
        }
        return false
    }
    private fun validarUsuario(usuario: Editable): Boolean{
        // Si se detectan espacios en blanco en el usuario, seran removidos
        val usuarioFil1 = usuario.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val usuarioFil2 = usuarioFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el usuario esta vacia
            TextUtils.isEmpty(usuarioFil2) -> avisoActu("Error: Favor de introducir un nombre de usuario")
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> avisoActu("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> avisoActu("Error: El nombre de usuario debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> avisoActu("Error: El nombre de usuario debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(usuarioFil2)) -> avisoActu("Error: Favor de incluir al menos un caracter especial en su nombre de usuario")
            else -> return true
        }
        return false
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        val correoFil1 = correo.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val correoFil2 = correoFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el correo esta vacio
            TextUtils.isEmpty(correoFil2) -> avisoActu("Error: Favor de introducir un correo")
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> avisoActu("Error: Favor de introducir un correo valido")
            else -> return true
        }
        return false
    }
    private fun validarContra(contra: Editable): Boolean{
        // Si se detectan espacios en la contraseña, estos seran removidos
        val contraFil1 = contra.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val contraFil2 = contraFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si la contraseña esta vacia
            TextUtils.isEmpty(contraFil2) -> avisoActu("Error: Favor de introducir una contraseña")
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> avisoActu("Error: La contraseña debera tener una extension minima de 8 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> avisoActu("Error: La contraseña debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> avisoActu("Error: La contraseña debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> avisoActu("Error: Favor de incluir al menos un caracter especial en su contraseña")
            else -> return true
        }
        return false
    }
    private fun validarResp(respuesta: Editable): Boolean{
        if(TextUtils.isEmpty(respuesta)){
            avisoActu("Error: Favor de introducir una respuesta para su pregunta")
            return false
        }
        return true
    }
    private fun validarTel(numTel: Editable): Boolean{
        // Si se detectan espacios en el numero, estos seran removidos
        val numTelFil1 = numTel.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val numTelFil2 = numTelFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el telefono esta vacio
            TextUtils.isEmpty(numTelFil2) -> avisoActu("Error: Favor de introducir un numero telefonico")
            // Si se encuentra algun caracter ademas de numeros
            (Regex("""\D""").containsMatchIn(numTelFil2)) -> avisoActu("Error: El numero de telefono solo puede contener digitos")
            // Contemplando numeros fijos con lada y celulares; estos deberan ser de 10 caracteres
            (numTelFil2.length < 10) -> avisoActu("Advertencia: Favor de introducir su numero telefonico fijo con lada o su celular")
            else -> return true
        }
        return false
    }

    private fun crearPeticionGoogle() {
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@EditDataUsTxtActivity, googleConf)
        // Fin de crearPeticionGoogle() y preparar la peticion de google
    }

    private fun chgGoogle(campoChg: String) {
        // Obteniendo el intent de google
        val intentGoo = googleCli.signInIntent.apply {
            putExtra("campoGoo", campoChg)
        }
        // Implementando el launcher result posterior al haber obtenido el intent de google
        //startForResult.launch(intentGoo)
        startActivityForResult(intentGoo, GoogleAcces)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si el codigo de respuesta es el mismo que se planteo para el login de google, se procede con la preparacion del cliente google
        if (requestCode == GoogleAcces) {
            val userAuth = auth.currentUser!!
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var campoGoo = ""
            if(intent.extras == null){
                Toast.makeText(this@EditDataUsTxtActivity, "Error: no se pudo obtener el campo solicitado mediante Google", Toast.LENGTH_SHORT).show()
            }else{
                bundle = intent.extras!!
                campoGoo = bundle.getString("campoGoo").toString()
            }

            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                userAuth.reauthenticate(credencial).addOnSuccessListener {
                    when(campoGoo){
                        "Correo" -> {
                            userAuth.let { _ ->
                                val correo = userAuth.email
                                userAuth.updateEmail(correo.toString()).addOnSuccessListener {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val updCorreo = async {
                                            // Cuando se actualice correo en auth, se procedera con la actualizacion en la DB
                                            ref = database.getReference("Usuarios")
                                            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    for(objUs in dataSnapshot.children){
                                                        if(objUs.key.toString() == user){
                                                            val refEma = objUs.ref.child("accesos")
                                                            refEma.child("google").setValue(correo!!.trim()).addOnSuccessListener {
                                                                Toast.makeText(this@EditDataUsTxtActivity,"Su correo fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                                                Timer().schedule(1500) {
                                                                    lifecycleScope.launch(Dispatchers.Main){
                                                                        retorno()
                                                                        finish()
                                                                    }
                                                                }
                                                            }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su correo no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su correo no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        }
                                        updCorreo.await()
                                    }
                                }
                            }
                        }
                        "Contraseña" -> {
                            userAuth.updatePassword(txtValNuePas.text.toString()).addOnSuccessListener {
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
                                .addOnFailureListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                    Log.w("UpdateFirebaseError:", it.cause.toString())
                                }
                        }
                    }
                }
            }catch (error: ApiException){
                avisoActu("Error: No se pudieron actualizar los valores con la informacion ingresada")
            }
        }
    }

    //Actualizacion de campos
    private fun actNombre(nombre: String, usuario: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updNombre = async {
                // Actualizar el nombre del usuario visible en la lista de Firebase Auth
                val user = auth.currentUser!!
                val actPerfil = userProfileChangeRequest { displayName = nombre }
                user.updateProfile(actPerfil)
                // Actualizacion en la BD
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUs in dataSnapshot.children){
                            if(objUs.key.toString() == usuario){
                                objUs.ref.child("nombre").setValue(nombre.trim()).addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Su nombre fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su nombre no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su nombre no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updNombre.await()
        }
    }
    private fun actEma(nCorreo: String, correo: String, contra: String, usuario: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updCorreo = async {
                val userAuth = auth.currentUser!!
                // Para poder actualizar el correo, es necesario renovar las credenciales de acceso
                val credential = EmailAuthProvider.getCredential(correo, contra)
                val reAuth = userAuth.reauthenticate(credential)
                reAuth.addOnSuccessListener {
                    // Cuando se reautentique el usuario se actualizara el valor en auth
                    userAuth.updateEmail(nCorreo).addOnSuccessListener {
                    // Cuando se actualice correo en auth, se procedera con la actualizacion en la DB
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children){
                                    if(objUs.key.toString() == usuario){
                                        val refEma = objUs.ref.child("accesos")
                                        refEma.child("correo").setValue(nCorreo.trim()).addOnSuccessListener {
                                            Toast.makeText(this@EditDataUsTxtActivity,"Su correo fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                            Timer().schedule(1500) {
                                                lifecycleScope.launch(Dispatchers.Main){
                                                    retorno()
                                                    finish()
                                                }
                                            }
                                        }
                                            .addOnFailureListener {
                                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Su correo no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataUsTxtActivity,"Error: Su correo no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
            updCorreo.await()
        }
    }

    private fun actEmaGoo(nDirGoo: String, correo: String, contra: String, usuario: String) {

    }

    private fun actUsername(nUser: String, usuario: String){
        //Actualizando la informacion del username y sus relaciones, parte 1: entidad Preguntas
        lifecycleScope.launch(Dispatchers.IO){
            val updUser = async {
                ref = database.getReference("Preguntas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objPregs in dataSnapshot.children) {
                            val refPregUs = objPregs.ref.child("usuarios")
                            refPregUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(objUsers in snapshot.children) {
                                        if(objUsers.key.toString() == usuario) {
                                            // Dado que no es posible renombrar las keys de los objetos
                                            // se removera el valor con el usuario previo y se creara uno nuevo con el nuevo usuario
                                            objUsers.ref.removeValue()
                                            refPregUs.child(nUser).setValue(true)
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su usuario no pudo ser actualizado, proceso fallido p11",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su usuario no pudo ser actualizado, proceso fallido p12",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updUser.await()
        }
        //Actualizando la informacion del username y sus relaciones, parte 2: entidad Sistemas
        lifecycleScope.launch(Dispatchers.IO){
            val updUser = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children) {
                            val refSisUs = objSis.ref.child("usuarios")
                            refSisUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(objUsers in snapshot.children) {
                                        if(objUsers.key.toString() == usuario) {
                                            // Dado que no es posible renombrar las keys de los objetos
                                            // se removera el valor con el usuario previo y se creara uno nuevo con el nuevo usuario
                                            objUsers.ref.removeValue()
                                            refSisUs.child(nUser).setValue(true)
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Error: Su usuario no pudo ser actualizado, proceso fallido p11",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su usuario no pudo ser actualizado, proceso fallido p12",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updUser.await()
        }
        //Actualizando la informacion del username y sus relaciones, parte 3: entidad Usuarios
        lifecycleScope.launch(Dispatchers.IO){
            val updUser = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == usuario) {
                                // Primero se cambiara el valor del atributo username del objeto
                                objUser.ref.child("username").setValue(nUser)
                                // Dado que no es posible renombrar las keys de los objetos
                                // se removera el valor con el usuario previo y se creara uno nuevo con el nuevo usuario
                                // En este caso, se tomará el valor completo del usuario y se escribira uno nuevo con los datos del viejo
                                objUser.ref.child(nUser).setValue(objUser.child(usuario).value)
                                objUser.ref.child(usuario).removeValue()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su usuario no pudo ser actualizado, proceso fallido p12",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updUser.await()
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
    private fun actResp(resp: String, usuario: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updResp = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUs in dataSnapshot.children){
                            if(objUs.key.toString() == usuario){
                                objUs.ref.child("resp_Seguri").setValue(resp).addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity,"Su respuesta fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su respuesta no pudo ser actualizada, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su respuesta no pudo ser actualizada",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updResp.await()
        }
    }
    private fun actTel(telefono: Long, usuario: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updTel = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUs in dataSnapshot.children) {
                            if(objUs.key.toString() == usuario) {
                                objUs.ref.child("num_Tel").setValue(telefono).addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsTxtActivity, "Su telefono fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su telefono no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataUsTxtActivity,"Error: Su telefono no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updTel.await()
        }
    }
}