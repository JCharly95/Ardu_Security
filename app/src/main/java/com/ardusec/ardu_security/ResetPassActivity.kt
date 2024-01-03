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
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class ResetPassActivity : AppCompatActivity(){
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtUserRec: TextInputEditText
    private lateinit var txtEmaRec: TextInputEditText
    private lateinit var spPregsRec: AppCompatSpinner
    private lateinit var txtRespRec: TextInputEditText
    private lateinit var btnRecPass: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference
    private lateinit var auth: FirebaseAuth
    // Banderas de validacion
    private var valiUser = false
    private var valiCorr = false
    private var valiPreg = false
    private var valiResp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pass)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
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
                        val adapPregs = ArrayAdapter(this@ResetPassActivity, android.R.layout.simple_spinner_item, arrPregs)
                        adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spPregsRec.adapter = adapPregs
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ResetPassActivity, "Error: Consulta Incompleta; Causa: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellPregs.await()
        }
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Recuperar Contraseña"
        // Relacionando los elementos con su objeto de la interfaz
        btnAyuda = findViewById(R.id.btnInfoRecPass)
        txtUserRec = findViewById(R.id.txtUserForPass)
        txtEmaRec = findViewById(R.id.txtEmaForPass)
        spPregsRec = findViewById(R.id.spPregsSegForPass)
        txtRespRec = findViewById(R.id.txtResPregForPass)
        btnRecPass = findViewById(R.id.btnSendRecPass)

        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()
        // Invocacion a la funcion para rellenar el spinner de las preguntas
        rellSpinPregs()
    }

    private fun avisoForPass(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        // Agregar los listener
        btnAyuda.setOnClickListener {
            val msgAyuda = "Requisitos necesarios de cada campo: \n\n" +
                    "Username (Nombre de usuario):\n" +
                    "* Debe tener una extensión mínima de 6 caracteres.\n" +
                    "* Debe tener por lo menos una letra mayúscula.\n" +
                    "* Debe tener por lo menos el digito de un número.\n" +
                    "Dirección de Correo:\n" +
                    "* La estructura aceptada es:\n" +
                    "-- usuario@dominio (.extensión de país; esto es opcional ingresarlo solo si su dirección lo contiene)\n\n" +
                    "Pregunta de Seguridad:\n" +
                    "* Debe seleccionar cualquiera de las 5 preguntas de seguridad disponibles.\n\n" +
                    "Respuesta de Seguridad:\n" +
                    "* Debe ingresar una respuesta a la pregunta seleccionada, esta será de formato libre.\n\n"
            avisoForPass(msgAyuda)
        }
        // Añadiendo el listener del boton de envio
        btnRecPass.setOnClickListener {
            buscarUsBD()
        }
    }

    private fun validarUsuario(usuario: Editable): Boolean{
        // Si se detectan espacios en blanco en el usuario, seran removidos
        val usuarioFil1 = usuario.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val usuarioFil2 = usuarioFil1.replace("\\p{Zs}+".toRegex(), "")
        when {
            // Si el usuario esta vacia
            TextUtils.isEmpty(usuarioFil2) -> avisoForPass("Error: Favor de introducir un nombre de usuario")
            // Extension minima de 8 caracteres
            (usuarioFil2.length < 6) -> avisoForPass("Error: El nombre de usuario debera tener una extension minima de 6 caracteres")
            // No se tiene al menos una mayuscula
            (!Regex("[A-Z]+").containsMatchIn(usuarioFil2)) -> avisoForPass("Error: El nombre de usuario debera tener al menos una letra mayuscula")
            // No se tiene al menos un numero
            (!Regex("""\d""").containsMatchIn(usuarioFil2)) -> avisoForPass("Error: El nombre de usuario debera tener al menos un numero")
            else -> return true
        }
        return false
    }
    private fun validarCorreo(correo: Editable): Boolean{
        // Si se detectan espacios en el correo, estos seran removidos
        val correoFil1 = correo.replace("\\s".toRegex(), "")
        // Si se detectan espacios en blanco (no estandarizados), seran eliminados
        val correoFil2 = correoFil1.replace("\\p{Zs}+".toRegex(), "")
        when{
            // Si el correo esta vacio
            TextUtils.isEmpty(correoFil2) -> avisoForPass("Error: Favor de introducir un correo")
            // Si la validacion del correo no coincide con la evaluacion de Patterns.EMAIL_ADDRESS
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoFil2).matches() -> avisoForPass("Error: Favor de introducir un correo valido")
            else -> return true
        }
        return false
    }
    private fun validarSelPreg(lista: Spinner): Boolean{
        if(lista.selectedItemPosition == 0){
            avisoForPass("Error: Favor de seleccionar una pregunta")
            return false
        }
        return true
    }
    private fun validarResp(respuesta: Editable): Boolean{
        if(TextUtils.isEmpty(respuesta)){
            avisoForPass("Error: Favor de introducir una respuesta para su pregunta")
            return false
        }
        return true
    }

    private fun buscarUsBD(){
        // Cuando se solicite el registro independientemente del metodo, primero se validara el user y luego se buscara en la BD
        valiUser = validarUsuario(txtUserRec.text!!)
        // Preparando variables para la obtencion de valores de los campos
        val usuario = txtUserRec.text!!.toString().trim()
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
                    if(busUser){
                        validaciones()
                    }else{
                        // Mostrar mensaje de error y limpiar los campos
                        avisoForPass("Error: El username que desea recuperar no existe, favor de ingresar otro")
                        txtUserRec.text!!.clear()
                        txtEmaRec.text!!.clear()
                        spPregsRec.setSelection(0)
                        txtRespRec.text!!.clear()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ResetPassActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            avisoForPass("El nombre de usuario no pudo ser validado correctamente, favor de revisarlo")
        }
    }

    private fun validaciones(){
        // Validacion individual de los campos
        valiCorr = validarCorreo(txtEmaRec.text!!)
        valiPreg = validarSelPreg(spPregsRec)
        valiResp = validarResp(txtRespRec.text!!)
        // En esta comprobacion tambien se deberia incluir la del username, pero este fue comprobado de manera previa a la llamada de la funcion
        if(valiCorr && valiPreg && valiResp) {
            // Una vez validados, se procede a comprobar la informacion ingresada
            comproInfo()
        }else if(!valiCorr){
            avisoForPass("Error: El correo no cumplio con los parametros de validacion establecidos")
        }else if(!valiPreg){
            avisoForPass("Error: Favor de seleccionar una pregunta")
        }else{
            avisoForPass("Error: La respuesta de seguridad no cumplio con los parametros de validacion establecidos")
        }
    }

    private fun comproInfo(){
        // Preparando variables para la comparacion de valores con lo almacenado en firebase
        val usuario = txtUserRec.text!!.toString().trim()
        val correo = txtEmaRec.text!!.toString().trim()
        val pregunta = "pregunta${spPregsRec.selectedItemPosition}"
        val respuesta = txtRespRec.text!!.toString()
        var comproUser = false

        // Buscando al usuario en la BD
        ref = database.getReference("Usuarios")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(objUser in dataSnapshot.children) {
                    val userBus = objUser.key.toString()
                    val emaBus = objUser.child("accesos").child("correo").value
                    val gooBus = objUser.child("accesos").child("google").value
                    val pregBus = objUser.child("pregunta_Seg").value
                    val respBus = objUser.child("resp_Seguri").value
                    if (userBus == usuario && (emaBus == correo || gooBus == correo) && pregBus == pregunta && respBus == respuesta){
                        comproUser = true
                        break
                    }
                }
                if(comproUser){
                    recuPass(txtEmaRec.text!!.toString().trim())
                }else{
                    // Mostrar mensaje de error y limpiar los campos
                    avisoForPass("Error: La informacion ingresada no coincide con nuestros registros, favor de corroborarla")
                    txtUserRec.text!!.clear()
                    txtEmaRec.text!!.clear()
                    spPregsRec.setSelection(0)
                    txtRespRec.text!!.clear()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResetPassActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun recuPass(correo: String){
        // Solicitar a firebase que mande el correo de recuperacion y establecer el idioma de envio
        auth.setLanguageCode("es_419")
        auth.sendPasswordResetEmail(correo).addOnCompleteListener {
            // Si se cumpli el envio del correo satisfactoriamente, se procede al envio hacia la main activity despues de 2 segundos y medio
            avisoForPass("Enviando correo de recuperacion..., favor de revisar su correo")
            Timer().schedule(2500){
                lifecycleScope.launch(Dispatchers.Main) {
                    // Cuando ya se envio el correo de recuperacion, se le enviara a la pantalla de inicio
                    val intentMain = Intent(this@ResetPassActivity, MainActivity::class.java)
                    startActivity(intentMain)
                }
            }
        }
        .addOnFailureListener {
            // Si no se pudo mandar el correo se procedera a mostrar el error y borrar los campos en cuestion
            avisoForPass("Error: No se pudo enviar el correo de recuperacion")
            txtUserRec.text!!.clear()
            txtEmaRec.text!!.clear()
            spPregsRec.setSelection(0)
            txtRespRec.text!!.clear()
        }
    }
}