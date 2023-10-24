package com.ardusec.ardu_security.admin

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class ManageAlarmActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var txtValAlaActu: EditText
    private lateinit var btnCreAlarma: Button
    private lateinit var btnChgAlarma: Button
    private lateinit var mateChgAla: MaterialCardView
    private lateinit var spAlarmas: AppCompatSpinner
    private lateinit var btnConfChgAlarma: Button
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Conteo de alarmas
    private var cantAlarmas: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_alarm)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))

        //Obteniendo los valores de activity
        if(intent.extras == null) {
            Toast.makeText(this@ManageAlarmActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("usuario").toString()
            sistema = bundle.getString("sistema").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la ventana
        title = "Alarma Administrador"
        // Relacionando los elementos con su objeto de la interfaz
        txtValAlaActu = findViewById(R.id.txtAlaID)
        btnCreAlarma = findViewById(R.id.btnAlaCrea)
        btnChgAlarma = findViewById(R.id.btnAlaChg)
        mateChgAla = findViewById(R.id.mateCardAlaCrea)
        spAlarmas = findViewById(R.id.spAlarmas)
        btnConfChgAlarma = findViewById(R.id.btnAlaConfChg)
        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Obteniendo el valor de la alarma y las alarmas disponibles
        getAlarma()
        rellSpinAlas()
    }

    private fun getAlarma(){
        lifecycleScope.launch(Dispatchers.IO){
            val getAla = async {
                ref = database.getReference("Alarmas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objAla in dataSnapshot.children){
                            if(objAla.child("sistema_Rel").value.toString() == sistema){
                                txtValAlaActu.setText(objAla.key.toString())
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ManageAlarmActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            getAla.await()
        }
    }

    private fun avisoGesAla(mensaje: String){
        val aviso = AlertDialog.Builder(this)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }
    private fun retorno(){
        return this.onBackPressedDispatcher.onBackPressed()
    }

    private fun rellSpinAlas(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellAlas = async {
                val lstAlas = resources.getStringArray(R.array.lstAlarmas)
                val arrAlas = ArrayList<String>()
                arrAlas.addAll(lstAlas)
                ref = database.getReference("Alarmas")
                ref.get().addOnSuccessListener{ taskGet ->
                    for (objAla in taskGet.children){
                        if(objAla.child("sistema_Rel").value == ""){
                            arrAlas.add(objAla.key.toString())
                        }
                    }
                    val adapAlas = ArrayAdapter(this@ManageAlarmActivity, android.R.layout.simple_spinner_item, arrAlas)
                    adapAlas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spAlarmas.adapter = adapAlas
                }
                    .addOnFailureListener {
                        avisoGesAla("Error: Datos parcialmente obtenidos")
                    }
            }
            rellAlas.await()
        }
    }

    private fun addListeners(){
        btnCreAlarma.setOnClickListener {
            crearAlarma()
        }
        btnChgAlarma.setOnClickListener {
            mateChgAla.isGone = false
        }
        btnConfChgAlarma.setOnClickListener {
            actuAlarma(txtValAlaActu.text.toString(), spAlarmas.selectedItem.toString())
        }
    }

    private fun getCantAla(){
        lifecycleScope.launch(Dispatchers.IO){
            val getCountAla = async {
                ref = database.getReference("Alarmas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        cantAlarmas = dataSnapshot.childrenCount
                    }
                    override fun onCancelled(error: DatabaseError) {
                        avisoGesAla("Error: Datos parcialmente obtenidos")
                    }
                })
            }
            getCountAla.await()
        }
    }

    private fun crearAlarma(){
        // Dataclass de alarmas
        data class Alarma(val condicion: Boolean, val estado: Boolean, val sistema_Rel: String)

        // Obtener conteo
        getCantAla()
        // Crear el objeto de la alarma
        val nAlarma = Alarma(condicion = false, estado = false, sistema_Rel = sistema)
        // Creacion de la alarma en la entidad correspondiente
        lifecycleScope.launch(Dispatchers.IO){
            val setNAla = async {
                ref = database.getReference("Alarmas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objAla in dataSnapshot.children){
                            if(objAla.key.toString() == txtValAlaActu.text.toString()){
                                objAla.ref.child("sistema_Rel").setValue(" ")
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        avisoGesAla("Error: Datos parcialmente obtenidos")
                    }
                })

                val nKeyAla = "alarma${cantAlarmas + 1}"
                ref.child(nKeyAla).setValue(nAlarma).addOnSuccessListener {
                    actuAlarma(txtValAlaActu.text.toString(), nKeyAla)
                    Toast.makeText(this@ManageAlarmActivity, "Su alarma fue creada y actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                    Timer().schedule(1500){
                        lifecycleScope.launch(Dispatchers.Main){
                            retorno()
                            finish()
                        }
                    }
                }
            }
            setNAla.await()
        }
    }

    private fun actuAlarma(alaKeyActu: String, alaKeyNew: String){
        lifecycleScope.launch(Dispatchers.IO){
            val setNSisAla = async {
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objSis in dataSnapshot.children){
                            if(objSis.key.toString() == sistema){
                                objSis.ref.child("alarma").setValue(alaKeyNew)
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        avisoGesAla("Error: Datos parcialmente obtenidos")
                    }
                })
            }
            setNSisAla.await()

            val setNAlaSis = async {
                ref = database.getReference("Alarmas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objAla in dataSnapshot.children){
                            if(objAla.key.toString() == alaKeyActu){
                                objAla.ref.child("sistema_Rel").setValue(sistema).addOnSuccessListener {
                                    Toast.makeText(this@ManageAlarmActivity, "Su alarma fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        avisoGesAla("Error: Datos parcialmente obtenidos")
                    }
                })
            }
            setNAlaSis.await()
        }
    }
}