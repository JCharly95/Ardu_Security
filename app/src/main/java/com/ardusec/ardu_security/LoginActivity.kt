package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.wifi.hotspot2.pps.Credential
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var txtUser: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var chbVerContra: CheckBox
    private lateinit var btnLostPass: Button
    private lateinit var btnSelAccEma: Button
    private lateinit var btnSelAccGoo: Button
    private lateinit var btnAcc: Button
    private lateinit var btnRegister: Button
    private lateinit var btnAyuda: Button
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
    private lateinit var linLayEma: LinearLayout
    // ID del acceso de google
    private val GoogleAcces = 195

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Iniciar Sesion"
        // Relacionando los elementos con su objeto de la interfaz
        txtUser = findViewById(R.id.txtUsername)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtPass)
        chbVerContra = findViewById(R.id.chbPass)
        btnLostPass = findViewById(R.id.btnLstContra)
        btnSelAccEma = findViewById(R.id.btnAccUsPass)
        btnSelAccGoo = findViewById(R.id.btnAccGoogle)
        btnAcc = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegistro)
        //btnAccGo = findViewById(R.id.btnRegGoo)
        btnAyuda = findViewById(R.id.btnInfo)
        linLayEma = findViewById(R.id.LinLaLogEma)
    }

    private fun aviso(){
        val mensaje = "Consideraciones de campos: \n\n" +
                "Correo; Formatos Aceptados (Ejemplos):\n" +
                "* usuario@dominio.com\n" +
                "* usuario@dominio.com.mx\n\n" +
                "Contraseña (Condiciones):\n" +
                "* Extension minima de 8 caracteres\n" +
                "* Incluir al menos una mayuscula\n" +
                "* Incluir al menos un numero\n" +
                "* Incluir al menos  un caracter especial"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        // Agregar los listener de los botones
        btnAyuda.setOnClickListener {
            aviso()
        }
        btnSelAccEma.setOnClickListener {
            linLayEma.isGone = !btnSelAccEma.isGone
        }
        btnLostPass.setOnClickListener{
            val intentLost = Intent(this,ResetPassActivity::class.java)
            startActivity(intentLost)
        }
        chbVerContra.setOnClickListener{
            if(!chbVerContra.isChecked){
                txtContra.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnRegister.setOnClickListener{
            val intentRegister = Intent(this,RegisterActivity::class.java)
            startActivity(intentRegister)
        }
        btnAcc.setOnClickListener{
            lifecycleScope.launch(Dispatchers.IO) {
                val accProc = async {
                    val user = txtUser.text.toString()
                    val correo = txtEmail.text.toString()
                    val contra = txtContra.text.toString()

                    if(correo.isNotEmpty() && contra.isNotEmpty()){
                        if(validarCorreo(correo) && validarContra(contra)){
                            val credencial = EmailAuthProvider.getCredential(correo, contra)
                            acceder(credencial)
                        }
                    }else{
                        if(correo.isEmpty() && contra.isEmpty()){
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@LoginActivity, "Error: Favor de ingresar sus datos", Toast.LENGTH_SHORT).show()
                            }
                        }else if (correo.isEmpty()){
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@LoginActivity, "Error: Favor de ingresar su correo", Toast.LENGTH_SHORT).show()
                            }
                        }else if (contra.isEmpty()){
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@LoginActivity, "Error: Favor de ingresar su contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                accProc.await()
            }
        }

        btnSelAccGoo.setOnClickListener {
            if(txtUser.text.toString().isNotEmpty()){
                crearPeticionGoogle()
                signInGoo()
            }else{
                Toast.makeText(this@LoginActivity, "Error: El usuario no ha sido ingresado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearPeticionGoogle(){
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@LoginActivity, googleConf)
    }

    private suspend fun validarCorreo(correo: String): Boolean {
        // Si se detectan espacios en el correo, estos seran removidos
        if(Regex("""\s+""").containsMatchIn(correo)){
            val correoFil = correo.replace("\\s".toRegex(), "")
            when{
                correoFil.isEmpty() -> { // Si el correo esta vacio despues de quitar todos los espacios del campo
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su correo no puede ser solamente espacios", Toast.LENGTH_SHORT).show()
                    }
                }
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil).matches() -> {
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: El correo ingresado no tiene una estructura valida", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    return true
                }
            }
        }else{
            when{
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: El correo ingresado no tiene una estructura valida", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    return true
                }
            }
        }
        return false
    }

    private suspend fun validarContra(contra: String): Boolean {
        // Si se detectan espacios en la contraseña, estos seran removidos
        if(Regex("""\s+""").containsMatchIn(contra)) {
            val contraFil = contra.replace("\\s".toRegex(), "")
            when {
                contraFil.isEmpty() -> { // Si la contraseña esta vacia despues de quitar todos los espacios
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no puede ser solamente espacios", Toast.LENGTH_SHORT).show()
                    }
                }
                (contraFil.length < 8) -> { // Extension minima de 8 caracteres
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña debe ser de minimo 8 caracteres", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("[A-Z]+").containsMatchIn(contraFil)) -> { // No se tiene al menos una mayuscula
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("""\d""").containsMatchIn(contraFil)) -> { // No se tiene al menos un numero
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos un numero", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil)) -> { // No se tiene al menos un caracter especial
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos un caracter especial", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    return true
                }
            }
        }else{
            when {
                (contra.length < 8) -> { // Extension minima de 8 caracteres
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña debe ser de minimo 8 caracteres", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("[A-Z]+").containsMatchIn(contra)) -> { // No se tiene al menos una mayuscula
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("""\d""").containsMatchIn(contra)) -> { // No se tiene al menos un numero
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos un numero", Toast.LENGTH_SHORT).show()
                    }
                }
                (!Regex("""[^A-Za-z ]+""").containsMatchIn(contra)) -> { // No se tiene al menos un caracter especial
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@LoginActivity, "Error: Su contraseña no contiene al menos un caracter especial", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    return true
                }
            }
        }
        return false
    }

    private fun signInGoo() {
        // Obteniendo el intent de google
        val intentGoo = googleCli.signInIntent
        // Implementando el launcher result posterior al haber obtenido el intent de google
        startActivityForResult(intentGoo, GoogleAcces)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si el codigo de respuesta es el mismo que se planteo para el login de google, se procede con la preparacion del cliente google
        if (requestCode == GoogleAcces) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                acceder(credencial)

                // Accediendo con los datos de la cuenta de google
                FirebaseAuth.getInstance().signInWithCredential(credencial).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                        val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intentoDash)
                    }else{
                        // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                        Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                        txtEmail.text.clear()
                        txtContra.text.clear()
                    }
                }
            }catch (error: ApiException){
                // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                txtEmail.text.clear()
                txtContra.text.clear()
            }
        }
    }

    private fun acceder(credencial: AuthCredential) {
        lifecycleScope.launch(Dispatchers.IO) {
            val acceso = async {
                data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String)
                // Se crea una instancia de FirebaseAuth (Autenticacion y se inicia sesion/loguea)
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(txtEmail.toString(), txtContra.toString())
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            // Si el usuario accedio satisfactoriamente, se le envia hacia el dashboard y se accede a firebase para mostrar su nombre
                            val refDB = Firebase.database.getReference("Usuarios")
                            refDB.addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(dataSnapshot: DataSnapshot){
                                    for (objUs in dataSnapshot.children){
                                        val gson = Gson()
                                        val userJSON = gson.toJson(objUs.value)
                                        val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                        val nombre = resUser.nombre
                                        Toast.makeText(this@LoginActivity, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                                        val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java)
                                        startActivity(intentoDash)
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                                }
                            })
                        }else{
                            // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                            Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                            txtEmail.text.clear()
                            txtContra.text.clear()
                        }
                    }
            }
            acceso.await()
        }
    }
}