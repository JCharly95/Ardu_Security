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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class LoginActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var chbVerContra: CheckBox
    private lateinit var btnLostPass: Button
    private lateinit var btnAcc: Button
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Relacionando los elementos con su objeto de la interfaz
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtPass)
        chbVerContra = findViewById(R.id.chbPass)
        btnLostPass = findViewById(R.id.btnLstContra)
        btnAcc = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegView)
        // Inicializando la autenticacion
        auth = Firebase.auth

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
        btnRegister.setOnClickListener{
            val intentRegister = Intent(applicationContext,RegisterActivity::class.java)
            startActivity(intentRegister)
        }
        btnAcc.setOnClickListener{
            val correo = txtEmail.text.toString()
            val contra = txtContra.text.toString()

            when{
                correo.isEmpty() -> Toast.makeText(this, "Favor de ingresar su correo", Toast.LENGTH_SHORT).show()
                contra.isEmpty() -> Toast.makeText(this, "Favor de ingresar su contraseña", Toast.LENGTH_SHORT).show()
                correo.isEmpty() && contra.isEmpty()-> Toast.makeText(this, "Favor de ingresar sus datos", Toast.LENGTH_SHORT).show()
                else -> acceder(correo, contra)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Comprobar si el usuario ha iniciado sesión (no es nulo) cuando se inicia la Activity
        val userActual = auth.currentUser
        if(userActual != null){
            // Si el usuario ya ha iniciado sesion pero no la cerro, se ira directamente al dashboard
            val intentoDash = Intent(this, DashboardActivity::class.java)
            this.startActivity(intentoDash)
        }
    }

    private fun acceder(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si el usuario accedio satisfactoriamente, se le envia hacia el dashboard
                    val user = Firebase.auth.currentUser
                    user?.let {
                        val nombre = it.displayName
                        Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                        val intentoDash = Intent(this, DashboardActivity::class.java)
                        this.startActivity(intentoDash)
                    }
                } else {
                    // Si el usuario no accedio satisfactoriamente, se limpiaran los campos y se mostrara un error
                    Toast.makeText(this, "No se pudo acceder con la informacion ingresada", Toast.LENGTH_SHORT).show()
                    txtEmail.text.clear()
                    txtContra.text.clear()
                }
            }
    }
}