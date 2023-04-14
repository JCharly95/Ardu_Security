package com.ardusec.ardu_security

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Constante para la obtencion de la IP establecida desde los strings
        val dirIP = getString(R.string.ip)
        // Obtener los campos de texto
        val txtEmail: EditText = findViewById(R.id.txtEmail)
        val txtContra: EditText = findViewById(R.id.txtPass)
        //val txtCopy: TextView = findViewById(R.id.txtCopyright)
        // Obtener los botones de interaccion en la interfaz
        val chbVerContra: CheckBox = findViewById(R.id.chbPass)
        val btnLostPass: Button = findViewById(R.id.btnLstContra)
        val btnAcceder: Button = findViewById(R.id.btnLogin)
        val btnRegister: Button = findViewById(R.id.btnRegView)
        // Variables y constantes de validacion: Banderas
        var valiCorr: Boolean
        var valiContra: Boolean

        // Agregar los listener de los botones
        btnLostPass.setOnClickListener{
            val intentLost = Intent(applicationContext,ForgotPassActivity::class.java)
            startActivity(intentLost)
        }
        chbVerContra.setOnClickListener{
            if(!chbVerContra.isChecked){
                txtContra.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnAcceder.setOnClickListener{
            // Validacion de correo
            valiCorr = Validacion.validarCorreo(txtEmail, applicationContext)
            // Validacion de contraseña
            valiContra = Validacion.validarContra(txtContra, applicationContext)
            // Si todas las validaciones coincidieron se busca al usuario en la base de datos
            if(valiCorr && valiContra){
                ProcesoVolley.peticionVolley(dirIP,txtEmail, txtContra, applicationContext)
            }
        }
        btnRegister.setOnClickListener{
            val intentRegister = Intent(applicationContext,RegisterActivity::class.java)
            startActivity(intentRegister)
        }
    }

    object Validacion {
        fun validarCorreo(correo: EditText, contexto: Context): Boolean{
            // Switch de condiciones para el correo
            when {
                // Si el correo esta vacio
                TextUtils.isEmpty(correo.text) -> Toast.makeText(contexto, "Favor de introducir un correo", Toast.LENGTH_SHORT).show()
                // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.text).matches() -> Toast.makeText(contexto, "Favor de introducir un correo valido", Toast.LENGTH_SHORT).show()
                else -> return true
            }
            return false
        }
        fun validarContra(contra: EditText, contexto: Context): Boolean{
            // Switch de condiciones para la contraseña
            when {
                // Si la contraseña esta vacia
                TextUtils.isEmpty(contra.text) -> Toast.makeText(contexto, "Favor de introducir una contraseña", Toast.LENGTH_SHORT).show()
                // Extension minima de 8 caracteres
                (contra.text.length < 8) -> Toast.makeText(contexto, "Error: la contraseña debera tener una extension minima de 8 caracteres", Toast.LENGTH_SHORT).show()
                // No se tiene al menos una mayuscula
                (!Regex("[A-Z]+").containsMatchIn(contra.text)) -> Toast.makeText(contexto, "Error: la contraseña debera tener al menos una letra mayuscula", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un numero
                (!Regex("""\d""").containsMatchIn(contra.text)) -> Toast.makeText(contexto, "Error: la contraseña debera tener al menos un numero", Toast.LENGTH_SHORT).show()
                // No se tiene al menos un caracter especial
                (!Regex("[^A-Za-z0-9]+").containsMatchIn(contra.text)) -> Toast.makeText(contexto, "Error: favor de incluir al menos un caracter especial en su contraseña", Toast.LENGTH_SHORT).show()
                else -> return true
            }
            return false
        }
    }

    object ProcesoVolley {
        // Si las validaciones coincidieron se crea la instancia de un objeto que genera la peticion volley y lanza la peticion
        fun peticionVolley(ip: String, correo: EditText, contra: EditText, contexto: Context){
            // Variables y constantes para Volley
            val url = "$ip/consultaInfoFull?tabla=Usuarios"
            val queue = Volley.newRequestQueue(contexto)
            val userData = JSONObject()

            val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    val jsonArray  = JSONTokener(response).nextValue() as JSONArray
                    // Obteniendo los datos del JSON regresado de la BD
                    for (cont in 0 until jsonArray.length()){
                        // Cuando el usuario sea encontrado por su correo y contraseña, se hara un JSON con sus datos
                        if(correo.text.toString() == jsonArray.getJSONObject(cont).getString("Correo") && contra.text.toString() == jsonArray.getJSONObject(cont).getString("Contrasenia")){
                            userData.put("nombre",jsonArray.getJSONObject(cont).getString("Nombre"))
                            userData.put("correo",correo.text)
                            // Aqui se debe hacer una subconsulta para saber el tipo de usuario, por lo pronto igual se jalara
                            userData.put("tipoUs",jsonArray.getJSONObject(cont).getString("UserTip_ID"))
                            // Aqui se debe hacer una subconsulta para saber la pregunta del usuario, por lo pronto igual se jalara
                            userData.put("ipPregSel",jsonArray.getJSONObject(cont).getString("Pregunta_ID"))
                            userData.put("respPreg",jsonArray.getJSONObject(cont).getString("Resp_Preg_Seg"))
                            userData.put("pinPass",jsonArray.getJSONObject(cont).getString("Pin_Pass"))
                            // Ya que el usuario fue encontrado procede a lanzarse la actividad de login mandando los datos serializados del JSON obtenido con los datos del usuario
                            val intentLogin = Intent(contexto,DashboardActivity::class.java)
                            intentLogin.putExtra("UserData", userData.toString())
                            startActivity(contexto, intentLogin, null)
                            break
                        }
                    }
                },
                { error ->
                    Toast.makeText(contexto, "Se rompio esta cosa porque $error", Toast.LENGTH_SHORT).show()
                }
            )
            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
    }
}