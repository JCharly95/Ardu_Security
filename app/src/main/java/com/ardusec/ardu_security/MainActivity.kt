package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.user.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        // Arranque de la app
        setUp()
    }

    private fun setUp(){
        // Inicializando instancia hacia el nodo raiz de la BD, la de la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()
        //insertValues()

        // Si el usuario salio de la app pero no finalizo su sesion, sera enviado directamente a su dashboard
        val user = auth.currentUser
        if(user != null){
            user.let { usuario ->
                val correo = usuario.email
                val nombre = usuario.displayName
                // Buscando al usuario en la BD
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            val dirCor = objUser.child("accesos").child("correo").value.toString()
                            val dirGoo = objUser.child("accesos").child("google").value.toString()

                            if(dirCor == correo || dirGoo == correo) {
                                // Nueva adicion, se agrego una nueva funcion de pantallas de carga y para eso se deja la pantalla por un segundo y luego se llama a la ventana de carga
                                Timer().schedule(1000) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        chgPanta()
                                    }
                                }
                                Timer().schedule(3000) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        Toast.makeText(this@MainActivity, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                                        val intentDash = Intent(this@MainActivity, DashboardActivity::class.java).apply {
                                            putExtra("username", objUser.key.toString())
                                        }
                                        startActivity(intentDash)
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    }
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Busqueda sin exito", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }else{
            // En caso de que no haya una sesion iniciada se hara un retardo de 2 segundos y se enviara al login
            // Nueva adicion, se agrego una nueva funcion de pantallas de carga y para eso se deja la pantalla por un segundo y luego se llama a la ventana de carga
            Timer().schedule(1000) {
                lifecycleScope.launch(Dispatchers.Main) {
                    chgPanta()
                }
            }
            Timer().schedule(3000) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()
                    val intentLogin = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intentLogin)
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
            }
        }
    }

    private fun chgPanta(){
        val builder = AlertDialog.Builder(this@MainActivity).create()
        val view = layoutInflater.inflate(R.layout.charge_transition,null)
        builder.setView(view)
        builder.show()
        Timer().schedule(2000){
            builder.dismiss()
        }
    }
}