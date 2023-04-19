package com.ardusec.ardu_security

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.text.Editable
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.Objects
import kotlin.reflect.typeOf

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
    private lateinit var txtAdminTel: EditText
    private lateinit var spSisRel: Spinner
    private lateinit var btnReg: Button
    // Instancias de Firebase; Autenticacion, Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    // Banderas de validacion
    private var valiNam = false
    private var valiCorr = false
    private var valiCon = false
    private var valiPreg = false
    private var valiResp = false
    private var valiSis = false
    private var valiTel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializando los elementos
        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmailReg)
        txtPass = findViewById(R.id.txtPassReg)
        chbVerContra = findViewById(R.id.chbPassReg)
        spSafQuKyReg = findViewById(R.id.spSafQuKyReg)
        txtRespQues = findViewById(R.id.txtRespQues)
        rbSelCli = findViewById(R.id.rbTipUsCli)
        rbSelAdmin = findViewById(R.id.rbTipUsAdmin)
        txtAdminTel = findViewById(R.id.txtTel)
        spSisRel = findViewById(R.id.spSistema)
        btnReg = findViewById(R.id.btnRegister)
        // Inicializando la autenticacion y creando la instancia de la BD
        auth = Firebase.auth
        database = Firebase.database

        // Mensaje de bienvenida para decirle al usuario que debe hacer
        Toast.makeText(applicationContext,"Ingrese los datos que se solicitan",Toast.LENGTH_LONG).show()

        //-------------------Rellenar los valores del Spinner de preguntas--------------------------
        // Creando un objeto Gson para su uso tanto de algo a JSON como de JSON a objeto
        val gson = Gson()
        // Obtener el arreglo de strings establecido para las preguntas
        val preguntas = resources.getStringArray(R.array.lstSavQues)
        // Crear y establecer un ArrayList de strings para agregar las nuevas preguntas extraidas de firebase
        var arreglo = ArrayList<String>()
        arreglo.addAll(preguntas)
        // Creando la referencia de la coleccion de preguntas en la BD
        ref = database.getReference("Pregunta")
        // Obteniendo los valores de la coleccion de preguntas y ordenados por el valor del ID de Firebase
        ref.orderByKey().get().addOnSuccessListener {
            // Creando una data class (es como una clase virtual de kotlin)
            data class Pregunta(val ID_Pregunta: String, val Val_Pregunta: String)
            // Creando un HashMap que contendra los valores retornados de firebase (clave, objeto)
            val coleccion = it.value as HashMap<String, Pregunta>
            // Recorrer el HasMap para buscar los elementos contenidos en el
            for(key in coleccion){
                // Transformacion a JSON del objeto obtenido con la informacion
                val pregJSON = gson.toJson(key.value)
                // Transformacion de JSON un objeto de tipo Pregunta para su iteracion
                val res = gson.fromJson(pregJSON, Pregunta::class.java)
                arreglo.add(res.Val_Pregunta)
            }
            // Estableciendo el adaptador para el rellenado del spinner
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, arreglo)
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSafQuKyReg.adapter = adaptador
        }.addOnFailureListener{
            Toast.makeText(this,"Error: Preguntas no obtenidas", Toast.LENGTH_SHORT).show()
        }
        //------------------------------------------------------------------------------------------
        //-------------------Rellenar los valores del Spinner de sistemas---------------------------
        // Obtener el arreglo de strings establecido para las preguntas
        val sistemas = resources.getStringArray(R.array.lstSistems)
        // Crear y establecer un ArrayList de strings para agregar las nuevas preguntas extraidas de firebase
        var arreglo2 = ArrayList<String>()
        arreglo2.addAll(sistemas)
        // Creando la referencia de la coleccion de preguntas en la BD
        ref = database.getReference("Sistema")
        // Obteniendo los valores de la coleccion de preguntas y ordenados por el valor del ID de Firebase
        ref.orderByKey().get().addOnSuccessListener {
            // Creando una data class (es como una clase virtual de kotlin)
            data class Sistema(val ID_Sistema: String, val Nombre_Sis: String, val Ulti_Cam_Nom: String)
            // Creando un HashMap que contendra los valores retornados de firebase (clave, objeto)
            val coleccion = it.value as HashMap<String, Sistema>
            // Recorrer el HasMap para buscar los elementos contenidos en el
            for(key in coleccion){
                // Transformacion a JSON del objeto obtenido con la informacion
                val pregJSON = gson.toJson(key.value)
                // Transformacion de JSON un objeto de tipo Sistema para su iteracion
                val res = gson.fromJson(pregJSON, Sistema::class.java)
                arreglo2.add(res.Nombre_Sis)
            }
            // Estableciendo el adaptador para el rellenado del spinner
            val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, arreglo2)
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSisRel.adapter = adaptador
        }.addOnFailureListener{
            Toast.makeText(this,"Error: Sistemas no encontrados", Toast.LENGTH_SHORT).show()
        }
        //------------------------------------------------------------------------------------------
        //Agregar los listener
        rbSelCli.setOnClickListener {
            if(rbSelCli.isChecked){
                txtAdminTel.isGone = true
            }
        }
        rbSelAdmin.setOnClickListener {
            if(rbSelAdmin.isChecked){
                txtAdminTel.isGone = false
            }
        }
        chbVerContra.setOnClickListener{
            if(!chbVerContra.isChecked){
                txtPass.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnReg.setOnClickListener {
            valiNam = Validacion.validarNombre(txtName.text, this)
            valiCorr = Validacion.validarCorreo(txtEmail.text, this)
            valiCon = Validacion.validarContra(txtPass.text, this)
            valiPreg = Validacion.validarSelPreg(spSafQuKyReg, this)
            valiResp = Validacion.validarResp(txtRespQues.text, this)
            // Se creara un JSON Array con los datos del usuario, para su manipulacion dentro de la app
            val userData = JSONObject()

            // Registrar si las validaciones coincidieron
            if(valiNam && valiCorr && valiCon && valiPreg && valiResp){
                // Si se va a registrar un admin, el proceso sera un poco diferente ya que tambien sera necesario validar los datos que se ingresen de su parte adicional
                if(rbSelAdmin.isChecked){
                    valiTel = Validacion.validarTel(txtAdminTel.text, this)
                    valiSis = Validacion.validarSelSis(spSisRel, this)
                    // Si las validaciones correspondientes pasaron se procede con el registro del admin
                    if(valiTel && valiSis) {
                        // URL de peticion Volley hacia Flask
                        //val urlRegi = "$dirIP/registrarAdmin"
                        // Peticion String Request
                        /*val stringRequest = object : StringRequest(Request.Method.POST, urlRegi,
                            { response ->
                                try {
                                    Toast.makeText(this,"El administrador fue registrado!", Toast.LENGTH_SHORT).show()
                                    // Agregando los elementos al objeto JSON de valores del usuario
                                    userData.put("nombre",txtName.text.toString())
                                    userData.put("correo",txtEmail.text.toString())
                                    userData.put("contra",txtPass.text.toString())
                                    userData.put("tipoUs",tipUser.toString())
                                    userData.put("pregSel",spSafQuKyReg.selectedItem.toString())
                                    userData.put("respPreg",txtRespQues.text.toString())
                                    userData.put("telAdm",txtAdminTel.text.toString())
                                    userData.put("nomSis",txtNamSis.text.toString())
                                    // Creacion del Intent para lanzar la actividad del dashboard junto con el extra
                                    val intentRegistro = Intent(this,DashboardActivity::class.java)
                                    intentRegistro.putExtra("UserDataReg", userData.toString())
                                    startActivity(intentRegistro)
                                } catch (e: JSONException) {
                                    Toast.makeText(this,
                                        "Se sucito un problema al registrar; $e",Toast.LENGTH_SHORT).show()
                                }
                            }, { error ->
                                Toast.makeText(this,
                                    "Error: Registro fallido; resultado del error: $error", Toast.LENGTH_SHORT).show()
                            }){
                            override fun getParams(): Map<String, String> {
                                // Parametros de envio hacia la peticion
                                val params = HashMap<String, String>()
                                params["Nombre"] = txtName.text.toString()
                                params["Correo"] = txtEmail.text.toString()
                                params["Contrasenia"] = txtPass.text.toString()
                                params["UserTip_ID"] = tipUser.toString()
                                params["Pregunta"] = spSafQuKyReg.selectedItem.toString()
                                params["Resp_Preg"] = txtRespQues.text.toString()
                                params["TelAdmin"] = txtAdminTel.text.toString()
                                params["NomSis"] = txtNamSis.text.toString()
                                return params
                            }
                        }
                        queue.add(stringRequest)*/
                    }
                }else{
                    // URL de peticion Volley hacia Flask
                    /*val urlRegi = "$dirIP/registrarCliente"
                    // Peticion String Request
                    val stringRequest = object : StringRequest(Request.Method.POST, urlRegi,
                        { response ->
                            try {
                                Toast.makeText(this,"El usuario fue registrado!", Toast.LENGTH_SHORT).show()
                                // Agregando los elementos al objeto JSON de valores del usuario
                                userData.put("nombre",txtName.text.toString())
                                userData.put("correo",txtEmail.text.toString())
                                userData.put("contra",txtPass.text.toString())
                                userData.put("tipoUs",tipUser.toString())
                                userData.put("pregSel",spSafQuKyReg.selectedItem.toString())
                                userData.put("respPreg",txtRespQues.text.toString())
                                // Creacion del Intent para lanzar la actividad del dashboard junto con el extra
                                val intentRegistro = Intent(this,DashboardActivity::class.java)
                                intentRegistro.putExtra("UserDataReg", userData.toString())
                                startActivity(intentRegistro)
                            } catch (e: JSONException) {
                                Toast.makeText(this,"Se sucito un problema al registrar; $e",Toast.LENGTH_SHORT).show()
                            }
                        }, { error ->
                            error.printStackTrace()
                            Toast.makeText(this,"Error: Registro fallido; resultado del error: $error", Toast.LENGTH_SHORT).show()
                        }){
                        override fun getParams(): Map<String, String> {
                            // Parametros de envio hacia la peticion
                            val params = HashMap<String, String>()
                            params["Nombre"] = txtName.text.toString()
                            params["Correo"] = txtEmail.text.toString()
                            params["Contrasenia"] = txtPass.text.toString()
                            params["UserTip_ID"] = tipUser.toString()
                            params["Pregunta"] = spSafQuKyReg.selectedItem.toString()
                            params["Resp_Preg"] = txtRespQues.text.toString()
                            return params
                        }
                    }
                    queue.add(stringRequest)*/
                }
            }
        }
    }

    object Validacion {
        fun validarNombre(nombre: Editable, contexto: Context): Boolean {
            when {
                // Si el nombre esta vacio
                TextUtils.isEmpty(nombre) -> Toast.makeText(contexto, "Error: Favor de introducir un nombre", Toast.LENGTH_SHORT).show()
                // Si se encuentra algun numero
                (Regex("""\d+""").containsMatchIn(nombre)) -> Toast.makeText(contexto, "Error: Su nombre no puede contener numeros", Toast.LENGTH_SHORT).show()
                // Si el nombre es mas corto a 10 caracteres (tomando como referencia de los nombres mas cortos posibles: Juan Lopez)
                (nombre.length < 10) -> Toast.makeText(contexto, "Advertencia: Su nombre es muy corto, favor de agregar su nombre completo", Toast.LENGTH_SHORT).show()
                // Si se encuentran caracteres especiales
                (Regex("""[^A-Za-z ]+""").containsMatchIn(nombre)) -> Toast.makeText(contexto, "Error: Su nombre no puede contener caracteres especiales", Toast.LENGTH_SHORT).show()
                else -> return true
            }
            return false
        }
        fun validarCorreo(correo: Editable, contexto: Context): Boolean {
            // Si se detectan espacios en el correo, estos seran removidos
            if(Regex("""\s+""").containsMatchIn(correo)){
                val correoFil = correo.replace("\\s".toRegex(), "")
                when {
                    // Si el correo esta vacio
                    TextUtils.isEmpty(correoFil) -> Toast.makeText(contexto, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                    // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil).matches() -> Toast.makeText(contexto, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                    else -> return true
                }
            }else{
                when {
                    // Si el correo esta vacio
                    TextUtils.isEmpty(correo) -> Toast.makeText(contexto, "Error: Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                    // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> Toast.makeText(contexto, "Error: Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                    else -> return true
                }
            }
            return false
        }
        fun validarContra(contra: Editable, contexto: Context): Boolean {
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
        fun validarNomSis(nomSis: Editable, contexto: Context): Boolean {
            when {
                // Si el nombre del sistema esta vacio
                TextUtils.isEmpty(nomSis) -> Toast.makeText(contexto, "Error: Favor de introducir el nombre que tendra su sistema", Toast.LENGTH_SHORT).show()
                // Se optara por una longitud minima de 5 caracteres contemplado de cualquier tipo
                (nomSis.length < 5) -> Toast.makeText(contexto, "Advertencia: Se recomienda un nombre de mas de 5 caracteres", Toast.LENGTH_SHORT).show()
                else -> return true
            }
            return false
        }
    }
}