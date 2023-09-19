package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {
    private lateinit var swAlarma: Switch
    private lateinit var rbCondAlaActi: RadioButton
    private lateinit var rbCondAlaOper: RadioButton
    private lateinit var rbCondAlaInac: RadioButton
    private lateinit var rbPrendeAla: RadioButton
    private lateinit var rbApagaAla: RadioButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

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
        swAlarma = findViewById(R.id.swAlarma)
        rbCondAlaActi = findViewById(R.id.rbAlaActi)
        rbCondAlaOper = findViewById(R.id.rbAlaOper)
        rbCondAlaInac = findViewById(R.id.rbAlaInac)
        rbPrendeAla = findViewById(R.id.rbEncenderAlarma)
        rbApagaAla = findViewById(R.id.rbApagarAlarma)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Estableciendo el estado del switch acorde a la info de firebase
        setSwitch()
    }

    private fun setSwitch(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setEstAla = async {
                // Establecer el estado del switch acorde al valor de la entidad en firebase
                ref = database.getReference("Alarmas")
                ref.get().addOnSuccessListener { taskGet ->
                    for (objAla in taskGet.children) {
                        if(objAla.child("sistema_Rel").value.toString() == sistema) {
                            val condicion = objAla.child("condicion").value.toString()
                            val estado = objAla.child("estado").value.toString()

                            if(condicion == "true" && estado == "true"){
                                rbCondAlaActi.isChecked = true
                            }
                            if(condicion == "false" && estado == "true"){
                                rbCondAlaOper.isChecked = true
                            }
                            if(condicion == "false" && estado == "false"){
                                rbCondAlaInac.isChecked = true
                            }
                            break
                        }
                    }
                }
                    .addOnFailureListener {
                        Log.w(
                            "FirebaseError",
                            "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados"
                        )
                    }
            }
            setEstAla.await()
        }
    }

    private fun addListeners(){
        rbPrendeAla.setOnClickListener {
            if(rbPrendeAla.isChecked){
                // Establecer el estado de la alarma en firebase
                ref = database.getReference("Alarmas")
                ref.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for(objAla in dataSnapshot.children){
                            if(objAla.child("sistema_Rel").value.toString() == sistema){
                                ref.child(objAla.key.toString()).child("estado").setValue(true)
                                rbCondAlaOper.isChecked = true
                                /*val estado = objAla.child("estado").value.toString().toBoolean()
                                if(estado){
                                    rbCondAlaActi.isChecked = true
                                }else{
                                    rbCondAlaOper.isChecked = true
                                }*/
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
        }

        rbApagaAla.setOnClickListener {
            if(rbApagaAla.isChecked){
                // Establecer el estado de la alarma en firebase
                ref = database.getReference("Alarmas")
                ref.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for(objAla in dataSnapshot.children){
                            if(objAla.child("sistema_Rel").value.toString() == sistema){
                                ref.child(objAla.key.toString()).child("estado").setValue(false)
                                rbCondAlaInac.isChecked = true
                                break
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
        }

        /*lifecycleScope.launch(Dispatchers.IO){
            val changeAla = async {
                swAlarma.setOnClickListener {
                    if(swAlarma.isChecked) {
                        swAlarma.text = resources.getString(R.string.alEstaAct)
                        // Establecer el estado de la alarma en firebase
                        ref = database.getReference("Alarmas")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for(objAla in dataSnapshot.children){
                                    if(objAla.child("sistema_Rel").value.toString() == sistema){
                                        ref.child(objAla.key.toString()).child("estado").setValue(true)
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                        // Establecer el estado del switch acorde al valor de la entidad en firebase
                        ref = database.getReference("Alarmas")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for(objAla in dataSnapshot.children){
                                    if(objAla.child("sistema_Rel").value.toString() == sistema){
                                        val estado = objAla.child("estado").value.toString().toBoolean()
                                        if(estado){
                                            rbCondAlaActi.isChecked = true
                                        }else{
                                            rbCondAlaOper.isChecked = true
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }else{
                        rbCondAlaInac.isChecked = true
                        swAlarma.text = resources.getString(R.string.alEstaDesa)
                        // Establecer el estado de la alarma en firebase
                        ref = database.getReference("Alarmas")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                for(objAla in dataSnapshot.children){
                                    if(objAla.child("sistema_Rel").value.toString() == sistema){
                                        ref.child(objAla.key.toString()).child("estado").setValue(false)
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
            changeAla.await()
        }*/
    }
}