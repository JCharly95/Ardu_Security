package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {
    private lateinit var rbPrendeAla: RadioButton
    private lateinit var rbApagaAla: RadioButton
    private lateinit var btnSetEstado: Button
    private lateinit var adAlaActi: MaterialCardView
    private lateinit var adAlaOper: MaterialCardView
    private lateinit var adAlaInac: MaterialCardView
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_alarm)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))

        //Obteniendo los valores del usuario y estacion
        if(intent.extras != null){
            bundle = intent.extras!!
            sistema = bundle.getString("sistema").toString()
        }else{
            Toast.makeText(this@AlarmActivity, "Error: no se pudo obtener la informacion del sistema", Toast.LENGTH_SHORT).show()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Gestionar Alarma"
        // Relacionando los elementos con su objeto de la interfaz
        rbPrendeAla = findViewById(R.id.rbEncenderAlarma)
        rbApagaAla = findViewById(R.id.rbApagarAlarma)
        btnSetEstado = findViewById(R.id.btnSetEstaAl)
        adAlaActi = findViewById(R.id.alaActiAd)
        adAlaOper = findViewById(R.id.alaOperAd)
        adAlaInac = findViewById(R.id.alaInacAd)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Estableciendo el estado del switch acorde a la info de firebase
        setAlarma()
        // Agregar los listeners
        addListeners()
    }

    private fun avisoAl(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setAlarma(){
        lifecycleScope.launch(Dispatchers.IO){
            val setAla = async {
                // Establecer el estado del switch acorde al valor de la entidad en firebase
                ref = database.getReference("Alarmas")
                ref.get().addOnCompleteListener {taskGet ->
                    if(taskGet.isSuccessful){
                        for (objAla in taskGet.result.children) {
                            if(objAla.child("sistema_Rel").value.toString() == sistema) {
                                val condicion = objAla.child("condicion").value.toString()
                                val estado = objAla.child("estado").value.toString()

                                if(condicion == "true" && estado == "true"){
                                    adAlaActi.isGone = false
                                    adAlaOper.isGone = true
                                    adAlaInac.isGone = true
                                }
                                if(condicion == "false" && estado == "true"){
                                    adAlaActi.isGone = true
                                    adAlaOper.isGone = false
                                    adAlaInac.isGone = true
                                }
                                if(condicion == "false" && estado == "false"){
                                    adAlaActi.isGone = true
                                    adAlaOper.isGone = true
                                    adAlaInac.isGone = false
                                }
                            }
                        }
                    }else{
                        Log.w("FirebaseError","Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados")
                    }
                }
            }
            setAla.await()
        }
    }

    private fun addListeners(){
        btnSetEstado.setOnClickListener {
            if(rbPrendeAla.isChecked){
                lifecycleScope.launch(Dispatchers.IO){
                    val alaEstaPren = async {
                        // Establecer el estado de la alarma en firebase
                        ref = database.getReference("Alarmas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for(objAla in dataSnapshot.children){
                                    if(objAla.child("sistema_Rel").value.toString() == sistema){
                                        objAla.ref.child("estado").setValue(true)
                                        Toast.makeText(this@AlarmActivity, "Su alarma ha sido encendida", Toast.LENGTH_SHORT).show()
                                        this@AlarmActivity.finish()
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }
                    alaEstaPren.await()
                }
            }else if(rbApagaAla.isChecked){
                lifecycleScope.launch(Dispatchers.IO){
                    val alaEstaApa = async {
                        // Establecer el estado de la alarma en firebase
                        ref = database.getReference("Alarmas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for(objAla in dataSnapshot.children){
                                    if(objAla.child("sistema_Rel").value.toString() == sistema){
                                        objAla.ref.child("estado").setValue(false)
                                        Toast.makeText(this@AlarmActivity, "Su alarma ha sido apagada", Toast.LENGTH_SHORT).show()
                                        this@AlarmActivity.finish()
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }
                    alaEstaApa.await()
                }
            }
        }
    }
}