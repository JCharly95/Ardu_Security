package com.ardusec.ardu_security

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class ForgotPassActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        // Constante para la obtencion de la IP establecida desde los strings
        val dirIP = getString(R.string.ip)
        // Creacion de constante para peticiones Volley
        val queue = Volley.newRequestQueue(this)
        // Direccion de correo
        var direccionCor: String

        // Establecer los botones del formulario
        val rbRecPasEma: RadioButton = findViewById(R.id.rbSelPassEma)
        val rbRecPasQue: RadioButton = findViewById(R.id.rbSelQuesSav)
        val txtEmaForPwd: EditText = findViewById(R.id.txtEmaForPass)
        val btnConfEma: Button = findViewById(R.id.btnConfEma)
        val respSelRec: TextView = findViewById(R.id.lblRespSel)
        val spPregKey: Spinner = findViewById(R.id.spSafQuKy)
        val txtResPreg: EditText = findViewById(R.id.txtResPregKey)
        val btnConfVeri: Button = findViewById(R.id.btnChResp)
        val respVerRes: TextView = findViewById(R.id.lblRespVeri)

        //----------------------Rellenar los valores del Spinner de seleccion de pregunta-----------
        val opcs = resources.getStringArray(R.array.lstSavQues)
        var arreglo = ArrayList<String>()
        arreglo.addAll(opcs)
        // Variables y constantes para Volley
        val urlPregs = "$dirIP/consultaInfoFull?tabla=Pregunta"
        val stringRequest = StringRequest(
            Request.Method.GET, urlPregs,
            { response ->
                val jsonArray = JSONTokener(response).nextValue() as JSONArray
                // Obteniendo los datos del JSON regresado de la BD
                for (cont in 0 until jsonArray.length())
                    arreglo.add(jsonArray.getJSONObject(cont).getString("Val_Pregunta"))
                val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, arreglo)
                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPregKey.adapter = adaptador
            },
            { error -> Toast.makeText(this, "Se rompio esta cosa porque $error", Toast.LENGTH_SHORT).show() }
        )
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        //------------------------------------------------------------------------------------------
        //ActionListener para la confirmacion de seleccion
        rbRecPasEma.setOnClickListener {
            if(rbRecPasEma.isChecked){
                // Mostrar el campo para ingresar el correo, el boton para verificarlo y el campo de respuesta
                txtEmaForPwd.isVisible = true
                btnConfEma.isVisible = true
                respSelRec.isVisible = true

                // Establecer spinner y volverlo invisible
                spPregKey.isVisible = false
                // Aqui debo incluir un arreglo con los elementos para el spinner

                // Establecer como invisible el label de respuesta y el campo para responder
                txtResPreg.isVisible = false

                // Establecer como invisible el boton de verificacion de respuesta y el label de respuesta
                btnConfVeri.isVisible = false
                respVerRes.isVisible = false
            }
        }

        btnConfEma.setOnClickListener {
            // Establecer la respuesta de la seleccion y volverla visible
            respSelRec.setText("Enviando contraseña a su correo...")
            respSelRec.isVisible = true

            // Llamar a la funcion para obtener el correo y la contra desde la bd y ejecutar el envio del contraseña
            //recPassEma()
            ProcesoVolleyForPass.peticionVolley(dirIP,txtEmaForPwd,applicationContext)
        }

        rbRecPasQue.setOnClickListener {
            if(rbRecPasQue.isChecked){
                /* Ocultar el campo de la seleccion de radios (desapareciendo el campo del area)
                val rbGroup: RadioGroup = findViewById(R.id.rbgSelForPass)
                rbGroup.isGone = true*/

                // Establecer la respuesta de la seleccion y volverla visible
                respSelRec.setText("Favor de seleccionar su pregunta clave")
                respSelRec.isVisible = true

                // Establecer los elementos del spinner y volverlo visible
                spPregKey.isVisible = true
                // Aqui debo incluir un arreglo con los elementos para el spinner

                // Establecer como visible el label de respuesta y el campo para responder
                txtResPreg.isVisible = true

                // Establecer como visible el boton de verificacion de respuesta y el label de respuesta
                btnConfVeri.isVisible = true
                respVerRes.setText("Esperando respuesta...")
                respVerRes.isVisible = true
            }
        }
    }

    object ProcesoVolleyForPass {
        // Si las validaciones coincidieron se crea la instancia de un objeto que genera la peticion volley y lanza la peticion
        fun peticionVolley(ip: String, correo: EditText, contexto: Context){
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
                        if(correo.text.toString() == jsonArray.getJSONObject(cont).getString("Correo")){

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
                            ContextCompat.startActivity(contexto, intentLogin, null)
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

    fun restartPasEma(){

    }
}