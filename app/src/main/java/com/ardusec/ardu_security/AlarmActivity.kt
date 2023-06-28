package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
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
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

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
        // Inicializando instancia hacia el nodo raiz de la BD
        database = Firebase.database

        // Estableciendo el estado del switch acorde a la info de firebase
        setSwitch()
    }

    private fun setSwitch(){
        lifecycleScope.launch(Dispatchers.IO) {
            val setEstAla = async {
                // Establecer el estado del switch acorde al valor de la entidad en firebase
                ref = database.getReference("Alarma").child("estado")
                ref.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        val estado = dataSnapshot.value
                        if(estado as Boolean){
                            swAlarma.text = resources.getString(R.string.alEstaAct)
                            swAlarma.isChecked = true
                        }else{
                            swAlarma.text = resources.getString(R.string.alEstaDesa)
                            swAlarma.isChecked = false
                            rbCondAlaInac.isChecked = true
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            setEstAla.await()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val setcondAla = async {
                // Establecer el estado del switch acorde al valor de la entidad en firebase
                ref = database.getReference("Alarma").child("condicion")
                ref.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        val estado = dataSnapshot.value
                        if(estado as Boolean){
                            rbCondAlaActi.isChecked = true
                        }else{
                            rbCondAlaOper.isChecked = true
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                    }
                })
            }
            setcondAla.await()
        }
    }

    private fun addListeners(){
        swAlarma.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                swAlarma.isChecked = true
                database.getReference("Alarma").child("estado").setValue(true)
                swAlarma.text = resources.getString(R.string.alEstaAct)
                lifecycleScope.launch(Dispatchers.IO) {
                    val setcondAla = async {
                        // Establecer el estado del switch acorde al valor de la entidad en firebase
                        ref = database.getReference("Alarma").child("condicion")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                val estado = dataSnapshot.value
                                if(estado as Boolean){
                                    rbCondAlaActi.isChecked = true
                                }else{
                                    rbCondAlaOper.isChecked = true
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("FirebaseError", "Error: No se pudieron obtener o no se pudieron actualizar los valores solicitados", databaseError.toException())
                            }
                        })
                    }
                    setcondAla.await()
                }
            }else{
                swAlarma.isChecked = false
                rbCondAlaInac.isChecked = true
                database.getReference("Alarma").child("estado").setValue(false)
                swAlarma.text = resources.getString(R.string.alEstaDesa)
            }
        }
    }
}