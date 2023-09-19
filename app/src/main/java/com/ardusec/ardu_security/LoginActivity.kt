package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.wifi.hotspot2.pps.Credential
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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

class LoginActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var rbSelAccEma: RadioButton
    private lateinit var rbSelAccGoo: RadioButton
    private lateinit var linLayEma: LinearLayout
    private lateinit var txtUser: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var chbVerPass: CheckBox
    private lateinit var btnAccEma: Button
    private lateinit var btnAccGoo: Button
    private lateinit var btnRegister: Button
    private lateinit var btnLostPass: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Banderas de validacion
    private var valiUser = false
    private var valiCorr = false
    private var valiPass = false
    // Variables de acceso para google
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
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
        btnAyuda = findViewById(R.id.btnInfoLog)
        rbSelAccEma = findViewById(R.id.rbSelEma)
        rbSelAccGoo = findViewById(R.id.rbSelGoo)
        linLayEma = findViewById(R.id.LLLogEma)
        txtUser = findViewById(R.id.txtUserLogin)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtPass)
        chbVerPass = findViewById(R.id.chbPass)
        btnAccEma = findViewById(R.id.btnLogEma)
        btnAccGoo = findViewById(R.id.btnLogGoo)
        btnRegister = findViewById(R.id.btnRegistro)
        btnLostPass = findViewById(R.id.btnLstContra)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
    }

    private fun addListeners(){
        val msg = "Consideraciones de campos: \n\n" +
                "Correo; Formatos Aceptados (Ejemplos):\n" +
                "* usuario@dominio.com\n" +
                "* usuario@dominio.com.mx\n\n" +
                "Contraseña (Condiciones):\n" +
                "* Extension minima de 8 caracteres\n" +
                "* Incluir al menos una mayuscula\n" +
                "* Incluir al menos un numero\n" +
                "* Incluir al menos  un caracter especial"

        // Agregar los listener de los botones
        btnAyuda.setOnClickListener {
            avisoLog(msg)
        }
        rbSelAccEma.setOnClickListener {
            if(rbSelAccEma.isChecked){
                txtUser.isGone = false
                linLayEma.isGone = false
                btnAccGoo.isGone = true
            }else{
                linLayEma.isGone = true
            }
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
        chbVerPass.setOnClickListener{
            if(!chbVerPass.isChecked){
                txtContra.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnAccEma.setOnClickListener{
            buscarUsBD()
        }
        btnAccGoo.setOnClickListener {
            buscarUsBD()
        }
        btnRegister.setOnClickListener{
            val intentRegister = Intent(this@LoginActivity,RegisterActivity::class.java)
            startActivity(intentRegister)
        }
        btnLostPass.setOnClickListener{
            val intentLost = Intent(this@LoginActivity,ResetPassActivity::class.java)
            startActivity(intentLost)
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
            TextUtils.isEmpty(usuarioFil2) -> avisoLog("Error: Favor de introducir un nombre de usuario")
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> avisoLog("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> avisoLog("Error: El nombre de usuario debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> avisoLog("Error: El nombre de usuario debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(usuarioFil2)) -> avisoLog("Error: Favor de incluir al menos un caracter especial en su nombre de usuario")
            else -> return true
        }
        return false
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        val correoFil1 = correo.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val correoFil2 = correoFil1.replace("\\p{Zs}+".toRegex(), "")
        when{
            // Si el correo esta vacio
            TextUtils.isEmpty(correoFil2) -> avisoLog("Error: Favor de introducir un correo")
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> avisoLog("Error: Favor de introducir un correo valido")
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
            TextUtils.isEmpty(contraFil2) -> avisoLog("Error: Favor de introducir una contraseña")
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> avisoLog("Error: La contraseña debera tener una extension minima de 8 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> avisoLog("Error: La contraseña debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> avisoLog("Error: La contraseña debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> avisoLog("Error: Favor de incluir al menos un caracter especial en su contraseña")
            else -> return true
        }
        return false
    }

    private fun buscarUsBD(){
        lifecycleScope.launch(Dispatchers.IO){
            val busUser = async {
                valiUser = validarUsuario(txtUser.text)
                // Preparando variables para la obtencion de valores de los campos
                val usuario = txtUser.text.toString().trim()

                if(valiUser){
                    // Buscando al usuario en la BD
                    ref = database.getReference("Usuarios")
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objUser in dataSnapshot.children) {
                                if (objUser.key.toString() == usuario){
                                    if(rbSelAccEma.isChecked) {
                                        valiLogEmail(objUser.child("tipo_Usuario").value.toString())
                                    }else if(rbSelAccGoo.isChecked) {
                                        valiLogGoo()
                                    }
                                    break
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(this@LoginActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }else{
                    avisoLog("El username no pudo ser validado correctamente")
                }
            }
            busUser.await()
        }
    }

    private fun valiLogEmail(tipo: String){
        lifecycleScope.launch(Dispatchers.IO){
            val logEma = async {
                // Validacion individual de los campos (para este punto ya se valido el username previamente)
                valiCorr = validarCorreo(txtEmail.text)
                valiPass = validarContra(txtContra.text)

                if(valiCorr && valiPass){
                    val usuario = txtUser.text.toString().trim()
                    val emaLimp = txtEmail.text.toString().trim()
                    val pasLimp = txtContra.text.toString().trim()
                    // Accediendo a la ppa usando el correo
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(emaLimp,pasLimp).addOnCompleteListener {task ->
                        if(task.isSuccessful){
                            val user = Firebase.auth.currentUser
                            user?.let{
                                lifecycleScope.launch(Dispatchers.Main){
                                    Toast.makeText(this@LoginActivity, "Bienvenido ${user.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                    putExtra("username", usuario)
                                    putExtra("tipo", tipo)
                                }
                                startActivity(intentoDash)
                            }
                        }else{
                            // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                            lifecycleScope.launch(Dispatchers.Main){
                                Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                            }
                            txtEmail.text.clear()
                            txtContra.text.clear()
                            txtUser.text.clear()
                        }
                    }
                }else if(!valiCorr){
                    avisoLog("El correo ingresado no cumplio todos los puntos de validacion, favor de verificar su informacion")
                }else{
                    avisoLog("Su contraseña no cumplio todos los puntos de validacion, favor de verificar su informacion")
                }
            }
            logEma.await()
        }
    }

    private fun valiLogGoo(){
        crearPeticionGoogle()
        loginGoo()
    }

    private fun crearPeticionGoogle(){
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@LoginActivity, googleConf)
        // Fin de crearPeticionGoogle() y preparar la peticion de google
    }

    private fun loginGoo() {
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
            val usuario = txtUser.text.toString().trim()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                // Accediendo con los datos de la cuenta de google
                FirebaseAuth.getInstance().signInWithCredential(credencial).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val user = Firebase.auth.currentUser
                        user?.let{
                            // Buscando al usuario en la BD
                            ref = database.getReference("Usuarios")
                            ref.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (objUser in dataSnapshot.children) {
                                        if (objUser.key.toString() == usuario){
                                            Toast.makeText(this@LoginActivity, "Bienvenido ${user.displayName}", Toast.LENGTH_SHORT).show()
                                            val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                                                putExtra("username", usuario)
                                                putExtra("tipo", objUser.child("tipo_Usuario").value.toString())
                                            }
                                            startActivity(intentoDash)
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@LoginActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }else{
                        // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                        Toast.makeText(this@LoginActivity, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                        txtUser.text.clear()
                    }
                }
            }catch (error: ApiException){
                avisoLog("Error: No se pudo acceder con la informacion ingresada; informacion ${error}")
            }
        }
    }
}