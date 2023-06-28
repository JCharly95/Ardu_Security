package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnEstas: Button
    private lateinit var btnGenRep: Button
    private lateinit var btnMenAj: Button
    private lateinit var btnManual: Button
    private lateinit var btnMenSis: Button
    private lateinit var btnCerSes: Button
    // Creando el objeto GSON
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Configurando el backPressed
        onBackPressedDispatcher.addCallback(this, presAtrasCallback)
    }

    private val presAtrasCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            avisoDash()
        }
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Dashboard"
        // Relacionando los elementos con su objeto de la interfaz
        btnEstas = findViewById(R.id.btnStats)
        btnGenRep = findViewById(R.id.btnGenRep)
        btnMenAj = findViewById(R.id.btnAjuste)
        btnManual = findViewById(R.id.btnManUs)
        btnMenSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)

        // Obtener el correo del usuario desde Firebase auth y enviarlo a la funcion de la vista del boton
        val corrAcc = getEmail()
        btnGestSis(corrAcc)
    }

    private fun addListeners(){
        // Agregar los listener
        btnEstas.setOnClickListener {
            val statsActi = Intent(this, MenuStationsActivity::class.java)
            startActivity(statsActi)
        }
        btnGenRep.setOnClickListener {

        }
        btnMenAj.setOnClickListener {
            val settingActi = Intent(this, SettingsActivity::class.java)
            startActivity(settingActi)
        }
        btnManual.setOnClickListener {

        }
        btnMenSis.setOnClickListener {

        }
        btnCerSes.setOnClickListener {
            // Cerrar Sesion en Firebase
            FirebaseAuth.getInstance().signOut()
            // Lanzar la app hacia la primera ventana
            val endActi = Intent(this, MainActivity::class.java)
            startActivity(endActi)
            finish()
        }
    }

    private fun avisoDash(){
        val mensaje = "Boton deshabilitado.\n" +
                "Si desea regresar al inicio o iniciar sesion, favor de cerrar su sesión antes."
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun getEmail(): String {
        val user = Firebase.auth.currentUser
        var email = ""
        user?.let {task ->
            email = task.email.toString()
        }
        return email
    }

    private fun btnGestSis(correo: String){
        // Creando la referencia de la coleccion de preguntas en la BD
        val refDB = Firebase.database.getReference("Usuarios")
        data class Usuario(val id_Usuario: String, val nombre: String, val correo: String, val tipo_Usuario: String, val num_Tel: Long, val preg_Seguri: String, val resp_Seguri: String, val pin_Pass: Int)
        refDB.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (objUs in dataSnapshot.children){
                    val userJSON = gson.toJson(objUs.value)
                    val resUser = gson.fromJson(userJSON, Usuario::class.java)
                    if(resUser.correo == correo){
                        if(resUser.tipo_Usuario == "Administrador"){
                            btnMenSis.isGone = false
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }
}