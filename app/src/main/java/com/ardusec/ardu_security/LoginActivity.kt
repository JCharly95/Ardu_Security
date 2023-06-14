package com.ardusec.ardu_security

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var chbVerContra: CheckBox
    private lateinit var btnLostPass: Button
    private lateinit var btnAcc: Button
    private lateinit var btnRegister: Button
    private lateinit var btnAyuda: Button
    private var valCorr: Boolean = false
    private var valContra: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Iniciar Sesion"
        // Relacionando los elementos con su objeto de la interfaz
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtPass)
        chbVerContra = findViewById(R.id.chbPass)
        btnLostPass = findViewById(R.id.btnLstContra)
        btnAcc = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegView)
        btnAyuda = findViewById(R.id.btnInfo)
    }

    private fun aviso(){
        val mensaje = "Consideraciones de campos: \n\n" +
                "Correo; Formato Aceptado:\n" +
                "* usuario@dominio.com(.mx)\n\n" +
                "Contraseña:\n" +
                "* Extension minima de 8 caracteres\n" +
                "* Por lo menos una mayuscula\n" +
                "* Por lo menos un numero\n" +
                "* Por lo menos  un caracter especial"
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun addListeners(){
        // Agregar los listener de los botones
        btnLostPass.setOnClickListener{
            val intentLost = Intent(this,ResetPassActivity::class.java)
            startActivity(intentLost)
        }
        chbVerContra.setOnClickListener{
            if(!chbVerContra.isChecked){
                txtContra.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                txtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }
        btnRegister.setOnClickListener{
            val intentRegister = Intent(this,RegisterActivity::class.java)
            startActivity(intentRegister)
        }
        btnAyuda.setOnClickListener {
            aviso()
        }
        btnAcc.setOnClickListener{
            val correo = txtEmail.text.toString()
            val contra = txtContra.text.toString()

            if(correo.isNotEmpty() && contra.isNotEmpty()){
                // Validacion de campos
                if(validarCorreo(correo, this)){
                    valCorr = true
                }
                if(validarContra(contra, this)){
                    valContra = true
                }
                // Si las validaciones de campos fueron correctas, se logueara en firebase
                if(valCorr && valContra){
                    acceder(correo, contra)
                }else{
                    Toast.makeText(this, "Error: Favor de revisar sus datos", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Error: Favor de ingresar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCorreo(correo: String, contexto: Context): Boolean{
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

    private fun validarContra(contra: String, contexto: Context): Boolean{
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

    private fun acceder(email: String, password: String) {
        // Se crea una instancia de FirebaseAuth (Autenticacion y se inicia sesion/loguea)
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        // Si el usuario accedio satisfactoriamente, se le envia hacia el dashboard y se accede a firebase para mostrar su nombre
                        val refDB = Firebase.database.getReference("Usuarios")
                        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
                        refDB.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for (objUs in dataSnapshot.children){
                                    val gson = Gson()
                                    val userJSON = gson.toJson(objUs.value)
                                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                                    val nombre = resUser.nombre
                                    Toast.makeText(this@LoginActivity, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                                    val intentoDash = Intent(this@LoginActivity, DashboardActivity::class.java)
                                    startActivity(intentoDash)
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }else{
                        // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                        Toast.makeText(this, "Error: No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                        txtEmail.text.clear()
                        txtContra.text.clear()
                    }
                }
    }
}