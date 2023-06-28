package com.ardusec.ardu_security

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class ResetPassActivity : AppCompatActivity(){
    // Estableciendo los elementos de interaccion
    private lateinit var txtNombre: EditText
    private lateinit var btnHelp: Button
    private lateinit var txtDirEma: EditText
    private lateinit var spPreguntas: Spinner
    private lateinit var txtResPreg: EditText
    private lateinit var btnEnvCorr: Button
    private lateinit var lblMsgSta: TextView
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var auth: FirebaseAuth
    // Creando el objeto GSON
    private var gson = Gson()
    // Banderas de validacion
    private var valiNam = false
    private var valiCorr = false
    private var valiPreg = false
    private var valiResp = false

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pass)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun rellSpinPregs(){
        val lstPregs = resources.getStringArray(R.array.lstSavQues)
        var arrPregs = ArrayList<String>()
        arrPregs.addAll(lstPregs)

        ref = database.getReference("Pregunta")
        data class Pregunta(val ID_Pregunta: String, val Val_Pregunta: String)

        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objPreg in dataSnapshot.children){
                    val pregJSON = gson.toJson(objPreg.value)
                    val resPreg = gson.fromJson(pregJSON, Pregunta::class.java)
                    arrPregs.add(resPreg.Val_Pregunta)
                }
                val adapPregs = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrPregs)
                adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPreguntas.adapter = adapPregs
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Recuperar Contraseña"
        // Relacionando los elementos con su objeto de la interfaz
        txtNombre = findViewById(R.id.txtNomForPass)
        btnHelp = findViewById(R.id.btnInfoRecPass)
        txtDirEma = findViewById(R.id.txtEmaForPass)
        spPreguntas = findViewById(R.id.spSafQuKy)
        txtResPreg = findViewById(R.id.txtResPregKey)
        btnEnvCorr = findViewById(R.id.btnEnviar)
        lblMsgSta = findViewById(R.id.lblRespPeti)
        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Invocacion a la funcion para rellenar el spinner de las preguntas
        rellSpinPregs()
    }

    private fun avisoForPass(){
        val mensaje = "Consideraciones de campos: \n\n" +
                "Nombre;\n" +
                "* Su nombre no debe tener numeros\n" +
                "* Su nombre debe tener al menos 10 caracteres\n\n" +
                "Correo; Formato Aceptado:\n" +
                "* usuario@dominio.com(.mx)"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        btnHelp.setOnClickListener {
            avisoForPass()
        }
        // Añadiendo el listener del boton de envio
        btnEnvCorr.setOnClickListener {
            validaciones()
        }
    }

    private fun validaciones(){
        if(ValiCampos.validarNombre(txtNombre.text, this)){
            valiNam = true
        }
        if(ValiCampos.validarCorreo(txtDirEma.text, this)){
            valiCorr = true
        }
        if(ValiCampos.validarSelPreg(spPreguntas, this)){
            valiPreg = true
        }
        if(ValiCampos.validarResp(txtResPreg.text, this)){
            valiResp = true
        }
        // Si todo fue validado de manera satisfactoria se procedera a buscar la informacion en la BD
        if(valiNam && valiCorr && valiPreg && valiResp){
            data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
            ref = database.getReference("Usuarios")
            ref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot){
                    for (objUser in dataSnapshot.children){
                        val userJSON = gson.toJson(objUser.value)
                        val resUser = gson.fromJson(userJSON, Usuario::class.java)
                        // Si el usuario es encontrado en la BD por todos los elementos solicitados en el formulario, se hace el envio del correo
                        if(txtNombre.text.toString() == resUser.nombre && txtDirEma.text.toString() == resUser.correo && spPreguntas.selectedItem.toString() == resUser.preg_Seguri && txtResPreg.text.toString() == resUser.resp_Seguri){
                            // Preparar el correo para su envio
                            val emaLimp = txtDirEma.text.toString().trim()
                            // Solicitar a firebase que mande el correo de recuperacion y establecer el idioma de envio
                            auth.setLanguageCode("es_419")
                            auth.sendPasswordResetEmail(emaLimp).addOnCompleteListener { task ->
                                if(task.isSuccessful){
                                    lblMsgSta.text = "Correo de recuperacion enviado, favor de revisar su correo"
                                    lblMsgSta.isGone = false
                                }else{
                                    lblMsgSta.text = "Error: No se pudo enviar el correo de recuperacion"
                                    lblMsgSta.isGone = false
                                }
                            }
                            // Cuando ya se envio el correo de recuperacion, se le enviara a la pantalla de inicio
                            val intentMain = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intentMain)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                }
            })
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
    }
}