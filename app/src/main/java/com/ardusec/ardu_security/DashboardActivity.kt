package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnMenEsta: ImageButton
    private lateinit var btnGenRep: ImageButton
    private lateinit var btnMenAj: ImageButton
    private lateinit var btnManual: ImageButton
    private lateinit var btnMenSis: ImageButton
    private lateinit var btnCerSes: ImageButton
    private lateinit var linLayGesSis: LinearLayout
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var tipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo los valores de acceso/registro
        if(intent.extras == null){
            tipo = "Cliente"
            Toast.makeText(this@DashboardActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            tipo = bundle.getString("tipo").toString()
        }

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
        btnMenEsta = findViewById(R.id.btnStats)
        btnGenRep = findViewById(R.id.btnGenRep)
        btnMenAj = findViewById(R.id.btnAjuste)
        btnManual = findViewById(R.id.btnManUs)
        linLayGesSis = findViewById(R.id.LinBtnEsta5)
        btnMenSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)

        if(tipo == "Administrador"){
            linLayGesSis.isGone = false
        }
    }

    private fun addListeners(){
        // Agregar los listener
        btnMenEsta.setOnClickListener {
            val statsActi = Intent(this, MenuStationsActivity::class.java).apply {
                putExtra("username", user)
            }
            startActivity(statsActi)
        }
        btnGenRep.setOnClickListener {

        }
        btnMenAj.setOnClickListener {
            val settingActi = Intent(this, SettingsActivity::class.java).apply {
                putExtra("username", user)
            }
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
}