package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
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
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.teal_700)))
        // Arranque de la app
        setUp()
    }

    private fun setUp(){
        // Mensaje de bienvenida a la App
        Timer().schedule(1000){
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()
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
                val intentLogin = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intentLogin)
            }
        }
    }
}