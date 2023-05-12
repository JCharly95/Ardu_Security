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
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class RegisterActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPass: EditText
    private lateinit var chbVerContra: CheckBox
    private lateinit var spSafQuKyReg: Spinner
    private lateinit var txtRespQues: EditText
    private lateinit var rbSelCli: RadioButton
    private lateinit var rbSelAdmin: RadioButton
    private lateinit var spSisRel: Spinner
    private lateinit var rbPinSi: RadioButton
    private lateinit var rbPinNo: RadioButton
    private lateinit var txtPin: EditText
    private lateinit var txtAdminTel: EditText
    private lateinit var btnReg: Button
    private lateinit var btnAyuda: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Creando el objeto GSON
    private var gson = Gson()
    // Banderas de validacion
    private var valiNam = false
    private var valiCorr = false
    private var valiCon = false
    private var valiPreg = false
    private var valiResp = false
    private var selPin = false  // Por defecto, se espera que el usuario no ingrese pin al registro
    private var valiPin = false
    private var valiSis = false
    private var valiTel = false
    private var valiTipUser = false // Po defecto, se establecera a todos como clientes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Preparacion de los elementos
        setUp()
        // Agregando los listener
        addListeners()
    }

    private fun setUp(){
        title = "Registrarse"
        // Inicializando los elementos
        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmailReg)
        txtPass = findViewById(R.id.txtPassReg)
        chbVerContra = findViewById(R.id.chbPassReg)
        spSafQuKyReg = findViewById(R.id.spSafQuKyReg)
        txtRespQues = findViewById(R.id.txtRespQues)
        rbSelCli = findViewById(R.id.rbTipUsCli)
        rbSelAdmin = findViewById(R.id.rbTipUsAdmin)
        rbPinSi = findViewById(R.id.rbIngrePin)
        rbPinNo = findViewById(R.id.rbNoIngrePin)
        txtPin = findViewById(R.id.txtPin)
        txtAdminTel = findViewById(R.id.txtTel)
        spSisRel = findViewById(R.id.spSistema)
        btnReg = findViewById(R.id.btnRegister)
        btnAyuda = findViewById(R.id.btnInfoReg)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Mensaje de bienvenida para decirle al usuario que debe hacer
        Toast.makeText(this,"Ingrese los datos que se solicitan",Toast.LENGTH_LONG).show()
        // Invocacion a la funcion para rellenar el spinner de las preguntas
        rellSpinPregs()
        // Invocacion a la funcion para rellenar el spinner de los sistemas registrados
        rellSpinSis()
    }

    private fun rellSpinPregs(){
        // Obtener el arreglo de strings establecido para las preguntas
        val lstPregs = resources.getStringArray(R.array.lstSavQues)
        var arrPregs = ArrayList<String>()
        arrPregs.addAll(lstPregs)
        // Creando la referencia de la coleccion de preguntas en la BD
        ref = database.getReference("Pregunta")
        data class Pregunta(val ID_Pregunta: String, val Val_Pregunta: String) // Creando una data class (es como una clase virtual de kotlin)
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objPreg in dataSnapshot.children){
                    val pregJSON = gson.toJson(objPreg.value)
                    val resPreg = gson.fromJson(pregJSON, Pregunta::class.java)
                    arrPregs.add(resPreg.Val_Pregunta)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapPregs = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrPregs)
                adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spSafQuKyReg.adapter = adapPregs
            }
            override fun onCancelled(databaseError: DatabaseError) {
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
        data class Sistema(val id_Sistema: String, val nombre_Sis: String, val ulti_Cam_Nom: String) // Creando una data class (es como una clase virtual de kotlin)
        // Agregando un ValueEventListener para operar con las instancias de pregunta
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objSis in dataSnapshot.children){
                    val sisJSON = gson.toJson(objSis.value)
                    val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                    arrSists.add(resSis.nombre_Sis)
                }
                // Estableciendo el adaptador para el rellenado del spinner
                val adapSis = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrSists)
                adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spSisRel.adapter = adapSis
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun avisoReg(){
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
                "* Lada + Numero ó Tel. Celular"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        //Agregar los listener
        rbSelCli.setOnClickListener {
            if(rbSelCli.isChecked){
                txtAdminTel.isGone = true
                valiTipUser = false
            }
        }
        rbSelAdmin.setOnClickListener {
            if(rbSelAdmin.isChecked){
                txtAdminTel.isGone = false
                valiTipUser = true
            }
        }
        chbVerContra.setOnClickListener{
            if(!chbVerContra.isChecked){
                txtPass.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnAyuda.setOnClickListener {
            avisoReg()
        }
        rbPinSi.setOnClickListener {
            txtPin.isGone = false
            selPin = true
        }
        rbPinNo.setOnClickListener {
            txtPin.isGone = true
            selPin = false
        }
        btnReg.setOnClickListener {
            validaciones()
        }
    }

    private fun validaciones(){
        // Validacion individual de todos los campos
        valiNam = ValiCampos.validarNombre(txtName.text, this)
        valiCorr = ValiCampos.validarCorreo(txtEmail.text, this)
        valiCon = ValiCampos.validarContra(txtPass.text, this)
        valiPreg = ValiCampos.validarSelPreg(spSafQuKyReg, this)
        valiResp = ValiCampos.validarResp(txtRespQues.text, this)
        valiSis = ValiCampos.validarSelSis(spSisRel, this)

        // Clases de datos virtuales para implementar el guardado
        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
        data class Sistema(val id_Sistema: String, val nombre_Sis: String, val ulti_Cam_Nom: String)
        data class UserSistem(val id_User_Sis: String, val sistema_Nom: String, val user_Email: String)

        // Preparar los campos limpios sin espacios para su guardado
        val emaLimp = txtEmail.text.toString().trim()
        val pasLimp = txtPass.text.toString().trim()
        // Verificar que tipo de usuario se va a registrar; Primero sera cliente, si no, es porque es administrador
        if(!valiTipUser){
            if(valiNam && valiCorr && valiCon && valiPreg && valiResp && valiSis){
                // Usuario cliente
                auth.createUserWithEmailAndPassword(emaLimp, pasLimp).addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        // Obteniendo la referencia del usuario generada por el objeto de autenticacion
                        val authUs = auth.currentUser
                        // Ingresar el nombre del usuario en el perfil de autenticacion de firebase
                        val actPerfil = userProfileChangeRequest { displayName = txtName.text.toString() }
                        authUs?.updateProfile(actPerfil)
                        // Preparando el sistema para ingresar el usuario en la BD; Parte 1: Obteniendo el valor de la pregunta seleccionada
                        val pregSel = spSafQuKyReg.selectedItem.toString()
                        // Preparando la informacion del registro para que se guarde con una key generada por firebase autenticacion
                        val nUser = if(selPin){    // Si se trae pin, aqui es donde sera agregado para su almacenado
                            valiPin = ValiCampos.validarPin(txtPin.text, this)
                            Usuario(id_Usuario=authUs!!.uid,nombre=txtName.text.toString(),correo=emaLimp,tipo_Usuario="Cliente",num_Tel=0,preg_Seguri=pregSel,resp_Seguri=txtRespQues.text.toString(),pin_Pass=txtPin.text.toString().toInt())
                        }else{
                            Usuario(id_Usuario=authUs!!.uid,nombre=txtName.text.toString(),correo=emaLimp,tipo_Usuario="Cliente",num_Tel=0,preg_Seguri=pregSel,resp_Seguri=txtRespQues.text.toString(),pin_Pass=0)
                        }
                        // Estableciendo el nuevo valor del usuario usando un child en la coleccion de usuarios
                        database.reference.child("Usuarios").child(authUs.uid).setValue(nUser)
                        // Creando los elementos para relacionar el usuario con el sistema
                        val sisRef = Firebase.database.getReference("Sistema")
                        sisRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for (objSis in dataSnapshot.children){
                                    val sisJSON = gson.toJson(objSis.value)
                                    val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                                    // Si el sistema selecionado corresponde con el registro de firebase se extraera el valor para ingresarlo en user_sistems
                                    if(spSisRel.selectedItem.toString() == resSis.nombre_Sis){
                                        val sisUsRef = Firebase.database.getReference("User_Sistems")
                                        val addUserSisDB = sisUsRef.push()
                                        val nRelSisUs = UserSistem(id_User_Sis=addUserSisDB.key.toString(),sistema_Nom=resSis.nombre_Sis,user_Email=emaLimp)
                                        addUserSisDB.setValue(nRelSisUs)
                                    }
                                    // Se procede a lanzar al usuario a la activity de dashboard
                                    Toast.makeText(applicationContext, "Bienvenido a Ardu Security ${txtName.text}", Toast.LENGTH_SHORT).show()
                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                    val intentDash = Intent(applicationContext, DashboardActivity::class.java).apply {
                                        putExtra("correo", txtEmail.text.toString())
                                    }
                                    startActivity(intentDash)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }
                }
            }else{
                Toast.makeText(this, "Error: No se ha podido autenticar al cliente", Toast.LENGTH_SHORT).show()
            }
        }else{
            valiTel = ValiCampos.validarTel(txtAdminTel.text, this)
            if(valiNam && valiCorr && valiCon && valiPreg && valiResp && valiSis && valiTel){
                // Usuario administrador
                auth.createUserWithEmailAndPassword(emaLimp,pasLimp).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        // Obteniendo el ID generada por el objeto de autenticacion
                        val authUs = auth.currentUser
                        // Obteniendo el valor de la pregunta seleccionada
                        val pregSel = spSafQuKyReg.selectedItem.toString()
                        // Preparando la informacion del registro para que se guarde con una key generada por firebase autenticacion
                        val nUser = if(selPin){    // Si se trae pin, aqui es donde sera agregado para su almacenado
                            valiPin = ValiCampos.validarPin(txtPin.text, this)
                            Usuario(id_Usuario=authUs!!.uid,nombre=txtName.text.toString(),correo=emaLimp,tipo_Usuario="Administrador",num_Tel=txtAdminTel.text.toString().toLong(),preg_Seguri=pregSel,resp_Seguri=txtRespQues.text.toString(),pin_Pass=txtPin.text.toString().toInt())
                        }else{
                            Usuario(id_Usuario=authUs!!.uid,nombre=txtName.text.toString(),correo=emaLimp,tipo_Usuario="Administrador",num_Tel=txtAdminTel.text.toString().toLong(),preg_Seguri=pregSel,resp_Seguri=txtRespQues.text.toString(),pin_Pass=0)
                        }
                        // Estableciendo el nuevo valor del usuario usando un child en la coleccion de usuarios
                        database.reference.child("Usuarios").child(authUs.uid).setValue(nUser)
                        // Creando los elementos para relacionar el usuario con el sistema
                        val sisRef = Firebase.database.getReference("Sistema")
                        sisRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for (objSis in dataSnapshot.children){
                                    val sisJSON = gson.toJson(objSis.value)
                                    val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                                    // Si el sistema selecionado corresponde con el registro de firebase se extraera el valor para ingresarlo en user_sistems
                                    if(spSisRel.selectedItem.toString() == resSis.nombre_Sis){
                                        val sisUsRef = Firebase.database.getReference("User_Sistems")
                                        val addUserSisDB = sisUsRef.push()
                                        val nRelSisUs = UserSistem(id_User_Sis=addUserSisDB.key.toString(),sistema_Nom=resSis.nombre_Sis,user_Email=authUs.email!!)
                                        addUserSisDB.setValue(nRelSisUs)
                                    }
                                    // Se procede a lanzar al usuario a la activity de dashboard
                                    Toast.makeText(applicationContext, "Bienvenido a Ardu Security ${txtName.text}", Toast.LENGTH_SHORT).show()
                                    // Una vez que se autentico y registro en firebase, lo unico que queda es lanzarlo hacia el dashboard enviando como extra usuario y contraseña
                                    val intentDash = Intent(applicationContext, DashboardActivity::class.java).apply {
                                        putExtra("correo", txtEmail.text.toString())
                                        putExtra("contra", txtPass.text.toString())
                                    }
                                    startActivity(intentDash)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }
                }
            }else{
                Toast.makeText(this, "Error: No se ha podido autenticar al administrador", Toast.LENGTH_SHORT).show()
            }
        }
    }

    object ValiCampos{
        fun validarNombre(nombre: Editable, contexto: Context): Boolean{
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
        fun validarCorreo(correo: Editable, contexto: Context): Boolean{
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
        fun validarContra(contra: Editable, contexto: Context): Boolean{
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
        fun validarSelPreg(lista: Spinner, contexto: Context): Boolean {
            if(lista.selectedItemPosition == 0){
                Toast.makeText(contexto, "Error: Favor de seleccionar una pregunta", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
        fun validarResp(respuesta: Editable, contexto: Context): Boolean {
            if(TextUtils.isEmpty(respuesta)){
                Toast.makeText(contexto, "Error: Favor de introducir una respuesta para su pregunta", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
        fun validarSelSis(lista: Spinner, contexto: Context): Boolean {
            if(lista.selectedItemPosition == 0){
                Toast.makeText(contexto, "Error: Favor de seleccionar un sistema", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
        fun validarPin(Pin: Editable, contexto: Context): Boolean {
            when {
                TextUtils.isEmpty(Pin) -> Toast.makeText(contexto, "Error: Favor de introducir un pin", Toast.LENGTH_SHORT).show()
                (Regex("""\D""").containsMatchIn(Pin)) -> Toast.makeText(contexto, "Error: El pin de acceso solo puede contener digitos", Toast.LENGTH_SHORT).show()
                (Pin.length < 4) -> Toast.makeText(contexto, "Advertencia: Se recomienda un pin numerico de al menos 4 digitos", Toast.LENGTH_SHORT).show()
                else -> return true
            }
            return false
        }
        fun validarTel(numTel: Editable, contexto: Context): Boolean {
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
    }
}