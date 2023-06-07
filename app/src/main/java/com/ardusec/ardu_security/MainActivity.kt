package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Arranque de la app
        setUp()
        /* Agregando elementos a la BD
        addValores()*/
    }

    private fun setUp(){
        // Mensaje de bienvenida a la App
        Timer().schedule(1000){
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()
            }
        }

        // Si el usuario salio de la app pero no finalizo su sesion, sera enviado directamente a su dashboard
        val user = FirebaseAuth.getInstance().currentUser
        if(user!=null){
            val intentDash = Intent(this, DashboardActivity::class.java)
            startActivity(intentDash)
        }else{
            // En caso de que no haya una sesion iniciada se hara un retardo de 2 segundos y se enviara al login
            Timer().schedule(2000) {
                val intentLogin = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intentLogin)
            }
        }
    }

    /*private fun addValores(){
        // Agregar estaciones
        data class Estacion(val id_Estacion: String, val nombre: String, val sistema_Nom: String)
        val estaRef = Firebase.database.getReference("Estacion")
        val addEstaDB = estaRef.push()
        val nEsta = Estacion(id_Estacion=addEstaDB.key.toString(),nombre="EstGen5",sistema_Nom="SistemaGen1")
        addEstaDB.setValue(nEsta)
        // Agregar camaras
        data class Camara(val id_Camara: String, val nom_Cam: String, val ip_Transmi: String, val estacion_Nom: String)
        val camRef = Firebase.database.getReference("Camara")
        var host = 68
        for (conta in 2..5){
            val addCamDB = camRef.push()
            val nCam = Camara(id_Camara=addCamDB.key.toString(),nom_Cam="CamEsta$conta",ip_Transmi="192.168.1.$host",estacion_Nom="EstGen$conta")
            addCamDB.setValue(nCam)
            host ++
        }
        // Agregar sensores
        data class Sensor(val id_Sensor: String, val nom_Sen: String, val tipo: String, val estacion_Nom: String)
        val senRef = Firebase.database.getReference("Sensor")
        val tipo2 = "Humo/Gas"
        for (conta in 2..5){
            val addSenDB = senRef.push()
            val nSen = if(conta % 2 == 0){
                Sensor(id_Sensor=addSenDB.key.toString(),nom_Sen="SenGen$conta",tipo=tipo2,estacion_Nom="EstGen$conta")
            }else{
                Sensor(id_Sensor=addSenDB.key.toString(),nom_Sen="SenGen$conta",tipo="Magnetico",estacion_Nom="EstGen$conta")
            }
            addSenDB.setValue(nSen)
        }
    }*/
}