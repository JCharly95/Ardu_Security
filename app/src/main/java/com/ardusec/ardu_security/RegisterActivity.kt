package com.ardusec.ardu_security

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.user.DashboardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtNomReg: TextInputEditText
    private lateinit var txtUsReg: TextInputEditText
    private lateinit var rbSelRegEma: RadioButton
    private lateinit var linLayEmaReg: LinearLayout
    private lateinit var rbSelRegGoo: RadioButton
    private lateinit var txtEmailReg: TextInputEditText
    private lateinit var txtPassReg: TextInputEditText
    private lateinit var spPregsSegur: Spinner
    private lateinit var txtRespSeguri: TextInputEditText
    private lateinit var rbSelCli: RadioButton
    private lateinit var rbSelAdmin: RadioButton
    private lateinit var spSisRel: Spinner
    private lateinit var txtLayTel: TextInputLayout
    private lateinit var txtTel: TextInputEditText
    private lateinit var btnRegEma: Button
    private lateinit var btnRegGoo: Button
    // Estableciendo la obtencion de valores
    // Instancias de Firebase; Autenticacion, ReferenciaDB, DatabaseGen y Cloud Messasing
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var notifications: FirebaseMessaging
    // Banderas de validacion
    private var valiNam = false
    private var valiUser = false
    private var valiCorr = false
    private var valiPass = false
    private var valiPreg = false
    private var valiResp = false
    private var valiTipUser = false // Por defecto, se establecera a todos como clientes
    private var valiSis = false
    private var valiTel = false
    // Variables de acceso para google
    private lateinit var googleConf: GoogleSignInOptions
    private lateinit var googleCli: GoogleSignInClient
    // ID del acceso de google
    private val GoogleAcces = 195
    // Verificando el permiso de notificaciones de la aplicacion
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this@RegisterActivity, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@RegisterActivity,"Ardu Security no puede enviarle notificaciones hasta que autorice el permiso adecuado",Toast.LENGTH_LONG).show()
        }
    }

    // Data clases para objetos virtuales (simulados) de kotlin
    data class Acceso(val correo: String, val google: String)
    data class UserCliente(val nombre: String, val username: String, val accesos: Acceso, val sistema_Rel: String, val tipo_Usuario: String, val pregunta_Seg: String, val resp_Seguri: String, val tokenNotificaciones: String)
    data class UserAdmin(val nombre: String, val username: String, val accesos: Acceso, val sistema_Rel: String, val tipo_Usuario: String, val pregunta_Seg: String, val resp_Seguri: String, val num_Tel: Long, val tokenNotificaciones: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@RegisterActivity,R.color.teal_700)))

        // Preparacion de los elementos
        setUp()
        // Agregando los listener
        addListeners()
    }

    private fun setUp(){
        title = "Registrarse"
        // Inicializando los elementos
        btnAyuda = findViewById(R.id.btnInfoReg)
        txtNomReg = findViewById(R.id.txtNomReg)
        txtUsReg = findViewById(R.id.txtUserReg)
        rbSelRegEma = findViewById(R.id.rbSelEmaReg)
        linLayEmaReg = findViewById(R.id.LLRegEma)
        rbSelRegGoo = findViewById(R.id.rbSelGooReg)
        txtEmailReg = findViewById(R.id.txtEmailReg)
        txtPassReg = findViewById(R.id.txtPassReg)
        spPregsSegur = findViewById(R.id.spSafQuKyReg)
        txtRespSeguri = findViewById(R.id.txtRespSegur)
        rbSelCli = findViewById(R.id.rbTipUsCli)
        rbSelAdmin = findViewById(R.id.rbTipUsAdmin)
        spSisRel = findViewById(R.id.spSistema)
        txtLayTel = findViewById(R.id.txtLayTel)
        txtTel = findViewById(R.id.txtTel)
        btnRegEma = findViewById(R.id.btnRegEma)
        btnRegGoo = findViewById(R.id.btnRegGoo)
        // Inicializando instancia hacia el nodo raiz de la BD, la de la autenticacion y el servicio de mensajes
        database = Firebase.database
        auth = FirebaseAuth.getInstance()
        notifications = FirebaseMessaging.getInstance()

        // Mensaje de bienvenida para decirle al usuario que debe hacer
        avisoReg("Bienvenido, por favor, ingrese los datos solicitados")
        // Invocacion a la funcion para rellenar el spinner de las preguntas
        rellSpinPregs()
        // Invocacion a la funcion para rellenar el spinner de los sistemas registrados
        rellSpinSis()

        // Despues de la configuracion inicial se pregunta por el permiso de notificaciones
        preguntarPermisoNotificaciones()
    }

    private fun addListeners(){
        // Agregar los listener
        btnAyuda.setOnClickListener {
            val msgAyuda = "Requisitos necesarios de cada campo: \n\n" +
                    "Nombre:\n" +
                    "* NO debe tener números.\n" +
                    "* Debe tener una extensión mínima de 10 caracteres.\n\n" +
                    "Username (Nombre de usuario):\n" +
                    "* Debe tener una extensión mínima de 6 caracteres.\n" +
                    "* Debe tener por lo menos una letra mayúscula.\n" +
                    "* Debe tener por lo menos el digito de un número.\n" +
                    "Dirección de Correo:\n" +
                    "* La estructura aceptada es:\n" +
                    "-- usuario@dominio (.extensión de país; esto es opcional ingresarlo solo si su dirección lo contiene)\n\n" +
                    "Contraseña:\n" +
                    "* Debe tener una extensión mínima de 8 caracteres.\n" +
                    "* Debe tener por lo menos una letra mayúscula.\n" +
                    "* Debe tener por lo menos el digito de un número.\n" +
                    "* Debe tener por lo menos un carácter especial.\n\n" +
                    "Pregunta de Seguridad:\n" +
                    "* Debe seleccionar cualquiera de las 5 preguntas de seguridad disponibles.\n\n" +
                    "Respuesta de Seguridad:\n" +
                    "* Debe ingresar una respuesta a la pregunta seleccionada, esta será de formato libre.\n\n" +
                    "Sistema:\n" +
                    "* Debe seleccionar un sistema para poder relacionarse con el entorno.\n" +
                    "*** NOTA: Si el sistema al que desea ingresar no está en la lista, favor de seleccionar algun sistema disponible y hacer el cambio dentro.\n\n" +
                    "Tipo de Usuario:\n" +
                    "* Debe seleccionar uno de los tipos de usuarios disponibles.\n\n" +
                    "Numero de Teléfono:\n" +
                    "* Debe ingresar solamente números\n" +
                    "* Debe tener una extensión de 10 dígitos, no más y no menos\n" +
                    "** Estructuras Aceptadas:\n" +
                    "-- Número Fijo: Lada + Numero\n" +
                    "-- Número Celular\n"
            avisoReg(msgAyuda)
        }
        rbSelRegEma.setOnClickListener {
            if (rbSelRegEma.isChecked) {
                linLayEmaReg.isGone = false
                btnRegEma.isGone = false
                btnRegGoo.isGone = true
            }
        }
        rbSelRegGoo.setOnClickListener {
            if (rbSelRegGoo.isChecked) {
                linLayEmaReg.isGone = true
                btnRegEma.isGone = true
                btnRegGoo.isGone = false
            }
        }
        rbSelCli.setOnClickListener {
            if(rbSelCli.isChecked){
                txtLayTel.isGone = true
                valiTipUser = false
            }
        }
        rbSelAdmin.setOnClickListener {
            if(rbSelAdmin.isChecked){
                txtLayTel.isGone = false
                valiTipUser = true
            }
        }
        btnRegEma.setOnClickListener {
            buscarUsBD()
        }
        btnRegGoo.setOnClickListener {
            buscarUsBD()
        }
    }

    private fun rellSpinPregs(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellPregs = async {
                // Obtener el arreglo de strings establecido para las preguntas
                val lstPregs = resources.getStringArray(R.array.lstSavQues)
                val arrPregs = ArrayList<String>()
                arrPregs.addAll(lstPregs)
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objPreg in dataSnapshot.children){
                            arrPregs.add(objPreg.child("Val_Pregunta").value.toString())
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapPregs = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, arrPregs)
                        adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spPregsSegur.adapter = adapPregs
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@RegisterActivity, "Error: Consulta Incompleta; Causa: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellPregs.await()
        }
    }

    private fun rellSpinSis(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                val arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children){
                            arrSists.add(objSis.child("nombre_Sis").value.toString())
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spSisRel.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@RegisterActivity, "Error: Consulta Incompleta; Causa: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellSis.await()
        }
    }

    private fun avisoReg(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun validarNombre(nombre: Editable): Boolean{
        when{
            // Si el nombre esta vacio
            TextUtils.isEmpty(nombre) -> {
                lifecycleScope.launch(Dispatchers.Main){
                    avisoReg("Error: Favor de introducir un nombre")
                }
                txtNomReg.text!!.clear()
                return false
            }
            // Si se encuentra algun numero
            (Regex("""\d+""").containsMatchIn(nombre)) -> {
                lifecycleScope.launch(Dispatchers.Main){
                    avisoReg("Error: Su nombre no puede contener numeros")
                }
                txtNomReg.text!!.clear()
                return false
            }
            // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
            (nombre.length < 10) -> {
                lifecycleScope.launch(Dispatchers.Main){
                    avisoReg("Error: Su nombre es muy corto, favor de agregar su nombre completo")
                }
                txtNomReg.text!!.clear()
                return false
            }
            // Si se encuentran caracteres especiales
            (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> {
                lifecycleScope.launch(Dispatchers.Main){
                    avisoReg("Error: Su nombre no puede contener caracteres especiales")
                }
                txtNomReg.text!!.clear()
                return false
            }
            else -> { return true }
        }
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
                    avisoReg("Error: Favor de introducir un nombre de usuario")
                }
                txtUsReg.text!!.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
                }
                txtUsReg.text!!.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: El nombre de usuario debera tener al menos una letra mayuscula")
                }
                txtUsReg.text!!.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: El nombre de usuario debera tener al menos un numero")
                }
                txtUsReg.text!!.clear()
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
                    avisoReg("Error: Favor de introducir un correo")
                }
                txtEmailReg.text!!.clear()
                return false
            }
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: Favor de introducir un correo valido")
                }
                txtEmailReg.text!!.clear()
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
                    avisoReg("Error: Favor de introducir una contraseña")
                }
                txtPassReg.text!!.clear()
                return false
            }
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: La contraseña debera tener una extension minima de 8 caracteres")
                }
                txtPassReg.text!!.clear()
                return false
            }
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: La contraseña debera tener al menos una letra mayuscula")
                }
                txtPassReg.text!!.clear()
                return false
            }
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: La contraseña debera tener al menos un numero")
                }
                txtPassReg.text!!.clear()
                return false
            }
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: Favor de incluir al menos un caracter especial en su contraseña")
                }
                txtPassReg.text!!.clear()
                return false
            }
            else -> { return true }
        }
    }
    private fun validarSelPreg(lista: Spinner): Boolean{
        return if(lista.selectedItemPosition == 0){
            avisoReg("Error: Favor de seleccionar una pregunta")
            false
        }else{
            true
        }
    }
    private fun validarResp(respuesta: Editable): Boolean{
        return if(TextUtils.isEmpty(respuesta)){
            avisoReg("Error: Favor de introducir una respuesta a su pregunta")
            false
        }else{
            true
        }
    }
    private fun validarSelTipoUser(rbCliente: RadioButton, rbAdmin: RadioButton): Boolean{
        return if(!rbCliente.isChecked && !rbAdmin.isChecked){
            avisoReg("Error: Favor de seleccionar un tipo de usuario")
            false
        }else{
            true
        }
    }
    private fun validarSelSis(lista: Spinner): Boolean{
        return if(lista.selectedItemPosition == 0){
            avisoReg("Error: Favor de seleccionar un sistema")
            false
        }else {
            true
        }
    }
    private fun validarTel(numTel: Editable): Boolean{
        // Si se detectan espacios en el numero, estos seran removidos
        val numTelFil1 = numTel.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val numTelFil2 = numTelFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el telefono esta vacio
            TextUtils.isEmpty(numTelFil2) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: Favor de introducir un numero telefonico")
                }
                txtTel.text!!.clear()
                return false
            }
            // Si se encuentra algun caracter ademas de numeros
            (Regex("""\D""").containsMatchIn(numTelFil2)) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Error: El numero de telefono solo puede contener digitos")
                }
                txtTel.text!!.clear()
                return false
            }
            // Contemplando numeros fijos con lada y celulares; estos deberan ser de 10 caracteres
            (numTelFil2.length < 10) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Advertencia: Favor de introducir su número telefonico fijo con lada o su número celular")
                }
                txtTel.text!!.clear()
                return false
            }
            (numTelFil2.length > 10) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    avisoReg("Advertencia: Por el momento solo se contemplan numeros telefonicos mexicanos, lamentamos las molestias")
                }
                txtTel.text!!.clear()
                return false
            }
            else -> return true
        }
    }

    private fun buscarUsBD(){
        // Cuando se solicite el registro independientemente del metodo, primero se validara el user y luego se buscara en la BD
        valiUser = validarUsuario(txtUsReg.text!!)
        // Preparando variables para la obtencion de valores de los campos
        val usuario = txtUsReg.text!!.toString().trim()
        var busUser = false
        // Si el username fue validado correctamente, se procedera a buscar el usuario en la BD
        if(valiUser){
            ref = database.getReference("Usuarios")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (objUser in dataSnapshot.children) {
                        if (objUser.key.toString() == usuario){
                            busUser = true
                            break
                        }
                    }
                    // Si el usuario no se encuentra, se procederá con el registro del usuario
                    if(!busUser){
                        if(rbSelRegEma.isChecked)
                            valiRegEma()
                        else if(rbSelRegGoo.isChecked)
                            valiRegGoo()
                    }else{
                        // Si se encuentra, se lanzará un aviso de error y se limpiaran los campos
                        avisoReg("Error: El nombre de usuario que desea utilizar ya ha sido registrado, favor de ingresar otro")
                        txtNomReg.text!!.clear()
                        txtUsReg.text!!.clear()
                        rbSelRegEma.isChecked = false
                        rbSelRegGoo.isChecked = false
                        txtEmailReg.text!!.clear()
                        txtPassReg.text!!.clear()
                        if(!linLayEmaReg.isGone) {
                            linLayEmaReg.isGone = true
                        }
                        spPregsSegur.setSelection(0)
                        txtRespSeguri.text!!.clear()
                        spSisRel.setSelection(0)
                        rbSelAdmin.isChecked = false
                        rbSelCli.isChecked = false
                        txtTel.text!!.clear()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun valiRegEma(){
        // Validacion individual de los campos
        valiNam = validarNombre(txtNomReg.text!!)
        valiCorr = validarCorreo(txtEmailReg.text!!)
        valiPass = validarContra(txtPassReg.text!!)
        valiPreg = validarSelPreg(spPregsSegur)
        valiResp = validarResp(txtRespSeguri.text!!)
        valiTipUser = validarSelTipoUser(rbSelCli, rbSelAdmin)
        valiSis = validarSelSis(spSisRel)
        // Determinacion del tipo de usuario a registrar
        val tipo = when {
            rbSelCli.isChecked -> "Cliente"
            rbSelAdmin.isChecked -> "Administrador"
            else -> ""
        }
        // Verificacion de seleccion: Cliente
        if(valiTipUser && tipo=="Cliente"){
            if(valiNam && valiCorr && valiPass && valiPreg && valiResp && valiSis)
                registroEma(tipo)
            else
                avisoReg("Error: No se ha podido validar la informacion del cliente")
        }else if(valiTipUser && tipo=="Administrador"){
            // Verificacion de seleccion: Administrador
            valiTel = validarTel(txtTel.text!!)
            if(valiNam && valiCorr && valiPass && valiPreg && valiResp && valiSis && valiTel)
                registroEma(tipo)
            else
                avisoReg("Error: No se ha podido validar la informacion del administrador")
        }else{
            avisoReg("Error: No se puede proceder con el registro, porque no fue seleccionado un tipo de usuario, favor de ingresarlo")
        }
    }

    private fun valiRegGoo(){
        // Validacion individual de todos los campos
        valiNam = validarNombre(txtNomReg.text!!)
        valiPreg = validarSelPreg(spPregsSegur)
        valiResp = validarResp(txtRespSeguri.text!!)
        valiTipUser = validarSelTipoUser(rbSelCli, rbSelAdmin)
        valiSis = validarSelSis(spSisRel)
        // Determinacion del tipo de usuario a registrar
        val tipo = when {
            rbSelCli.isChecked -> "Cliente"
            rbSelAdmin.isChecked -> "Administrador"
            else -> ""
        }
        // Anteponemos la solicitud de la peticion (preparacion)
        crearPeticionGoogle()
        // Verificacion de seleccion: Cliente
        if(valiTipUser && tipo=="Cliente"){
            if(valiNam && valiPreg && valiResp && valiSis)
                registroGoo()
            else
                avisoReg("Error: No se ha podido validar la informacion del cliente")
        }else if(valiTipUser && tipo=="Administrador"){
            // Verificacion de seleccion: Administrador
            valiTel = validarTel(txtTel.text!!)
            if(valiNam && valiPreg && valiResp && valiSis && valiTel)
                registroGoo()
            else
                avisoReg("Error: No se ha podido validar la informacion del administrador")
        }else{
            avisoReg("Error: No se puede proceder con el registro, porque no fue seleccionado un tipo de usuario, favor de ingresarlo")
        }
    }

    private fun registroEma(tipo: String){
        // Preparando variables para la obtencion de valores de los campos
        val nombre = txtNomReg.text!!.toString()
        val usuario = txtUsReg.text!!.toString().trim()
        val emaLimp = txtEmailReg.text!!.toString().trim()
        val pasLimp = txtPassReg.text!!.toString().trim()
        val pregunta = spPregsSegur.selectedItem.toString()
        val respuesta = txtRespSeguri.text!!.toString()
        val sistema = spSisRel.selectedItem.toString()
        // Creando el usuario con correo y contraseña en la autenticacion
        val crearUsuario = auth.createUserWithEmailAndPassword(emaLimp, pasLimp)
        crearUsuario.addOnSuccessListener {
            // Obteniendo la referencia del usuario generada por el objeto de autenticacion
            val authUs = auth.currentUser
            // Ingresar el nombre del usuario en el perfil de autenticacion de firebase
            val actPerfil = userProfileChangeRequest { displayName = nombre }
            authUs?.updateProfile(actPerfil)
            // Creando la relacion del usuario con la pregunta en la entidad Preguntas
            ref = database.getReference("Preguntas")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (objPreg in dataSnapshot.children) {
                        if (objPreg.child("Val_Pregunta").value.toString() == pregunta)
                            objPreg.ref.child("usuarios").child(usuario).setValue(true)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Error: Registro de usuario parte 1, no completado",Toast.LENGTH_SHORT).show()
                }
            })
            // Creando la relacion del usuario con el sistema en la entidad Sistemas
            ref = database.getReference("Sistemas")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (objSis in dataSnapshot.children) {
                        if (objSis.child("nombre_Sis").value.toString() == sistema) {
                            objSis.ref.child("usuarios").child(usuario).setValue(true)
                            val sisKey = objSis.key.toString()
                            // Preparando el objeto del usuario para el registro en la BD
                            val nAcc = Acceso(correo = emaLimp, google = "")
                            if(tipo == "Cliente"){
                                // Token de notificaciones del dispositivo del usuario
                                notifications.token.addOnCompleteListener { taskTokNoti ->
                                    if(!taskTokNoti.isSuccessful){
                                        Toast.makeText(this@RegisterActivity, "No se pudo obtener el token para las notificaciones de Ardu Security", Toast.LENGTH_SHORT).show()
                                    }
                                    // Registro de Usuario cliente
                                    val nUser = UserCliente(nombre = nombre, username = usuario, accesos = nAcc, sistema_Rel = sisKey, tipo_Usuario = tipo, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, tokenNotificaciones = taskTokNoti.result)
                                    // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                    ref = database.getReference("Usuarios")
                                    val regCliente = ref.child(usuario).setValue(nUser)
                                    regCliente.addOnCompleteListener {
                                        // Se procede a lanzar al usuario a la activity de dashboard
                                        Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                        // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                        val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                            putExtra("username", usuario)
                                        }
                                        startActivity(intentDash)
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    }
                                    regCliente.addOnFailureListener {
                                        Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{
                                // Token de notificaciones del dispositivo de usuario
                                notifications.token.addOnCompleteListener { taskTokNoti ->
                                    if(!taskTokNoti.isSuccessful){
                                        Toast.makeText(this@RegisterActivity, "No se pudo obtener el token para las notificaciones de Ardu Security", Toast.LENGTH_SHORT).show()
                                    }
                                    // Registro de Usuario administrador
                                    val telefono = txtTel.text!!.toString().toLong()
                                    val nUser = UserAdmin(nombre = nombre, username = usuario, accesos = nAcc, sistema_Rel = sisKey, tipo_Usuario = tipo, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, num_Tel = telefono, tokenNotificaciones = taskTokNoti.result)
                                    // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                    ref = database.getReference("Usuarios")
                                    val regAdmin = ref.child(usuario).setValue(nUser)
                                    regAdmin.addOnCompleteListener {
                                        // Se procede a lanzar al usuario a la activity de dashboard
                                        Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                        // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                        val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                            putExtra("username", usuario)
                                        }
                                        startActivity(intentDash)
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    }
                                    regAdmin.addOnFailureListener {
                                        Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Error: Registro de usuario parte 2, no completado",Toast.LENGTH_SHORT).show()
                }
            })
        }
        crearUsuario.addOnFailureListener {
            val mensajeError = "El usuario no pudo ser creado debido a: \n" +
                    "${it.message} \n" +
                    "favor de intentarlo después"
            avisoReg(mensajeError)
        }
    }

    private fun crearPeticionGoogle() {
        // Bloque de codigo de la funcion crearPeticionGoogle() con el fin de optimizar las funciones
        // Configuracion google
        googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Obteniendo el cliente de google
        googleCli = GoogleSignIn.getClient(this@RegisterActivity, googleConf)
        // Fin de crearPeticionGoogle() y preparar la peticion de google
    }

    private fun registroGoo() {
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
            // Determinacion del tipo de usuario a registrar
            val tipo = when {
                rbSelCli.isChecked -> "Cliente"
                rbSelAdmin.isChecked -> "Administrador"
                else -> ""
            }
            // Preparando variables para la obtencion de valores de los campos
            val nombre = txtNomReg.text!!.toString()
            val usuario = txtUsReg.text!!.toString().trim()
            val pregunta = spPregsSegur.selectedItem.toString()
            val respuesta = txtRespSeguri.text!!.toString()
            val sistema = spSisRel.selectedItem.toString()

            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                // Accediendo con los datos de la cuenta de google
                val accGoo = auth.signInWithCredential(credencial)
                accGoo.addOnSuccessListener {
                    // Obteniendo la referencia del usuario generada por el objeto de autenticacion
                    val authUs = auth.currentUser
                    // Ingresar el nombre del usuario en el perfil de autenticacion de firebase
                    val actPerfil = userProfileChangeRequest { displayName = nombre }
                    authUs?.updateProfile(actPerfil)
                    // Creando la relacion del usuario con la pregunta en la entidad Preguntas
                    ref = database.getReference("Preguntas")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objPreg in dataSnapshot.children) {
                                if (objPreg.child("Val_Pregunta").value.toString() == pregunta)
                                    objPreg.ref.child("usuarios").child(usuario).setValue(true)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@RegisterActivity, "Error: Registro de usuario parte 1, no completado",Toast.LENGTH_SHORT).show()
                        }
                    })
                    // Creando la relacion del usuario con el sistema en la entidad Sistemas
                    ref = database.getReference("Sistemas")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (objSis in dataSnapshot.children) {
                                if (objSis.child("nombre_Sis").value.toString() == sistema) {
                                    objSis.ref.child("usuarios").child(usuario).setValue(true)
                                    val sisKey = objSis.key.toString()
                                    // En este caso, como es necesario extraer el correo desde la autenticacion de google, se tiene usar el let
                                    authUs.let { _ ->
                                        val correo = authUs?.email
                                        // Preparando el objeto del usuario para el registro en la BD
                                        val nAcc = correo?.let { Acceso(correo = "", google = it) }
                                        if(tipo == "Cliente"){
                                            // Token de notificaciones del dispositivo del usuario
                                            notifications.token.addOnCompleteListener { taskTokNoti ->
                                                if(!taskTokNoti.isSuccessful){
                                                    Toast.makeText(this@RegisterActivity, "No se pudo obtener el token para las notificaciones de Ardu Security", Toast.LENGTH_SHORT).show()
                                                }
                                                // Registro de Usuario cliente
                                                val nUser = UserCliente(nombre = nombre, username = usuario, accesos = nAcc!!, sistema_Rel = sisKey, tipo_Usuario = tipo, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, tokenNotificaciones = taskTokNoti.result)
                                                // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                                ref = database.getReference("Usuarios")
                                                val accGooClien = ref.child(usuario).setValue(nUser)
                                                accGooClien.addOnSuccessListener {
                                                    // Se procede a lanzar al usuario a la activity de dashboard
                                                    Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                                    val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                                        putExtra("username", usuario)
                                                    }
                                                    startActivity(intentDash)
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                }
                                                accGooClien.addOnFailureListener {
                                                    Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }else{
                                            // Token de notificaciones del dispositivo de usuario
                                            notifications.token.addOnCompleteListener { taskTokNoti ->
                                                if(!taskTokNoti.isSuccessful){
                                                    Toast.makeText(this@RegisterActivity, "No se pudo obtener el token para las notificaciones de Ardu Security", Toast.LENGTH_SHORT).show()
                                                }
                                                // Registro de Usuario administrador
                                                val telefono = txtTel.text!!.toString().toLong()
                                                val nUser = UserAdmin(nombre = nombre, username = usuario, accesos = nAcc!!, sistema_Rel = sisKey, tipo_Usuario = tipo, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, num_Tel = telefono, tokenNotificaciones = taskTokNoti.result)
                                                // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                                ref = database.getReference("Usuarios")
                                                val accGooAdmin = ref.child(usuario).setValue(nUser)
                                                accGooAdmin.addOnCompleteListener{
                                                    // Se procede a lanzar al usuario a la activity de dashboard
                                                    Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                                    val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                                        putExtra("username", usuario)
                                                    }
                                                    startActivity(intentDash)
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                }
                                                accGooAdmin.addOnFailureListener {
                                                    Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@RegisterActivity, "Error: Registro de usuario parte 2, no completado",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                accGoo.addOnFailureListener {
                    val mensajeError = "El usuario no pudo ser creado debido a: \n" +
                            "${it.message} \n" +
                            "favor de intentarlo después"
                    avisoReg(mensajeError)
                }
            }catch (error: ApiException){
                avisoReg("Error: No se pudo registrar al usuario con la informacion ingresada mediante el registro de google")
            }
        }
    }

    private fun preguntarPermisoNotificaciones(){
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@RegisterActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                Toast.makeText(this@RegisterActivity, "Su dispositivo podrá recibir notificaciones de la aplicación", Toast.LENGTH_SHORT).show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}