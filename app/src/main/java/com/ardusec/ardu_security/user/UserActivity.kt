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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var txtConfRespEli: EditText
    private lateinit var linLayEmaConf: LinearLayout
    private lateinit var txtConfEmaEli: EditText
    private lateinit var txtConfPassEli: EditText
    private lateinit var chbConfChgEli: CheckBox
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
        chbConfChgEli = findViewById(R.id.chbConfPassEli)
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
            // Preparar la peticion de google por si se usa el correo de google
            crearPeticionGoogle()
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
        chbConfChgEli.setOnClickListener {
            if(!chbConfChgEli.isChecked){
                txtConfPassEli.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtConfPassEli.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
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
                // Determinar con que accesos cuenta el usuario
                obteAccesos()
            }
            avisoEliminacion.setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.cancel()
            }
            val popUpEliminacion: AlertDialog = avisoEliminacion.create()
            popUpEliminacion.show()
        }
        btnConfEli.setOnClickListener {
            eliAccesos()
        }
    }

    private fun obteAccesos(){
        lifecycleScope.launch(Dispatchers.IO){
            val getAccesos = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == user){
                                val refAccUs = objUser.ref.child("accesos")
                                refAccUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        // Si se contempla algun caso en el que se cuente con correo, se necesitara el
                                        if(snapshot.child("correo").value.toString() != ""){
                                            linLayEmaConf.isGone = false
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getAccesos.await()
        }
    }

    private fun eliAccesos(){
        lifecycleScope.launch(Dispatchers.IO){
            val getAccesos = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == user){
                                val refAccUs = objUser.ref.child("accesos")
                                refAccUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                        // Si los 2 accesos posibles no estan vacios, se invocaran ambos metodos de eliminacion en auth
                                        if(snapshot1.child("correo").value.toString() != "" && snapshot1.child("google").value.toString() != ""){
                                            // Para el caso del correo es necesario validar la informacion ingresada en los campos de confirmacion
                                            if(validarCorreo(txtConfEmaEli.text) && validarContra(txtConfPassEli.text)){
                                                delEmail()
                                            }
                                            delGoogle()
                                            // En cualquiera de los casos, se debera eliminar la informacion de Firebase
                                            eliminarInfoFire()
                                        }
                                        // EL caso de haber correo pero no google
                                        if(snapshot1.child("correo").value.toString() != "" && snapshot1.child("google").value.toString() == ""){
                                            // Para el caso del correo es necesario validar la informacion ingresada en los campos de confirmacion
                                            if(validarCorreo(txtConfEmaEli.text) && validarContra(txtConfPassEli.text)){
                                                delEmail()
                                            }
                                            // En cualquiera de los casos, se debera eliminar la informacion de Firebase
                                            eliminarInfoFire()
                                        }
                                        // En caso de haber google, pero no correo
                                        if(snapshot1.child("google").value.toString() != "" && snapshot1.child("correo").value.toString() == ""){
                                            delGoogle()
                                            // En cualquiera de los casos, se debera eliminar la informacion de Firebase
                                            eliminarInfoFire()
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getAccesos.await()
        }
    }

    private fun eliminarInfoFire(){
        lifecycleScope.launch(Dispatchers.IO){
            val delSisUs = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objUser in dataSnapshot.children){
                            if(objUser.key.toString() == user && objUser.child("sistema_Rel").value.toString() == sistema){
                                objUser.child("sistema_Rel").ref.setValue("User Not System")
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delSisUs.await()
            val delUsSis = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children){
                            if(objSis.key.toString() == sistema){
                                val refUsSis = objSis.ref.child("usuarios")
                                refUsSis.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(objUser in snapshot.children){
                                            if(objUser.key.toString() == user){
                                                val removeUser = objUser.ref.removeValue()
                                                removeUser.addOnSuccessListener {
                                                    lifecycleScope.launch(Dispatchers.Main){
                                                        val msg = "El usuario $user fue eliminado del sistema satifactoriamente"
                                                        avisoEli(msg)
                                                    }
                                                    Timer().schedule(1500){
                                                        lifecycleScope.launch(Dispatchers.Main){
                                                            Intent(this@UserActivity, MainActivity::class.java).apply {
                                                                startActivity(this)
                                                                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                                finish()
                                                            }
                                                        }
                                                    }
                                                }
                                                removeUser.addOnFailureListener {
                                                    Toast.makeText(this@UserActivity,"Error: No se pudo remover al usuario del sistema",Toast.LENGTH_SHORT).show()
                                                }
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delUsSis.await()
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
                txtConfEmaEli.text.clear()
                return false
            }
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: Favor de introducir un correo valido")
                }
                txtConfEmaEli.text.clear()
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
                txtConfPassEli.text.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener una extension minima de 8 caracteres")
                }
                txtConfPassEli.text.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener al menos una letra mayuscula")
                }
                txtConfPassEli.text.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: La contraseña debera tener al menos un numero")
                }
                txtConfPassEli.text.clear()
                return false
            }
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoEli("Error: Favor de incluir al menos un caracter especial en su contraseña")
                }
                txtConfPassEli.text.clear()
                return false
            }
            else -> { return true }
        }
    }

    // Proceso de correo y contraseña
    private fun delEmail(){
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
                        Toast.makeText(this@UserActivity,"Eliminación en proceso, aguarde...",Toast.LENGTH_SHORT).show()
                    }
                    eliminar.addOnFailureListener {
                        Toast.makeText(this@UserActivity,"Su informacion no pudo ser eliminada, proceso incompleto",Toast.LENGTH_SHORT).show()
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
    private fun crearPeticionGoogle() {
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@UserActivity, googleConf)
        // Fin de crearPeticionGoogle() y preparar la peticion de google
    }

    private fun delGoogle() {
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

            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                val userAuth = auth.currentUser!!
                lifecycleScope.launch(Dispatchers.IO) {
                    val updValsGoo = async {
                        val reautenticar = userAuth.reauthenticate(credencial)
                        reautenticar.addOnSuccessListener {
                            Toast.makeText(this@UserActivity,"Eliminación en proceso, aguarde...",Toast.LENGTH_SHORT).show()
                        }
                        reautenticar.addOnFailureListener {
                            Toast.makeText(this@UserActivity,"El usuario no pudo ser reautenticado",Toast.LENGTH_SHORT).show()
                        }
                    }
                    updValsGoo.await()
                }
            }catch (error: ApiException){
                avisoEli("Error: No se pudo eliminar la informacion solicitada")
            }
        }
    }
}