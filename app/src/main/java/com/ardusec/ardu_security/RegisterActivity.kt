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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Constante para la obtencion de la IP establecida desde los strings
        val dirIP = getString(R.string.ip)
        // Creacion de constante para peticiones Volley
        val queue = Volley.newRequestQueue(this)

        // Mensaje de bienvenida para decirle al usuario que debe hacer
        Toast.makeText(applicationContext,"Ingrese los datos que se solicitan",Toast.LENGTH_LONG).show()

        // Obtencion de los elementos editables de la vista
        val txtName: EditText = findViewById(R.id.txtName)
        val txtEmail: EditText = findViewById(R.id.txtEmailReg)
        val txtPass: EditText = findViewById(R.id.txtPassReg)
        val chbVerContra: CheckBox = findViewById(R.id.chbPassReg)
        val spSafQuKyReg: Spinner = findViewById(R.id.spSafQuKyReg)
        val txtRespQues: EditText = findViewById(R.id.txtRespQues)
        val rbSelCli: RadioButton = findViewById(R.id.rbTipUsCli)
        val rbSelAdmin: RadioButton = findViewById(R.id.rbTipUsAdmin)
        val txtAdminTel: EditText = findViewById(R.id.txtTel)
        val txtNamSis: EditText = findViewById(R.id.txtSisNam)
        val btnReg: Button = findViewById(R.id.btnRegister)
        // Banderas de validacion
        var valiNam: Boolean; var valiCorr: Boolean; var valiCon: Boolean
        var valiPreg: Boolean; var valiResp: Boolean;
        // Los admin seran 1 y los clientes 2
        var tipUser: Int = 0; var valiTel: Boolean; var valiNomSis: Boolean

        //----------------------Rellenar los valores del Spinner------------------------------------
        val opcs = resources.getStringArray(R.array.lstSavQues)
        var arreglo = ArrayList<String>()
        arreglo.addAll(opcs)
        // Variables y constantes para Volley
        val urlPregs = "$dirIP/consultaInfoFull?tabla=Pregunta"
        val stringRequest = StringRequest(Request.Method.GET, urlPregs,
            { response ->
                val jsonArray = JSONTokener(response).nextValue() as JSONArray
                // Obteniendo los datos del JSON regresado de la BD
                for (cont in 0 until jsonArray.length())
                    arreglo.add(jsonArray.getJSONObject(cont).getString("Val_Pregunta"))
                val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, arreglo)
                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spSafQuKyReg.adapter = adaptador
            },
            { error -> Toast.makeText(this, "Se rompio esta cosa porque $error", Toast.LENGTH_SHORT).show() }
        )
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        //------------------------------------------------------------------------------------------
        //Agregar los listener
        rbSelCli.setOnClickListener {
            if(rbSelCli.isChecked){
                txtAdminTel.isGone = true
                txtNamSis.isGone = true
                // Estableciendo el tipo de usuario cliente
                tipUser = 2
            }
        }
        rbSelAdmin.setOnClickListener {
            if(rbSelAdmin.isChecked){
                txtAdminTel.isGone = false
                txtNamSis.isGone = false
                tipUser = 1
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
                if(tipUser == 1){
                    valiTel = Validacion.validarTel(txtAdminTel.text, this)
                    valiNomSis = Validacion.validarNomSis(txtNamSis.text, this)
                    // Si las validaciones correspondientes pasaron se procede con el registro del admin
                    if(valiTel && valiNomSis) {
                        // URL de peticion Volley hacia Flask
                        val urlRegi = "$dirIP/registrarAdmin"
                        // Peticion String Request
                        val stringRequest = object : StringRequest(Request.Method.POST, urlRegi,
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
                        queue.add(stringRequest)
                    }
                }else{
                    // URL de peticion Volley hacia Flask
                    val urlRegi = "$dirIP/registrarCliente"
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
                    queue.add(stringRequest)
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