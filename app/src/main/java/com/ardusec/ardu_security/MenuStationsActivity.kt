package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isGone
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class MenuStationsActivity : AppCompatActivity() {
    private lateinit var btnSta1: Button
    private lateinit var btnSta2: Button
    private lateinit var btnSta3: Button
    private lateinit var btnSta4: Button
    private lateinit var btnSta5: Button
    private lateinit var btnAlarma: Button
    // Objeto gson
    var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_stations)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Menu Estaciones"
        // Relacionando los elementos con su objeto de la interfaz
        btnSta1 = findViewById(R.id.btnStat1)
        btnSta2 = findViewById(R.id.btnStat2)
        btnSta3 = findViewById(R.id.btnStat3)
        btnSta4 = findViewById(R.id.btnStat4)
        btnSta5 = findViewById(R.id.btnStat5)
        btnAlarma = findViewById(R.id.btnAlarma)
        // Habilitando solo los botones adecuados de acuerdo al plan del sistema
        setupBtn()
    }

    private fun addListeners(){
        // Agregando los listener
        btnSta1.setOnClickListener {
            val intActEst1 = Intent(this,StationActivity::class.java)
            intActEst1.putExtra("name_station", "estacion 1")
            startActivity(intActEst1)
        }
        btnSta2.setOnClickListener {
            val intActEst2 = Intent(this,StationActivity::class.java)
            intActEst2.putExtra("name_station", "estacion 2")
            startActivity(intActEst2)
        }
        btnSta3.setOnClickListener {
            val intActEst3 = Intent(this,StationActivity::class.java)
            intActEst3.putExtra("name_station", "estacion 3")
            startActivity(intActEst3)
        }
        btnSta4.setOnClickListener {
            val intActEst4 = Intent(this,StationActivity::class.java)
            intActEst4.putExtra("name_station", "estacion 4")
            startActivity(intActEst4)
        }
        btnSta5.setOnClickListener {
            val intActEst5 = Intent(this,StationActivity::class.java)
            intActEst5.putExtra("name_station", "estacion 5")
            startActivity(intActEst5)
        }
        btnAlarma.setOnClickListener {
            val intentAlarma = Intent(this, AlarmActivity::class.java)
            startActivity(intentAlarma)
        }
    }

    private fun setupBtn(){
        data class Sistema(val id_Sistema: String, val nombre_Sis: String, val tipo: String, val ulti_Cam_Nom: String)
        data class UserSistem(val id_User_Sis: String, val sistema_Nom: String, val user_Email: String)

        // Obteniendo el correo del usuario
        val user = Firebase.auth.currentUser
        user?.let {task ->
            val correo = task.email.toString()
            // Obtener la referencia de User_Sistem
            val refDB = Firebase.database.getReference("User_Sistems")
            refDB.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot){
                    for (objUsSis in dataSnapshot.children){
                        val userSisJSON = gson.toJson(objUsSis.value)
                        val resUserSis = gson.fromJson(userSisJSON, UserSistem::class.java)
                        if(resUserSis.user_Email == correo){
                            val sistema = resUserSis.sistema_Nom
                            // Obtener la referencia del sistema para saber que botones mostrar
                            val sisRef = Firebase.database.getReference("Sistema")
                            sisRef.addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(dataSnapshot: DataSnapshot){
                                    for (objSis in dataSnapshot.children){
                                        val sisJSON = gson.toJson(objSis.value)
                                        val resSis = gson.fromJson(sisJSON, Sistema::class.java)
                                        if(resSis.nombre_Sis == sistema){
                                            if(resSis.tipo == "Standard"){
                                                btnSta1.isGone = false
                                                btnSta2.isGone = false
                                                btnSta3.isGone = false
                                                btnSta4.isGone = true
                                                btnSta5.isGone = true
                                            }else{
                                                btnSta1.isGone = false
                                                btnSta2.isGone = false
                                                btnSta3.isGone = false
                                                btnSta4.isGone = false
                                                btnSta5.isGone = false
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
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                }
            })
        }
    }
}