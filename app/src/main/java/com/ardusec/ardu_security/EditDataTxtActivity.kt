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
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class EditDataTxtActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSec: TextView
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtValVie1: TextView
    private lateinit var letrero2: MaterialCardView
    private lateinit var txtValVie2: TextView
    private lateinit var linLaySelProv: LinearLayout
    private lateinit var rbValNueEma: RadioButton
    private lateinit var rbValNueGoo: RadioButton
    private lateinit var txtValNueGen: EditText
    private lateinit var txtValNueEma: EditText
    private lateinit var txtValNueNum: EditText
    private lateinit var txtValNuePas: EditText
    private lateinit var mateLayConf: MaterialCardView
    private lateinit var lblConfPreg: TextView
    private lateinit var txtConfResp: EditText
    private lateinit var txtConfEma: EditText
    private lateinit var txtConfPass: EditText
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
    private lateinit var respuesta: String
    private lateinit var sistema: String
    // Variables de acceso para google
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
    private val GoogleAcces = 195

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_txt)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))
        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@EditDataTxtActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            campo = bundle.getString("campo").toString()
            user = bundle.getString("usuario").toString()
            sistema = bundle.getString("sistema").toString()
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
        txtValVie1 = findViewById(R.id.txtValorPrevGuarda1)
        letrero2 = findViewById(R.id.letrero2)
        txtValVie2 = findViewById(R.id.txtValorPrevGuarda2)
        linLaySelProv = findViewById(R.id.linLaySelProv)
        rbValNueEma = findViewById(R.id.rbSelEmaActu)
        rbValNueGoo = findViewById(R.id.rbSelGooActu)
        txtValNueGen = findViewById(R.id.txtNewDatGen)
        txtValNueEma = findViewById(R.id.txtNewDatEma)
        txtValNueNum = findViewById(R.id.txtNewDatNum)
        txtValNuePas = findViewById(R.id.txtNewDatPass)
        mateLayConf = findViewById(R.id.mateCardConfChg)
        lblConfPreg = findViewById(R.id.lblPregConf)
        txtConfResp = findViewById(R.id.txtConfResp)
        txtConfEma = findViewById(R.id.txtConfEmail)
        txtConfPass = findViewById(R.id.txtConfPass)
        chbConfChg = findViewById(R.id.chbConfPass)
        btnConfCamb = findViewById(R.id.btnConfEditDataTxt)

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        // Establecer el encabezado y el boton, acorde al campo a actualizar
        val letHeadVal = lblHeadSec.text.toString()+"\n"+campo
        lblHeadSec.text = letHeadVal
        val letBtnVal = btnConfCamb.text.toString()+"\n"+campo
        btnConfCamb.text = letBtnVal

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
        lifecycleScope.launch(Dispatchers.IO){
            val getVals = async {
                ref = database.getReference("Usuarios")
                val obteNom = ref.get()
                obteNom.addOnSuccessListener { taskGetValues ->
                    for (objUs in taskGetValues.children) {
                        if (objUs.key.toString() == user) {
                            when(campo){
                                "Nombre" -> {
                                    txtValVie1.text = objUs.child("nombre").value.toString()
                                    txtValNueGen.isGone = false
                                }
                                "Correo" -> {
                                    val valEmail = objUs.child("accesos").child("correo").value
                                    val valGoogle = objUs.child("accesos").child("google").value
                                    val correo1 = "Correo: $valEmail"
                                    val correo2 = "Google: $valGoogle"
                                    txtValVie1.text = correo1
                                    letrero2.isGone = false
                                    txtValVie2.text = correo2
                                    Toast.makeText(this@EditDataTxtActivity,"Seleccione un proveedor de correo",Toast.LENGTH_SHORT).show()
                                    txtValNueEma.isGone = false
                                    linLaySelProv.isGone = false
                                    mateLayConf.isGone = false
                                    // Obtener la respuesta y la pregunta del usuario
                                    respuesta = objUs.child("resp_Seguri").value.toString()
                                    ref = database.getReference("Preguntas")
                                    ref.get().addOnSuccessListener { taskGetPreg ->
                                        for(objPreg in taskGetPreg.children) {
                                            if(objPreg.key.toString() == objUs.child("pregunta_Seg").value) {
                                                lblConfPreg.text = objPreg.child("Val_Pregunta").value.toString()
                                                break
                                            }
                                        }
                                    }
                                    // Preparar la peticion de google por si se usa el correo de google
                                    crearPeticionGoogle()
                                }
                                "Username" -> {
                                    txtValVie1.text = objUs.child("username").value.toString()
                                    txtValNueGen.isGone = false
                                }
                                "Contraseña" -> {
                                    // Obtener la respuesta y la pregunta del usuario
                                    respuesta = objUs.child("resp_Seguri").value.toString()
                                    ref = database.getReference("Preguntas")
                                    ref.get().addOnSuccessListener { taskGetPreg ->
                                        for(objPreg in taskGetPreg.children) {
                                            if(objPreg.key.toString() == objUs.child("pregunta_Seg").value) {
                                                lblConfPreg.text = objPreg.child("Val_Pregunta").value.toString()
                                                break
                                            }
                                        }
                                    }
                                    val avisoContra = "Por seguridad no se puede mostrar este valor, ingrese su nueva contraseña"
                                    txtValVie1.text = avisoContra
                                    txtValNuePas.isGone = false
                                    linLaySelProv.isGone = false
                                    mateLayConf.isGone = false
                                }
                                "Respuesta" -> {
                                    txtValVie1.text = objUs.child("resp_Seguri").value.toString()
                                    txtValNueGen.isGone = false
                                }
                                "Telefono" -> {
                                    txtValVie1.text = objUs.child("num_Tel").value.toString()
                                    txtValNueNum.isGone = false
                                }
                                "Nombre Sistema" -> {
                                    lifecycleScope.launch(Dispatchers.IO){
                                        val getSis = async {
                                            ref = database.getReference("Sistemas")
                                            ref.get().addOnSuccessListener {
                                                for(objSis in it.children){
                                                    if(objSis.key.toString() == sistema){
                                                        txtValVie1.text = objSis.child("nombre_Sis").value.toString()
                                                        txtValNueGen.isGone = false
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                        getSis.await()
                                    }
                                }
                            }
                            break
                        }
                    }
                }
            }
            getVals.await()
        }
    }
    private fun addListeners() {
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
            txtConfEma.isGone = false
            txtConfPass.isGone = false
            chbConfChg.isGone = false
            lblConfPreg.isGone = true
            txtConfResp.isGone = true
        }
        rbValNueGoo.setOnClickListener {
            txtConfEma.isGone = true
            txtConfPass.isGone = true
            chbConfChg.isGone = true
            lblConfPreg.isGone = false
            txtConfResp.isGone = false
        }
        chbConfChg.setOnClickListener {
            if(!chbConfChg.isChecked){
                txtConfPass.transformationMethod = PasswordTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtConfPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                txtValNuePas.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnConfCamb.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val confChg = async {
                    when(campo){
                        "Nombre" -> {
                            if(validarNombre(txtValNueGen.text)) {
                                actNombre(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Correo" -> {
                            if(rbValNueEma.isChecked) {
                                if(validarCorreo(txtValNueEma.text) && validarCorreo(txtConfEma.text) && validarContra(txtConfPass.text)) {
                                    actCorreo(txtValNueEma.text.toString(), user)
                                }
                            }else if(rbValNueGoo.isChecked) {
                                // Dado que cuando se accede con google no se cuenta como tal con una contra, se usara la respuesta de seguridad en su lugar
                                if(validarCorreo(txtValNueEma.text) && (txtConfEma.text.toString() == respuesta)) {
                                    chgGoogle("Correo")
                                }
                            }
                        }
                        "Username" -> {
                            if(validarUsuario(txtValNueGen.text)) {
                                actUsername(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Contraseña" -> {
                            if(rbValNueEma.isChecked) {
                                if(validarContra(txtValNuePas.text) && validarContra(txtConfPass.text)) {
                                    actContra(txtValNuePas.text.toString())
                                }
                            }else if(rbValNueGoo.isChecked) {
                                if(validarContra(txtConfPass.text) && (txtConfEma.text.toString() == respuesta)) {
                                    chgGoogle("Contraseña")
                                }
                            }
                        }
                        "Respuesta" -> {
                            if(validarResp(txtValNueGen.text)) {
                                actResp(txtValNueGen.text.toString(), user)
                            }
                        }
                        "Telefono" -> {
                            if(validarTel(txtValNueNum.text)) {
                                actTel(txtValNueNum.text.toString().toLong(), user)
                            }
                        }
                        "Nombre Sistema" -> {
                            if(!txtValNueGen.text.isNullOrEmpty()) {
                                actNomSis(txtValNueGen.text.toString(), sistema, user)
                            }
                        }
                        else -> {
                            Toast.makeText(this@EditDataTxtActivity, "Error: El campo solicitado no esta disponible", Toast.LENGTH_SHORT).show()
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
        googleCli = GoogleSignIn.getClient(this@EditDataTxtActivity, googleConf)
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
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var campoGoo = ""
            if(intent.extras == null){
                Toast.makeText(this@EditDataTxtActivity, "Error: no se pudo obtener el campo solicitado mediante Google", Toast.LENGTH_SHORT).show()
            }else {
                bundle = intent.extras!!
                campoGoo = bundle.getString("campoGoo").toString()
            }
            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                val userAuth = auth.currentUser!!
                lifecycleScope.launch(Dispatchers.IO) {
                    val updValsGoo = async {
                        val reautenticar = userAuth.reauthenticate(credencial)
                        reautenticar.addOnSuccessListener {
                            when(campoGoo){
                                "Correo" -> {
                                    userAuth.updateEmail(txtValNueEma.text.toString())
                                        .addOnSuccessListener {
                                            // Cuando se actualice correo en auth, se procedera con la actualizacion en la DB
                                            ref = database.getReference("Usuarios")
                                            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    for(objUs in dataSnapshot.children){
                                                        if(objUs.key.toString() == user){
                                                            val refEma = objUs.ref.child("accesos")
                                                            refEma.child("google").setValue(txtValNueEma.text.toString().trim())
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(this@EditDataTxtActivity,"Su correo fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                                                    Timer().schedule(1500) {
                                                                        lifecycleScope.launch(Dispatchers.Main){
                                                                            retorno()
                                                                            finish()
                                                                        }
                                                                    }
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(this@EditDataTxtActivity,"Error: Su correo no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(this@EditDataTxtActivity,"Error: Su correo no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this@EditDataTxtActivity,"Su correo no pudo ser actualizado, proceso incompleto",Toast.LENGTH_SHORT).show()
                                        }
                                }
                                "Contraseña" -> {
                                    userAuth.updatePassword(txtValNuePas.text.toString())
                                        .addOnSuccessListener {
                                        Toast.makeText(this@EditDataTxtActivity,"Su contraseña fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                                        Timer().schedule(1500){
                                            FirebaseAuth.getInstance().signOut()
                                            lifecycleScope.launch(Dispatchers.Main){
                                                Intent(this@EditDataTxtActivity, MainActivity::class.java).apply {
                                                    startActivity(this)
                                                    finish()
                                                }
                                            }
                                        }
                                    }
                                        .addOnFailureListener {
                                            Toast.makeText(this@EditDataTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        reautenticar.addOnFailureListener {
                            Toast.makeText(this@EditDataTxtActivity,"El usuario no pudo ser reautenticado",Toast.LENGTH_SHORT).show()
                        }
                    }
                    updValsGoo.await()
                }
            }catch (error: ApiException){
                avisoActu("Error: No se pudieron actualizar los valores solicitados con la informacion ingresada")
            }
        }
    }

    //Actualizacion de campos
    private fun actNombre(nombre: String, usuario: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updNombre = async {
                // Actualizar el nombre del usuario visible en la lista de Firebase Auth
                val user1 = auth.currentUser!!
                val actPerfil = userProfileChangeRequest { displayName = nombre }
                user1.updateProfile(actPerfil)
                // Actualizacion en la BD
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUs in dataSnapshot.children){
                            if(objUs.key.toString() == usuario){
                                objUs.ref.child("nombre").setValue(nombre.trim()).addOnSuccessListener {
                                    Toast.makeText(this@EditDataTxtActivity,"Su nombre fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataTxtActivity,"Error: Su nombre no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su nombre no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updNombre.await()
        }
    }

    private fun actCorreo(nCorreo: String, usuario: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updCorreo = async {
                val userAuth = auth.currentUser!!
                // Para poder actualizar el correo, es necesario renovar las credenciales de acceso
                val credencial = EmailAuthProvider.getCredential(txtConfEma.text.toString(), txtConfPass.text.toString())
                val reautenticar = userAuth.reauthenticate(credencial)
                reautenticar.addOnSuccessListener {
                    // Cuando se reautentique el usuario se actualizara el valor en auth
                    val actualizar = userAuth.updateEmail(nCorreo)
                    actualizar.addOnSuccessListener {
                        // Cuando se actualice correo en auth, se procedera con la actualizacion en la DB
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children){
                                    if(objUs.key.toString() == usuario){
                                        val refEma = objUs.ref.child("accesos")
                                        val actuCorr = refEma.child("correo").setValue(nCorreo.trim())
                                        actuCorr.addOnSuccessListener {
                                            Toast.makeText(this@EditDataTxtActivity,"Su correo fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                            Timer().schedule(1500) {
                                                lifecycleScope.launch(Dispatchers.Main){
                                                    retorno()
                                                    finish()
                                                }
                                            }
                                        }
                                        actuCorr.addOnFailureListener {
                                            Toast.makeText(this@EditDataTxtActivity,"Error: Su correo no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataTxtActivity,"Error: Su correo no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    actualizar.addOnFailureListener {
                        Toast.makeText(this@EditDataTxtActivity,"Su correo no pudo ser actualizado, proceso incompleto",Toast.LENGTH_SHORT).show()
                    }
                }
                reautenticar.addOnFailureListener {
                    Toast.makeText(this@EditDataTxtActivity,"El usuario no pudo ser reautenticado",Toast.LENGTH_SHORT).show()
                }
            }
            updCorreo.await()
        }
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
                                    Toast.makeText(this@EditDataTxtActivity,"Error: Su username no pudo ser actualizado, p11",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su username no pudo ser actualizado, p12",Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@EditDataTxtActivity,"Error: Su username no pudo ser actualizado, p21",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su username no pudo ser actualizado, p22",Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@EditDataTxtActivity,"Su username fue actualizado satisfactoriamente",Toast.LENGTH_SHORT).show()
                                Timer().schedule(1500) {
                                    lifecycleScope.launch(Dispatchers.Main){
                                        retorno()
                                        finish()
                                    }
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su username no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updUser.await()
        }
    }
    private fun actContra(nContra: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val updContra = async {
                val user = Firebase.auth.currentUser!!
                // Para poder actualizar la contraseña, es necesario renovar las credenciales de acceso
                val credencial = EmailAuthProvider.getCredential(txtConfEma.text.toString(), txtConfPass.text.toString())
                val reautenticar = user.reauthenticate(credencial)
                reautenticar.addOnSuccessListener {
                    val upPas = user.updatePassword(nContra)
                    upPas.addOnSuccessListener {
                        Toast.makeText(this@EditDataTxtActivity,"Su contraseña fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                        Timer().schedule(1500){
                            FirebaseAuth.getInstance().signOut()
                            lifecycleScope.launch(Dispatchers.Main){
                                Intent(this@EditDataTxtActivity, MainActivity::class.java).apply {
                                    startActivity(this)
                                    finish()
                                }
                            }
                        }
                    }
                    upPas.addOnFailureListener {
                        Toast.makeText(this@EditDataTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                        Log.w("UpdateFirebaseError:", it.cause.toString())
                    }
                }
                reautenticar.addOnFailureListener {
                    Toast.makeText(this@EditDataTxtActivity, "Error: Su contraseña no pudo ser actualizada", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@EditDataTxtActivity,"Su respuesta fue actualizada satisfactoriamente",Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataTxtActivity,"Error: Su respuesta no pudo ser actualizada, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su respuesta no pudo ser actualizada",Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@EditDataTxtActivity, "Su telefono fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataTxtActivity,"Error: Su telefono no pudo ser actualizado, proceso fallido",Toast.LENGTH_SHORT).show()
                                    }
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity,"Error: Su telefono no pudo ser actualizado",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            updTel.await()
        }
    }

    private fun transFecha(valor: Int):String {
        if(valor < 10){
            return "0$valor"
        }else{
            return valor.toString()
        }
    }

    private fun actNomSis(nNombre: String, sisKey: String, usuario: String){
        // Creando la fecha del cambio
        val calendar = Calendar.getInstance()
        val dia = calendar.get(Calendar.DAY_OF_MONTH); val mes = calendar.get(Calendar.MONTH) + 1; val year = calendar.get(Calendar.YEAR)
        val hora = calendar.get(Calendar.HOUR_OF_DAY); val minuto = calendar.get(Calendar.MINUTE)
        val fechaCambio = "${transFecha(dia)}/${transFecha(mes)}/${transFecha(year)} ${transFecha(hora)}:${transFecha(minuto)}"

        lifecycleScope.launch(Dispatchers.IO){
            val upNomSis = async {
                // Accediendo a la ruta de cambio y estableciendo los nuevos valores
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children){
                            if(objSis.key.toString() == sisKey){
                                objSis.ref.child("ulti_Cam_Fecha").setValue(fechaCambio)
                                objSis.ref.child("ulti_Cam_User").setValue(usuario)
                                objSis.ref.child("nombre_Sis").setValue(nNombre).addOnSuccessListener {
                                    Toast.makeText(this@EditDataTxtActivity, "Su nombre fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                            }
                            break
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataTxtActivity, "Error: El cambio solicitado no se pudo realizar", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            upNomSis.await()
        }
    }
}