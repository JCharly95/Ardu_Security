package com.ardusec.ardu_security.admin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.ardusec.ardu_security.EditDataSpActivity
import com.ardusec.ardu_security.EditDataTxtActivity
import com.ardusec.ardu_security.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ManageSisActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnSisChgNam: ImageButton
    private lateinit var btnSisChgAla: ImageButton
    private lateinit var btnSisChgSta: ImageButton
    private lateinit var btnAdminUs: ImageButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_sis)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))

        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            Toast.makeText(this@ManageSisActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            sistema = bundle.getString("sistema").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Administrador Sistema"
        // Relacionando los elementos con su objeto de la interfaz
        btnSisChgNam = findViewById(R.id.btnSisNam)
        btnSisChgAla = findViewById(R.id.btnSisAla)
        btnSisChgSta = findViewById(R.id.btnSisSta)
        btnAdminUs = findViewById(R.id.btnSisGesUs)
    }

    private fun addListeners(){
        btnSisChgNam.setOnClickListener {
            val chgNamSisActi = Intent(this@ManageSisActivity, EditDataTxtActivity::class.java).apply {
                putExtra("sistema", sistema)
                putExtra("usuario", user)
                putExtra("campo", "Nombre Sistema")
            }
            startActivity(chgNamSisActi)
        }
        btnSisChgAla.setOnClickListener {
            val chgAlaSisActi = Intent(this@ManageSisActivity, EditDataSpActivity::class.java).apply {
                putExtra("sistema", sistema)
                putExtra("usuario", user)
                putExtra("campo", "AlaSis")
            }
            startActivity(chgAlaSisActi)
        }
        btnSisChgSta.setOnClickListener {

        }
        btnAdminUs.setOnClickListener {

        }
    }
}