package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts;
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtNomReg: EditText
    private lateinit var txtUsReg: EditText
    private lateinit var rbSelRegEma: RadioButton
    private lateinit var LinEmaReg: LinearLayout
    private lateinit var rbSelRegGoo: RadioButton
    private lateinit var txtEmailReg: EditText
    private lateinit var txtPassReg: EditText
    private lateinit var chbVerPassReg: CheckBox
    private lateinit var spPregsSegur: Spinner
    private lateinit var txtRespSeguri: EditText
    private lateinit var rbSelCli: RadioButton
    private lateinit var rbSelAdmin: RadioButton
    private lateinit var spSisRel: Spinner
    private lateinit var txtTel: EditText
    private lateinit var btnRegEma: Button
    private lateinit var btnRegGoo: Button
    // Estableciendo la obtencion de valores
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
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

    // Data clases para objetos virtuales (simulados) de kotlin
    data class Acceso(val correo: String, val google: String)
    data class SistemasUser(val sistema1: String)
    data class UserCliente(val nombre: String, val username: String, val tipo_Usuario: String, val accesos: Acceso, val sistemas: SistemasUser, val pregunta_Seg: String, val resp_Seguri: String)
    data class UserAdmin(val nombre: String, val username: String, val tipo_Usuario: String, val accesos: Acceso, val sistemas: SistemasUser, val pregunta_Seg: String, val resp_Seguri: String, val num_Tel: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@RegisterActivity, R.color.teal_700)))

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
        LinEmaReg = findViewById(R.id.LLRegEma)
        rbSelRegGoo = findViewById(R.id.rbSelGooReg)
        txtEmailReg = findViewById(R.id.txtEmailReg)
        txtPassReg = findViewById(R.id.txtPassReg)
        chbVerPassReg = findViewById(R.id.chbRegPass)
        spPregsSegur = findViewById(R.id.spSafQuKyReg)
        txtRespSeguri = findViewById(R.id.txtRespSegur)
        rbSelCli = findViewById(R.id.rbTipUsCli)
        rbSelAdmin = findViewById(R.id.rbTipUsAdmin)
        spSisRel = findViewById(R.id.spSistema)
        txtTel = findViewById(R.id.txtTel)
        btnRegEma = findViewById(R.id.btnRegEma)
        btnRegGoo = findViewById(R.id.btnRegGoo)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Mensaje de bienvenida para decirle al usuario que debe hacer
        Toast.makeText(this@RegisterActivity,"Ingrese los datos que se solicitan",Toast.LENGTH_LONG).show()
        // Invocacion a la funcion para rellenar el spinner de las preguntas
        rellSpinPregs()
        // Invocacion a la funcion para rellenar el spinner de los sistemas registrados
        rellSpinSis()
    }

    private fun addListeners(){
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
                "* Lada + Numero ó Tel. Celular"

        //Agregar los listener
        btnAyuda.setOnClickListener {
            avisoReg(msg)
        }
        rbSelRegEma.setOnClickListener {
            if (rbSelRegEma.isChecked) {
                LinEmaReg.isGone = false
                btnRegEma.isGone = false
                btnRegGoo.isGone = true
            }
        }
        rbSelRegGoo.setOnClickListener {
            if (rbSelRegGoo.isChecked) {
                LinEmaReg.isGone = true
                btnRegEma.isGone = true
                btnRegGoo.isGone = false
            }
        }
        rbSelCli.setOnClickListener {
            if(rbSelCli.isChecked){
                txtTel.isGone = true
                valiTipUser = false
            }
        }
        rbSelAdmin.setOnClickListener {
            if(rbSelAdmin.isChecked){
                txtTel.isGone = false
                valiTipUser = true
            }
        }
        chbVerPassReg.setOnClickListener{
            if(!chbVerPassReg.isChecked){
                txtPassReg.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtPassReg.transformationMethod = HideReturnsTransformationMethod.getInstance()
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
                var arrPregs = ArrayList<String>()
                arrPregs.addAll(lstPregs)
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                ref.get().addOnSuccessListener{ taskGet ->
                    for (objPreg in taskGet.children){
                        objPreg.ref.child("Val_Pregunta").get().addOnSuccessListener { taskAdd ->
                            arrPregs.add(taskAdd.value.toString())
                        }
                    }
                    // Estableciendo el adaptador para el rellenado del spinner
                    val adapPregs = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, arrPregs)
                    adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spPregsSegur.adapter = adapPregs
                }
                    .addOnFailureListener {
                        avisoReg("Error: Datos parcialmente obtenidos")
                    }
            }
            rellPregs.await()
        }
    }

    private fun rellSpinSis(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                var arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children){
                            objSis.ref.child("nombre_Sis").get().addOnSuccessListener { taskGet ->
                                arrSists.add(taskGet.value.toString())
                            }
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spSisRel.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        avisoReg("Error: Datos parcialmente obtenidos; ${databaseError.toException()}")
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
            TextUtils.isEmpty(nombre) -> avisoReg("Error: Favor de introducir un nombre")
            // Si se encuentra algun numero
            (Regex("""\d+""").containsMatchIn(nombre)) -> avisoReg("Error: Su nombre no puede contener numeros")
            // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
            (nombre.length < 10) -> avisoReg("Error: Su nombre es muy corto, favor de agregar su nombre completo")
            // Si se encuentran caracteres especiales
            (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> avisoReg("Error: Su nombre no puede contener caracteres especiales")
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
            TextUtils.isEmpty(usuarioFil2) -> avisoReg("Error: Favor de introducir un nombre de usuario")
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> avisoReg("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> avisoReg("Error: El nombre de usuario debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> avisoReg("Error: El nombre de usuario debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(usuarioFil2)) -> avisoReg("Error: Favor de incluir al menos un caracter especial en su nombre de usuario")
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
            TextUtils.isEmpty(correoFil2) -> avisoReg("Error: Favor de introducir un correo")
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> avisoReg("Error: Favor de introducir un correo valido")
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
            TextUtils.isEmpty(contraFil2) -> avisoReg("Error: Favor de introducir una contraseña")
            // Extension minima de 8 caracteres
            (contraFil2.length < 8) -> avisoReg("Error: La contraseña debera tener una extension minima de 8 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(contraFil2)) -> avisoReg("Error: La contraseña debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(contraFil2)) -> avisoReg("Error: La contraseña debera tener al menos un numero")
            // No se tiene al menos un caracter especial
            (!Regex("""[^A-Za-z ]+""").containsMatchIn(contraFil2)) -> avisoReg("Error: Favor de incluir al menos un caracter especial en su contraseña")
            else -> return true
        }
        return false
    }
    private fun validarSelPreg(lista: Spinner): Boolean{
        if(lista.selectedItemPosition == 0){
            avisoReg("Error: Favor de seleccionar una pregunta")
            return false
        }
        return true
    }
    private fun validarResp(respuesta: Editable): Boolean{
        if(TextUtils.isEmpty(respuesta)){
            avisoReg("Error: Favor de introducir una respuesta para su pregunta")
            return false
        }
        return true
    }
    private fun validarSelTipoUser(rbCliente: RadioButton, rbAdmin: RadioButton): Boolean{
        if(!rbCliente.isChecked && !rbAdmin.isChecked){
            avisoReg("Error: Favor de seleccionar un tipo de usuario")
            return false
        }
        return true
    }
    private fun validarSelSis(lista: Spinner): Boolean{
        if(lista.selectedItemPosition == 0){
            avisoReg("Error: Favor de seleccionar un sistema")
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
            TextUtils.isEmpty(numTelFil2) -> avisoReg("Error: Favor de introducir un numero telefonico")
            // Si se encuentra algun caracter ademas de numeros
            (Regex("""\D""").containsMatchIn(numTelFil2)) -> avisoReg("Error: El numero de telefono solo puede contener digitos")
            // Contemplando numeros fijos con lada y celulares; estos deberan ser de 10 caracteres
            (numTelFil2.length < 10) -> avisoReg("Advertencia: Favor de introducir su numero telefonico fijo con lada o su celular")
            else -> return true
        }
        return false
    }

    private fun buscarUsBD(){
        // Cuando se solicite el registro independientemente del metodo, primero se validara el user y luego se buscara en la BD
        valiUser = validarUsuario(txtUsReg.text)
        // Preparando variables para la obtencion de valores de los campos
        val usuario = txtUsReg.text.toString().trim()
        var busUser = false

        if(valiUser){
            // Buscando al usuario en la BD
            ref = database.getReference("Usuarios")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (objUser in dataSnapshot.children) {
                        if (objUser.key.toString() == usuario){
                            busUser = true
                            break
                        }
                    }
                    if(!busUser){
                        if(rbSelRegEma.isChecked)
                            valiRegEma()
                        else if(rbSelRegGoo.isChecked)
                            valiRegGoo()
                    }else{
                        // Mostrar mensaje de error y limpiar los campos
                        avisoReg("Error: El username que desea utilizar ya ha sido registrado, favor de ingresar otro")
                        txtNomReg.text.clear()
                        txtUsReg.text.clear()
                        if(rbSelRegEma.isChecked && txtEmailReg.text.isNotEmpty())
                            txtEmailReg.text.clear()
                        if(rbSelRegEma.isChecked && txtPassReg.text.isNotEmpty())
                            txtPassReg.text.clear()
                        if(!LinEmaReg.isGone)
                            LinEmaReg.isGone = true
                        rbSelRegEma.isChecked = false
                        rbSelRegGoo.isChecked = false
                        spPregsSegur.setSelection(0)
                        txtRespSeguri.text.clear()
                        if(rbSelAdmin.isChecked && txtTel.text.isNotEmpty())
                            txtTel.text.clear()
                        rbSelAdmin.isChecked = false
                        rbSelCli.isChecked = false
                        spSisRel.setSelection(0)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            avisoReg("El username no pudo ser validado correctamente")
        }
    }

    private fun valiRegEma(){
        // Validacion individual de los campos
        valiNam = validarNombre(txtNomReg.text)
        valiCorr = validarCorreo(txtEmailReg.text)
        valiPass = validarContra(txtPassReg.text)
        valiPreg = validarSelPreg(spPregsSegur)
        valiResp = validarResp(txtRespSeguri.text)
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
            valiTel = validarTel(txtTel.text)
            if(valiNam && valiCorr && valiPass && valiPreg && valiResp && valiSis && valiTel)
                registroEma(tipo)
            else
                avisoReg("Error: No se ha podido validar la informacion del administrador")
        }
    }

    private fun valiRegGoo(){
        // Validacion individual de todos los campos
        valiNam = validarNombre(txtNomReg.text)
        valiPreg = validarSelPreg(spPregsSegur)
        valiResp = validarResp(txtRespSeguri.text)
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
            valiTel = validarTel(txtTel.text)
            if(valiNam && valiPreg && valiResp && valiSis && valiTel)
                registroGoo()
            else
                avisoReg("Error: No se ha podido validar la informacion del administrador")
        }
    }

    private fun registroEma(tipo: String){
        // Preparando variables para la obtencion de valores de los campos
        val nombre = txtNomReg.text.toString()
        val usuario = txtUsReg.text.toString().trim()
        val emaLimp = txtEmailReg.text.toString().trim()
        val pasLimp = txtPassReg.text.toString().trim()
        val pregunta = spPregsSegur.selectedItem.toString()
        val respuesta = txtRespSeguri.text.toString()
        val sistema = spSisRel.selectedItem.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emaLimp, pasLimp).addOnCompleteListener { task ->
            if (task.isSuccessful) {
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
                        avisoReg("Datos recuperados parcialmente o sin recuperar")
                    }
                })
                // Creando la relacion del usuario con el sistema en la entidad Sistemas
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSis in dataSnapshot.children) {
                            if (objSis.child("nombre_Sis").value.toString() == sistema)
                                objSis.ref.child("usuarios").child(usuario).setValue(true)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        avisoReg("Datos recuperados parcialmente o sin recuperar")
                    }
                })
                // Preparando el objeto del usuario para el registro en la BD
                val nAcc = Acceso(correo = emaLimp, google = "")
                val nUsSis = SistemasUser(sistema1 = "sistema${spSisRel.selectedItemPosition}")
                if(tipo == "Cliente") {
                    // Usuario cliente
                    val nUser = UserCliente( nombre = nombre, username = usuario, tipo_Usuario = tipo, accesos = nAcc, sistemas = nUsSis, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta )
                    // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                    ref = database.getReference("Usuarios")
                    ref.child(usuario).setValue(nUser).addOnCompleteListener {
                        // Se procede a lanzar al usuario a la activity de dashboard
                        Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                        // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                        val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                            putExtra("username", usuario)
                            putExtra("tipo", tipo)
                        }
                        startActivity(intentDash)
                    }
                        .addOnFailureListener {
                            Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    // Usuario administrador
                    val telefono = txtTel.text.toString().toLong()
                    val nUser = UserAdmin( nombre = nombre, username = usuario, tipo_Usuario = tipo, accesos = nAcc, sistemas = nUsSis, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, num_Tel = telefono )
                    // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                    ref = database.getReference("Usuarios")
                    ref.child(usuario).setValue(nUser).addOnCompleteListener {
                        // Se procede a lanzar al usuario a la activity de dashboard
                        Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                        // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                        val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                            putExtra("username", usuario)
                            putExtra("tipo", tipo)
                        }
                        startActivity(intentDash)
                    }
                        .addOnFailureListener {
                            Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                        }
                }
            }else{
                avisoReg("Ocurrio un error en el proceso de creacion del usuario, favor de intentarlo despues")
            }
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
            val nombre = txtNomReg.text.toString()
            val usuario = txtUsReg.text.toString().trim()
            val pregunta = spPregsSegur.selectedItem.toString()
            val respuesta = txtRespSeguri.text.toString()
            val sistema = spSisRel.selectedItem.toString()

            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Obteniendo la credencial
                val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
                // Accediendo con los datos de la cuenta de google
                FirebaseAuth.getInstance().signInWithCredential(credencial).addOnCompleteListener {task2 ->
                    if(task2.isSuccessful){
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
                                avisoReg("Datos recuperados parcialmente o sin recuperar")
                            }
                        })
                        // Creando la relacion del usuario con el sistema en la entidad Sistemas
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (objSis in dataSnapshot.children) {
                                    if (objSis.child("nombre_Sis").value.toString() == sistema)
                                        objSis.ref.child("usuarios").child(usuario).setValue(true)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                avisoReg("Datos recuperados parcialmente o sin recuperar")
                            }
                        })
                        // En este caso, como es necesario extraer el correo desde la autenticacion de google, se tiene usar el let
                        authUs.let { _ ->
                            val correo = authUs?.email
                            // Preparando el objeto del usuario para el registro en la BD
                            val nAcc = correo?.let { Acceso(correo = "", google = it) }
                            val nUsSis = SistemasUser(sistema1 = "sistema${spSisRel.selectedItemPosition}")
                            if(tipo == "Cliente"){
                                // Usuario cliente
                                val nUser = nAcc?.let {
                                    UserCliente( nombre = nombre, username = usuario, tipo_Usuario = tipo, accesos = nAcc, sistemas = nUsSis, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta )
                                }
                                // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                ref = database.getReference("Usuarios")
                                ref.child(usuario).setValue(nUser).addOnCompleteListener {
                                    // Se procede a lanzar al usuario a la activity de dashboard
                                    Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                    val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                        putExtra("username", usuario)
                                        putExtra("tipo", tipo)
                                    }
                                    startActivity(intentDash)
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                    }
                            }else{
                                // Usuario administrador
                                val telefono = txtTel.text.toString().toLong()
                                val nUser = nAcc?.let {
                                    UserAdmin( nombre = nombre, username = usuario, tipo_Usuario = tipo, accesos = nAcc, sistemas = nUsSis, pregunta_Seg = "pregunta${spPregsSegur.selectedItemPosition}", resp_Seguri = respuesta, num_Tel = telefono )
                                }
                                // Establecer la referencia con la entidad Usuarios y agregar el nuevo objeto del usuario en la misma
                                ref = database.getReference("Usuarios")
                                ref.child(usuario).setValue(nUser).addOnCompleteListener {
                                    // Se procede a lanzar al usuario a la activity de dashboard
                                    Toast.makeText(this@RegisterActivity, "Bienvenido a Ardu Security $nombre", Toast.LENGTH_SHORT).show()
                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                    val intentDash = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                                        putExtra("username", usuario)
                                        putExtra("tipo", tipo)
                                    }
                                    startActivity(intentDash)
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@RegisterActivity, "Error: No se pudo registrar el usuario en cuestion", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }else{
                        avisoReg("Ocurrio un error en el proceso de creacion del usuario, favor de intentarlo despues")
                    }
                }
            }catch (error: ApiException){
                avisoReg("Error: No se pudo acceder con la informacion ingresada")
            }
        }
    }
}