package com.ardusec.ardu_security.user

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.EditDataSpActivity
import com.ardusec.ardu_security.EditDataTxtActivity
import com.ardusec.ardu_security.MainActivity
import com.ardusec.ardu_security.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
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

class UserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var linLayBotones: LinearLayout
    private lateinit var lblNom: TextView
    private lateinit var lblUser: TextView
    private lateinit var btnEditNom: ImageButton
    private lateinit var btnEditEma: ImageButton
    private lateinit var btnEditPass: ImageButton
    private lateinit var btnEditPreg: ImageButton
    private lateinit var btnEditResp: ImageButton
    private lateinit var btnEditSis: ImageButton
    private lateinit var btnEditUsNom: ImageButton
    private lateinit var btnDelUsAcc: ImageButton
    private lateinit var linLayTel: LinearLayout
    private lateinit var btnEditTel: ImageButton
    // Formulario de confirmacion de eliminacion de cuenta
    private lateinit var mateLayConfEli: MaterialCardView
    private lateinit var lblConfPregEli: TextView
    private lateinit var txtConfRespEli: TextInputEditText
    private lateinit var linLayEmaConf: LinearLayout
    private lateinit var txtConfEmaEli: TextInputEditText
    private lateinit var txtConfPassEli: TextInputEditText
    private lateinit var btnConfEli: Button
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
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
        setContentView(R.layout.user_activity_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))
        //Obteniendo el usuario
        if(intent.extras == null){
            Toast.makeText(this@UserActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
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
        title = "Editar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        linLayBotones = findViewById(R.id.linLayUserBtns)
        lblNom = findViewById(R.id.lblNomVal)
        lblUser = findViewById(R.id.lblUserVal)
        btnEditNom = findViewById(R.id.btnEditNam)
        btnEditEma = findViewById(R.id.btnEditCorr)
        btnEditPass = findViewById(R.id.btnEditContra)
        btnEditPreg = findViewById(R.id.btnEditPreg)
        btnEditResp = findViewById(R.id.btnEditResp)
        btnEditSis = findViewById(R.id.btnEditSisRel)
        btnEditUsNom = findViewById(R.id.btnEditUsNam)
        btnDelUsAcc = findViewById(R.id.btnDelAcco)
        linLayTel = findViewById(R.id.linLayBtnEditTel)
        btnEditTel = findViewById(R.id.btnEditTel)
        // Elementos del formulario de confirmacion para la eliminacion
        mateLayConfEli = findViewById(R.id.mateCardConfEli)
        lblConfPregEli = findViewById(R.id.lblPregConfEli)
        txtConfRespEli = findViewById(R.id.txtConfRespEli)
        linLayEmaConf = findViewById(R.id.linLayConfEmailEli)
        txtConfEmaEli = findViewById(R.id.txtConfEmailEli)
        txtConfPassEli = findViewById(R.id.txtConfPassEli)
        btnConfEli = findViewById(R.id.btnConfEliCuen)

        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Establecer los valores a mostrar en la pantalla con respecto al usuario
        setFormulario()
    }
    private fun setFormulario() {
        lifecycleScope.launch(Dispatchers.IO){
            val getVals = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUs in dataSnapshot.children){
                            if(objUs.key.toString() == user) {
                                lblNom.text = objUs.child("nombre").value.toString()
                                lblUser.text = objUs.child("username").value.toString()
                                if(objUs.child("tipo_Usuario").value.toString() == "Administrador")
                                    linLayTel.isGone = false
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity, "Error: Datos no obtenidos", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getVals.await()
        }
    }

    private fun setFormularioConf(){
        lifecycleScope.launch(Dispatchers.IO) {
            // Obteniendo la pregunta del usuario por si desea eliminar su cuenta
            val getPreg = async {
                ref = database.getReference("Preguntas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objPreg in dataSnapshot.children) {
                            val refArrUs = objPreg.child("usuarios").ref
                            refArrUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot1: DataSnapshot) {
                                    for(objUs in snapshot1.children){
                                        if(objUs.key.toString() == user) {
                                            lblConfPregEli.text = objPreg.child("Val_Pregunta").value.toString()
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@UserActivity, "Error: Datos no obtenidos", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity, "Error: Datos no obtenidos", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getPreg.await()
            // Si se tiene como proveedor de acceso el correo, se mostraran los campos de confirmacion para ingresar correo
            val getProvAcc = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == user && objUser.child("accesos").child("correo").value != ""){
                                linLayEmaConf.isGone = false
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity, "Error: Datos no obtenidos", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getProvAcc.await()
        }
    }

    private fun addListeners(){
        // Toda la edicion de campos se lanzara hacia la misma actividad,
        // solo que dependera del campo a editar, los valores que seran mostrados
        btnEditNom.setOnClickListener {
            val editNom = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Nombre")
            }
            startActivity(editNom)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditEma.setOnClickListener {
            val editEma = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Correo")
            }
            startActivity(editEma)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditPass.setOnClickListener {
            val editPass = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Contraseña")
            }
            startActivity(editPass)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditPreg.setOnClickListener {
            val editPreg = Intent(this@UserActivity, EditDataSpActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Pregunta")
            }
            startActivity(editPreg)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditResp.setOnClickListener {
            val editResp = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Respuesta")
            }
            startActivity(editResp)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditSis.setOnClickListener {
            val editSis = Intent(this@UserActivity, EditDataSpActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Sistema")
            }
            startActivity(editSis)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditUsNom.setOnClickListener {
            val editUser = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Username")
            }
            startActivity(editUser)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnEditTel.setOnClickListener {
            val editTel = Intent(this@UserActivity, EditDataTxtActivity::class.java).apply {
                putExtra("usuario", user)
                putExtra("sistema", sistema)
                putExtra("campo", "Telefono")
            }
            startActivity(editTel)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnDelUsAcc.setOnClickListener {
            val avisoEliminacion = AlertDialog.Builder(this)
            avisoEliminacion.setTitle("Aviso")
            avisoEliminacion.setMessage("Al presionar el boton Aceptar sus datos serán borrados por completo y para acceder nuevamente deberá generar una cuenta nueva" +
                    "\n\n¿Esta totalmente seguro de proceder?")
            avisoEliminacion.setPositiveButton("Aceptar") { _, _ ->
                // Establecer elementos de interaccion en el formulario de confirmacion y mostrarlo
                setFormularioConf()
                mateLayConfEli.isGone = false
                linLayBotones.isGone = true
            }
            avisoEliminacion.setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.cancel()
            }
            val popUpEliminacion: AlertDialog = avisoEliminacion.create()
            popUpEliminacion.show()
        }
        btnConfEli.setOnClickListener {
            // Averiguar si se confirmará con correo la eliminacion
            if(!linLayEmaConf.isGone){
                delAccEma()
            }else{
                delAccGoo()
            }
        }
    }

    private fun avisoEli(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun validarResp(respuesta: Editable): Boolean{
        return if(TextUtils.isEmpty(respuesta)){
            avisoEli("Error: Favor de introducir una respuesta a su pregunta")
            false
        }else{
            true
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
                    avisoEli("Error: Favor de introducir un correo")
                }
                txtConfEmaEli.text!!.clear()
                return false
            }
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: Favor de introducir un correo valido")
                }
                txtConfEmaEli.text!!.clear()
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
                    avisoEli("Error: Favor de introducir una contraseña")
                }
                txtConfPassEli.text!!.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener una extension minima de 8 caracteres")
                }
                txtConfPassEli.text!!.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener al menos una letra mayuscula")
                }
                txtConfPassEli.text!!.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener al menos un numero")
                }
                txtConfPassEli.text!!.clear()
                return false
            }
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: Favor de incluir al menos un caracter especial en su contraseña")
                }
                txtConfPassEli.text!!.clear()
                return false
            }
            else -> { return true }
        }
    }

    private fun delAccEma(){
        // Si los 3 campos fueron validados correctamente, se procede con la comparacion de valores para la eliminacion
        if(validarResp(txtConfRespEli.text!!) && validarCorreo(txtConfEmaEli.text!!) && validarContra(txtConfPassEli.text!!)){
            lifecycleScope.launch(Dispatchers.IO){
                val cmpInfo = async {
                    ref = database.getReference("Usuarios")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for(objUser in dataSnapshot.children){
                                if(objUser.key.toString() == user){
                                    // Una vez encontrado el usuario se comparará la respuesta y el correo que se tiene
                                    val emaFire = objUser.child("accesos").child("correo").value.toString()
                                    val respFire = objUser.child("resp_Seguri").value.toString()
                                    if(emaFire == txtConfEmaEli.text!!.toString() && respFire == txtConfRespEli.text!!.toString()){
                                        //Primero se eliminará la informacion del usuario en firebase y luego el acceso de auth
                                        delUserInfoFire()
                                        delUserEmail()
                                    }else{
                                        avisoEli("Error: La informacion ingresada en la confirmación, no coincide con los registros")
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                cmpInfo.await()
            }
        }
    }

    private fun delAccGoo(){
        // En el caso de google, solo se podra validar primero la respuesta y se debera hacer todo el proceso de eliminacion en la activity result
        if(validarResp(txtConfRespEli.text!!)){
            accederGoogle()
        }
    }

    private fun delUserInfoFire(){
        lifecycleScope.launch(Dispatchers.IO){
            // Eliminacion de informacion, parte 1: Relacion con preguntas
            val delUsPreg = async {
                ref = database.getReference("Preguntas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objPregs in dataSnapshot.children){
                            val refPregUs = objPregs.child("usuarios").ref
                            refPregUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot1: DataSnapshot) {
                                    for(objUs in snapshot1.children){
                                        if(objUs.key.toString() == user){
                                            objUs.ref.removeValue()
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delUsPreg.await()
            // Eliminacion de informacion, parte 2: Relacion con sistemas
            val delUsSis = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children){
                            val refSisUs = objSis.child("usuarios").ref
                            refSisUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot1: DataSnapshot) {
                                    for(objUs in snapshot1.children){
                                        if(objUs.key.toString() == user){
                                            objUs.ref.removeValue()
                                            break
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delUsSis.await()
            // Eliminacion de informacion, parte 3: Entidad Usuarios
            val delUser = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == user){
                                objUser.ref.removeValue()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delUser.await()
        }
    }

    // Proceso de correo y contraseña
    private fun delUserEmail(){
        lifecycleScope.launch(Dispatchers.IO) {
            val delCorreo = async {
                val userAuth = auth.currentUser!!
                // Para poder eliminar el usuario, es necesario renovar las credenciales de acceso
                val credencial = EmailAuthProvider.getCredential(txtConfEmaEli.text.toString(), txtConfPassEli.text.toString())
                val reautenticar = userAuth.reauthenticate(credencial)
                reautenticar.addOnSuccessListener {
                    // Cuando se reautentique el usuario se eliminara el valor en auth
                    val eliminar = userAuth.delete()
                    eliminar.addOnSuccessListener {
                        lifecycleScope.launch(Dispatchers.Main){
                            Toast.makeText(this@UserActivity,"El usuario $user fue eliminado satifactoriamente",Toast.LENGTH_LONG).show()
                            chgPanta()
                        }
                        Timer().schedule(1500){
                            lifecycleScope.launch(Dispatchers.Main){
                                Intent(this@UserActivity, MainActivity::class.java).apply {
                                    startActivity(this)
                                    finish()
                                }
                            }
                        }
                    }
                    eliminar.addOnFailureListener {
                        Toast.makeText(this@UserActivity,"El usuario no pudo ser eliminado, proceso incompleto",Toast.LENGTH_SHORT).show()
                    }
                }
                reautenticar.addOnFailureListener {
                    Toast.makeText(this@UserActivity,"El usuario no pudo ser reautenticado",Toast.LENGTH_SHORT).show()
                }
            }
            delCorreo.await()
        }
    }

    // Proceso de google
    private fun accederGoogle(){
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@UserActivity, googleConf)
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
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val userAuth = auth.currentUser!!
            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                // Comparando la informacion obtenida contra la almacenada
                lifecycleScope.launch(Dispatchers.IO){
                    val cmpInfo = async {
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUser in dataSnapshot.children){
                                    if(objUser.key.toString() == user){
                                        // Una vez encontrado el usuario se comparará la respuesta y el correo que se tiene
                                        val gooFire = objUser.child("accesos").child("google").value.toString()
                                        val respFire = objUser.child("resp_Seguri").value.toString()
                                        if(gooFire == cuenta.email && respFire == txtConfRespEli.text!!.toString()){
                                            //Primero se eliminará la informacion del usuario en firebase y luego el acceso de auth
                                            delUserInfoFire()
                                            val reautenticar = userAuth.reauthenticate(credencial)
                                            reautenticar.addOnSuccessListener {
                                                // Cuando se reautentique el usuario se eliminara el valor en auth y en este caso se saldra de la sesion de Google solicitada
                                                val eliminar = userAuth.delete()
                                                googleCli.signOut()
                                                eliminar.addOnSuccessListener {
                                                    lifecycleScope.launch(Dispatchers.Main){
                                                        Toast.makeText(this@UserActivity,"El usuario $user fue eliminado satifactoriamente",Toast.LENGTH_LONG).show()
                                                        chgPanta()
                                                    }
                                                    Timer().schedule(1500){
                                                        lifecycleScope.launch(Dispatchers.Main){
                                                            Intent(this@UserActivity, MainActivity::class.java).apply {
                                                                startActivity(this)
                                                                finish()
                                                            }
                                                        }
                                                    }
                                                }
                                                eliminar.addOnFailureListener {
                                                    Toast.makeText(this@UserActivity,"El usuario no pudo ser eliminado, proceso incompleto",Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            reautenticar.addOnFailureListener {
                                                Toast.makeText(this@UserActivity,"El usuario no pudo ser reautenticado",Toast.LENGTH_SHORT).show()
                                            }
                                        }else{
                                            avisoEli("Error: La informacion ingresada en la confirmación, no coincide con los registros")
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    cmpInfo.await()
                }
            }catch (error: ApiException){
                avisoEli("Error: No se pudo eliminar la informacion solicitada")
            }
        }
    }

    private fun chgPanta(){
        val builder = AlertDialog.Builder(this@UserActivity).create()
        val view = layoutInflater.inflate(R.layout.charge_transition,null)
        builder.setView(view)
        builder.show()
        Timer().schedule(2000){
            builder.dismiss()
        }
    }
}