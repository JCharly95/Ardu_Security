package com.ardusec.ardu_security

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.view.isGone
import org.json.JSONArray
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Obtener los botones de la pantalla
        val btnEstas: Button = findViewById(R.id.btnStats)
        val btnGenRep: Button = findViewById(R.id.btnGenRep)
        val btnEdiPerf: Button = findViewById(R.id.btnPerfUs)
        val btnMenAj: Button = findViewById(R.id.btnAjuste)
        val btnManual: Button = findViewById(R.id.btnManUs)
        val btnManSis: Button = findViewById(R.id.btnGesSis)
        val btnCerSes: Button = findViewById(R.id.btnCerrSes)

        // Establecer o crear la informacion en la base de datos SQLite
        //bdSQLite(applicationContext)

        /*  Obtencion de informacion del usuario al entrar a la actividad
        Saber si se viene de login o del registro; UserData para el login y UserDataReg para el registro
        Primero se evalua si el intent procedente del login no esta vacio, se toma como bueno, en caso
        contrario se entiende que viene del registro. en ambos casos se crea un JSONObject  */
        val usDatObj: JSONObject? = if(intent.extras?.getString("UserData")?.isEmpty() == false){
            intent.extras?.getString("UserData")?.let { JSONObject(it) }
        }else{
            intent.extras?.getString("UserDataReg")?.let { JSONObject(it) }
        }

        // Si el usuario logueado es admin hacer visible el boton de gestion del sistema
        if(usDatObj?.get("tipoUs") == "1"){
            btnManSis.isGone = false
        }

        // Agregar los listener
        btnEstas.setOnClickListener {
            val statsActi = Intent(applicationContext,StationsActivity::class.java)
            startActivity(statsActi)
        }

        btnCerSes.setOnClickListener {
            val endActi = Intent(applicationContext,MainActivity::class.java)
            startActivity(endActi)
            finish()
        }

        /*
        android:id="@+id/btnAjuste"
        android:id="@+id/btnManUs"
        android:id="@+id/btnGesSis"
        * */
    }

    fun bdSQLite(contexto: Context){
        val admin = ArduSecurityLite(this,"ardu_security", null, 1)
        val bd = admin.writableDatabase
        val registro = ContentValues()
        //registro.put("codigo", et1.getText().toString())
        //registro.put("descripcion", et2.getText().toString())
        //registro.put("precio", et3.getText().toString())
        //bd.insert("Usuarios", null, registro)
        //bd.close()
    }
}