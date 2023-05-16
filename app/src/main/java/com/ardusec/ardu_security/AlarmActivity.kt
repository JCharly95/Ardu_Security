package com.ardusec.ardu_security

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.view.isGone
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

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

        // Establecer el estado del switch acorde al valor de la entidad en firebase
        ref = database.getReference("Alarma")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                val estado = dataSnapshot.children
                Log.w("FirebaseResult", "Estado de la alarma en firebase: $estado")
                /*if(estado){
                    lblSta.text = resources.getString(R.string.alEstaAct)
                    swAlarma.isChecked = true
                }else{
                    lblSta.text = resources.getString(R.string.alEstaDesa)
                    swAlarma.isChecked = false
                }*/
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
            }
        })
    }

    private fun addListeners(){
        swAlarma.setOnClickListener {
            swAlarma.isChecked = !swAlarma.isChecked
        }
    }
}