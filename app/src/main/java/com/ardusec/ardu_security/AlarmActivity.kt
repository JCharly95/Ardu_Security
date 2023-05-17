package com.ardusec.ardu_security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AlarmActivity : AppCompatActivity() {
    private lateinit var lblSta: TextView
    private lateinit var swAlarma: SwitchMaterial
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Gestionar Alarma"
        // Relacionando los elementos con su objeto de la interfaz
        lblSta = findViewById(R.id.lblStaVal)
        swAlarma = findViewById(R.id.swAlarma)
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Estableciendo el estado del switch acorde a la info de firebase
        setSwitch()
    }

    private fun setSwitch(){
        // Establecer el estado del switch acorde al valor de la entidad en firebase
        ref = database.getReference("Alarma").child("estado")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                val estado = dataSnapshot.value
                if(estado as Boolean){
                    lblSta.text = resources.getString(R.string.alEstaAct)
                    swAlarma.isChecked = true
                }else{
                    lblSta.text = resources.getString(R.string.alEstaDesa)
                    swAlarma.isChecked = false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun addListeners(){
        swAlarma.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                swAlarma.isChecked = true
                database.getReference("Alarma").child("estado").setValue(true)
            }else{
                swAlarma.isChecked = false
                database.getReference("Alarma").child("estado").setValue(false)
            }
        }
    }
}