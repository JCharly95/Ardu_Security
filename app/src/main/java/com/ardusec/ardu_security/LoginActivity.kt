package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.user.DashboardActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var rbSelAccEma: RadioButton
    private lateinit var rbSelAccGoo: RadioButton
    private lateinit var linLayEma: LinearLayout
    private lateinit var txtUser: TextInputEditText
    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtContra: TextInputEditText
    private lateinit var btnAccEma: Button
    private lateinit var btnAccGoo: Button
    private lateinit var btnRegister: Button
    private lateinit var btnLostPass: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Variables de acceso para google
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
    private val GoogleAcces = 195

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Configurando el backPressed
        onBackPressedDispatcher.addCallback(this, finApp)
    }

    private val finApp = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAffinity();
        }
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Iniciar Sesion"
        // Relacionando los elementos con su objeto de la interfaz
        btnAyuda = findViewById(R.id.btnInfoLog)
        rbSelAccEma = findViewById(R.id.rbSelEma)
        rbSelAccGoo = findViewById(R.id.rbSelGoo)
        linLayEma = findViewById(R.id.LLLogEma)
        txtUser = findViewById(R.id.txtUserLogin)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtPass)
        //chbVerPass = findViewById(R.id.chbPass)
        btnAccEma = findViewById(R.id.btnLogEma)
        btnAccGoo = findViewById(R.id.btnLogGoo)
        btnRegister = findViewById(R.id.btnRegistro)
        btnLostPass = findViewById(R.id.btnLstContra)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()
    }

    private fun addListeners(){
        // Agregar los listener de los botones
        btnAyuda.setOnClickListener {
            val msg = "Requisitos necesarios de cada campo: \n\n" +
                "Dirección de Correo:\n" +
                "* La estructura aceptada es:\n" +
                "-- usuario@dominio (.extensión de país; esto es opcional ingresarlo solo si su dirección lo contiene)\n\n" +
                "Contraseña:\n" +
                "* Debe tener una extensión mínima de 8 caracteres.\n" +
                "* Debe tener por lo menos una letra mayúscula.\n" +
                "* Debe tener por lo menos el digito de un número.\n" +
                "* Debe tener por lo menos un carácter especial.\n"
            avisoLog(msg)
        }
        rbSelAccEma.setOnClickListener {
            if(rbSelAccEma.isChecked){
                txtUser.isGone = false
                linLayEma.isGone = false
                btnAccGoo.isGone = true
            }else
                linLayEma.isGone = true
        }
        rbSelAccGoo.setOnClickListener {
            if(rbSelAccGoo.isChecked){
                txtUser.isGone = false
                btnAccGoo.isGone = false
                linLayEma.isGone = true
            }else{
                linLayEma.isGone = true
                btnAccGoo.isGone = true
            }
        }
        btnAccEma.setOnClickListener{
            // Validacion individual de los campos
            if(validarUsuario(txtUser.text!!) && validarCorreo(txtEmail.text!!) && validarContra(txtContra.text!!))
                accederCorreo()
        }
        btnAccGoo.setOnClickListener {
            // Validacion individual de los campos
            if(validarUsuario(txtUser.text!!))
                accederGoogle()
        }
        btnRegister.setOnClickListener{
            val intentRegister = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intentRegister)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnLostPass.setOnClickListener{
            val intentLost = Intent(this@LoginActivity,ResetPassActivity::class.java)
            startActivity(intentLost)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun avisoLog(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun validarUsuario(usuario: Editable): Boolean{
        // Si se detectan espacios en blanco en el usuario, seran removidos
        val usuarioFil1 = usuario.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val usuarioFil2 = usuarioFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el usuario esta vacia
            TextUtils.isEmpty(usuarioFil2) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: Favor de introducir un nombre de usuario")
                }
                txtUser.text!!.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
                }
                txtUser.text!!.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: El nombre de usuario debera tener al menos una letra mayuscula")
                }
                txtUser.text!!.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: El nombre de usuario debera tener al menos un numero")
                }
                txtUser.text!!.clear()
                return false
            }
            else -> { return true }
        }
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        val correoFil1 = correo.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val correoFil2 = correoFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el correo esta vacio
            TextUtils.isEmpty(correoFil2) ->{
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: Favor de introducir un correo")
                }
                txtEmail.text!!.clear()
                return false
            }
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: Favor de introducir un correo valido")
                }
                txtEmail.text!!.clear()
                return false
            }
            else -> { return true }
        }
    }
    private fun validarContra(contra: Editable): Boolean{
        // Si se detectan espacios en la contraseña, estos seran removidos
        val contraFil1 = contra.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val contraFil2 = contraFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si la contraseña esta vacia
            TextUtils.isEmpty(contraFil2) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: Favor de introducir una contraseña")
                }
                txtContra.text!!.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: La contraseña debera tener una extension minima de 8 caracteres")
                }
                txtContra.text!!.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: La contraseña debera tener al menos una letra mayuscula")
                }
                txtContra.text!!.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: La contraseña debera tener al menos un numero")
                }
                txtContra.text!!.clear()
                return false
            }
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoLog("Error: Favor de incluir al menos un caracter especial en su contraseña")
                }
                txtContra.text!!.clear()
                return false
            }
            else -> { return true }
        }
    }

    private fun accederCorreo(){
        var busUser = false
        // Se busca al usuario en la BD para verificar que el correo que ingreso coincide con el que esta almacenado
        lifecycleScope.launch(Dispatchers.IO){
            val logEma = async {
                val usuario = txtUser.text!!.toString().trim()
                val emaLimp = txtEmail.text!!.toString().trim()
                val pasLimp = txtContra.text!!.toString().trim()
                // Accediendo a la entidad de los usuarios para buscar al usuario en cuestion
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if (objUser.key.toString() == usuario){
                                // Obtencion de la direccion de correo guardada en firebase y verificacion de igualdad con el ingresado
                                val correoFire = objUser.child("accesos").child("correo").value.toString()
                                if(correoFire == emaLimp){
                                    // Accediendo a la app usando el correo
                                    val loginEma = auth.signInWithEmailAndPassword(emaLimp,pasLimp)
                                    loginEma.addOnSuccessListener {
                                        val user = Firebase.auth.currentUser
                                        user?.let{
                                            Timer().schedule(1000) {
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    chgPanta()
                                                }
                                            }
                                            Timer().schedule(3000) {
                                                lifecycleScope.launch(Dispatchers.Main){
                                                    Toast.makeText(this@LoginActivity, "Bienvenido ${user.displayName}", Toast.LENGTH_SHORT).show()
                                                    val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                                        putExtra("username", usuario)
                                                    }
                                                    startActivity(intentoDash)
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                }
                                            }
                                        }
                                    }
                                    loginEma.addOnFailureListener {
                                        // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                                        lifecycleScope.launch(Dispatchers.Main){
                                            avisoLog("Error: No se pudo acceder con la informacion ingresada")
                                        }
                                        txtEmail.text!!.clear()
                                        txtContra.text!!.clear()
                                        txtUser.text!!.clear()
                                    }
                                }else{
                                    avisoLog("La dirección de correo ingresada no coincidió con la información del usuario")
                                }
                                busUser = true
                                break
                            }
                        }
                        if(!busUser){
                            // Si se llego a este punto, es porque no se encontro el usuario en la BD y por lo tanto no se va a proceder, mostrando mensaje y vaciando los campos
                            avisoLog("El usuario solicitado no fue encontrado, favor de revisar su informacion")
                            txtUser.text!!.clear()
                            txtEmail.text!!.clear()
                            txtContra.text!!.clear()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Error: Busqueda sin exito", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
            logEma.await()
        }
    }

    private fun accederGoogle(){
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@LoginActivity, googleConf)
        // Fin de crearPeticionGoogle() y preparar la peticion de google

        // Obteniendo el intent de google
        val intentGoo = googleCli.signInIntent
        // Implementando el launcher result posterior al haber obtenido el intent de google
        //startForResult.launch(intentGoo)
        startActivityForResult(intentGoo, GoogleAcces)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Si el codigo de respuesta es el mismo que se planteo para el login de google, se procede con la preparacion del cliente google
        if (requestCode == GoogleAcces) {
            val usuario = txtUser.text!!.toString().trim()
            val taskGoo = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = taskGoo.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                // Accediendo con los datos de la cuenta de google
                val loginGoo = auth.signInWithCredential(credencial)
                loginGoo.addOnSuccessListener {
                    val user = Firebase.auth.currentUser
                    user?.let{
                        // Buscando al usuario en la BD
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (objUser in dataSnapshot.children) {
                                    if (objUser.key.toString() == usuario){
                                        // Obtencion de la direccion de correo guardada en firebase y verificacion de igualdad con el obtenido en la peticion a google
                                        val googleFire = objUser.child("accesos").child("google").value.toString()
                                        if(googleFire == cuenta.email){
                                            // Nueva adicion, se agrego una nueva funcion de pantallas de carga y para eso se deja la pantalla por un segundo y luego se llama a la ventana de carga
                                            Timer().schedule(1000) {
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    chgPanta()
                                                }
                                            }
                                            Timer().schedule(3000) {
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    Toast.makeText(this@LoginActivity, "Bienvenido ${user.displayName}", Toast.LENGTH_SHORT).show()
                                                    val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                                        putExtra("username", usuario)
                                                    }
                                                    startActivity(intentoDash)
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                }
                                            }
                                        }else{
                                            avisoLog("La dirección de correo obtenida no coincidió con la información del usuario")
                                        }
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(this@LoginActivity, "Error: Busqueda sin exito", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }
                loginGoo.addOnFailureListener {
                    // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                        txtUser.text!!.clear()
                    }
                }
            }catch (error: ApiException){
                if(error.statusCode == 12501){
                    lifecycleScope.launch(Dispatchers.Main) {
                        avisoLog("Error: No se pudo acceder ya que no seleccionó una cuenta, favor de intentarlo nuevamente")
                    }
                }else{
                    lifecycleScope.launch(Dispatchers.Main) {
                        avisoLog("Error: No se pudo acceder con la informacion ingresada. Causa:\n${error.statusCode}")
                    }
                }
            }
        }
    }

    private fun chgPanta(){
        val builder = AlertDialog.Builder(this@LoginActivity).create()
        val view = layoutInflater.inflate(R.layout.charge_transition,null)
        builder.setView(view)
        builder.show()
        Timer().schedule(2000){
            builder.dismiss()
        }
    }
}