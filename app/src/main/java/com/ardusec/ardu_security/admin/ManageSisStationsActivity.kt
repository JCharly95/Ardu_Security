package com.ardusec.ardu_security.admin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.ardusec.ardu_security.user.DashboardActivity
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

class ManageSisStationsActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var spEstaciones: AppCompatSpinner
    private lateinit var txtNomEsta: EditText
    private lateinit var txtDirEsta: EditText
    private lateinit var btnHabiChgNom: ImageButton
    private lateinit var linLayChgNom: LinearLayout
    private lateinit var btnChgNom: ImageButton
    private lateinit var btnHabiChgDir: ImageButton
    private lateinit var linLayChgDir: LinearLayout
    private lateinit var btnChgDir: ImageButton
    // Elementos del bundle de usuario
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    // Bandera de activacion para el spínner
    private var bandeListen = false
    private var selEstaNom = ""
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_sis_stations)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))

        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            Toast.makeText(this@ManageSisStationsActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
            sistema = bundle.getString("sistema").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Menu Estaciones del Sistema"
        // Relacionando los elementos con su objeto de la interfaz
        spEstaciones = findViewById(R.id.spSistemStations)
        txtNomEsta = findViewById(R.id.txtNomSelStation)
        txtDirEsta = findViewById(R.id.txtDirIPSelStation)
        btnHabiChgNom = findViewById(R.id.btnHabiChgNomEsta)
        linLayChgNom = findViewById(R.id.linLayChgNomEsta)
        btnChgNom = findViewById(R.id.btnChgNomEsta)
        btnHabiChgDir = findViewById(R.id.btnHabiChgDirEsta)
        linLayChgDir = findViewById(R.id.linLayChgDirEsta)
        btnChgDir = findViewById(R.id.btnChgDirEsta)

        // Inicializando instancia hacia el nodo raiz de la BD y la autenticacion
        database = Firebase.database
        auth = FirebaseAuth.getInstance()

        // Rellenando el spinner
        rellEstaciones()
    }

    private fun addListeners(){
        spEstaciones.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!bandeListen) {
                    bandeListen = true
                    return
                }
                // Estableciendo la estacion a gestionar y la informacion a mostrar
                selEstaNom = parent!!.getItemAtPosition(position).toString()
                setInfo(selEstaNom)
            }
        }
        btnHabiChgNom.setOnClickListener {
            // Al ser un boton de activar tambien podra desactivar el cambio de nombre
            txtNomEsta.isEnabled = !txtNomEsta.isEnabled
            linLayChgNom.isGone = !linLayChgNom.isGone
        }
        btnHabiChgDir.setOnClickListener {
            // Al ser un boton de activar tambien podra desactivar el cambio de direccion IP
            txtDirEsta.isEnabled = !txtDirEsta.isEnabled
            linLayChgDir.isGone = !linLayChgDir.isGone
        }
        btnChgNom.setOnClickListener {
            // Solo se hara el proceso de cambio si el ultimo estado del campo del nombre esta habilitado
            if(txtNomEsta.isEnabled){
                cambioNombreEsta(txtNomEsta.text.toString())
            }
        }
        btnChgDir.setOnClickListener {
            // Solo se hara el proceso de cambio si el ultimo estado del campo de la direccion esta habilitado
            if(txtDirEsta.isEnabled){
                cambioDirEsta(txtDirEsta.text.toString())
            }
        }
    }

    private fun rellEstaciones(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellStas = async {
                val lstStations = resources.getStringArray(R.array.lstStaSis)
                val arrStations = ArrayList<String>()
                arrStations.addAll(lstStations)
                ref = database.getReference("Estaciones")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objEsta in dataSnapshot.children) {
                            if(objEsta.child("sistema_Rel").value.toString() == sistema){
                                arrStations.add(objEsta.child("nombre").value.toString())
                            }
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapEsta = ArrayAdapter(this@ManageSisStationsActivity, android.R.layout.simple_spinner_item, arrStations)
                        adapEsta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spEstaciones.adapter = adapEsta
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ManageSisStationsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellStas.await()
        }
    }

    private fun setInfo(estacion: String){
        lifecycleScope.launch(Dispatchers.IO) {
            val setInfo = async {
                ref = database.getReference("Estaciones")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for(objEsta in dataSnapshot.children){
                            if (objEsta.child("nombre").value.toString() == estacion){
                                txtNomEsta.setText(objEsta.child("nombre").value.toString())
                                txtDirEsta.setText(objEsta.child("dir_IP").value.toString())
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ManageSisStationsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setInfo.await()
        }
    }

    private fun avisoGesEsta(mensaje: String) {
        val aviso = AlertDialog.Builder(this@ManageSisStationsActivity)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun retorno(){
        return this.onBackPressedDispatcher.onBackPressed()
    }

    private fun cambioNombreEsta(nNomEsta: String){
        // Evaluar si el campo de texto no esta vacio
        if(nNomEsta.isBlank()){
            val msg = "Error: No se ingreso un nuevo nombre para la estación"
            avisoGesEsta(msg)
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                val upNomSta = async {
                    ref = database.getReference("Estaciones")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for(objEsta in dataSnapshot.children){
                                // Se buscara la estacion por el nombre seleccionado en el spinner
                                if (objEsta.child("nombre").value.toString() == selEstaNom){
                                    // Posteriomente, se evaluara si el nuevo nombre escrito no es igual al actual
                                    if(objEsta.child("nombre").value.toString() != nNomEsta){
                                        val upNomEstaFire = objEsta.child("nombre").ref.setValue(nNomEsta)
                                        upNomEstaFire.addOnSuccessListener {
                                            lifecycleScope.launch(Dispatchers.Main){
                                                val msg = "La estación $selEstaNom fue renombrada a $nNomEsta satisfactoriamente"
                                                avisoGesEsta(msg)
                                            }
                                            Timer().schedule(1500){
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    retorno()
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                    finish()
                                                }
                                            }
                                        }
                                        upNomEstaFire.addOnFailureListener {
                                            Toast.makeText(this@ManageSisStationsActivity,"Error: La estación $selEstaNom NO fue renombrada a $nNomEsta satisfactoriamente",Toast.LENGTH_SHORT).show()
                                        }
                                    }else{
                                        val msg = "Error: El nuevo nombre de la estación no puede ser igual al actual"
                                        avisoGesEsta(msg)
                                    }
                                    break
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ManageSisStationsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                upNomSta.await()
            }
        }
    }

    private fun evalDirIP(valDirIP: String): Boolean{
        // Primero se fragmenta la cadena de texto en los 4 segmentos posibles separados por el .
        val longi = valDirIP.split('.')
        // Si el arreglo de separacion no contiene los 4 elementos posibles de una direccion IP, directamente se rechaza el proceso
        if(longi.size != 4){
            return false
        }
        // En caso de continuar el proceso, se evalua cada seccion separada
        for(segmento in longi){
            // Si el "segmento" no contiene puros numeros, es entero y mayor a 255, es entero y menor a 0 y contiene números que inician con 0 (01, 002, etc.) se toma como direccion IP invalida y se rechaza el proceso
            if((!Regex("""\d+""").containsMatchIn(segmento)) || (segmento.toIntOrNull()!! > 255) || (segmento.toIntOrNull()!! < 0) || (Regex("""^0[0-9]{1,2}""").containsMatchIn(segmento))){
                return false
            }
        }
        // Si se llega a este punto, es porque se esquivaron los posibles errores y se da por buena la direccion ingresada
        return true
    }

    private fun cambioDirEsta(nDir: String){
        // Si la cadena no corresponde a una direccion IPV4 valida, se lanzara un error, caso contrario se continua con el proceso
        if(!evalDirIP(nDir)){
            val msg = "Error: La dirección ingresada no corresponde al formato valido de una dirección IP correcta.\n\n" +
                    "El formato adecuado es nnn.nnn.nnn.nnn\n\n" +
                    "Donde n es un digito, favor de consultar su modem o router de internet para redactar la dirección correcta"
            avisoGesEsta(msg)
        }else {
            lifecycleScope.launch(Dispatchers.IO) {
                val upDirSta = async {
                    ref = database.getReference("Estaciones")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for(objEsta in dataSnapshot.children){
                                // Se buscara la estacion por el nombre seleccionado en el spinner
                                if (objEsta.child("nombre").value.toString() == selEstaNom){
                                    val dirActua = objEsta.child("dir_IP").value.toString()
                                    // Posteriomente, se evaluara si la nueva direccion no es igual a la actual
                                    if(dirActua != nDir){
                                        val upDirIPEstaFire = objEsta.child("dir_IP").ref.setValue(nDir)
                                        upDirIPEstaFire.addOnSuccessListener {
                                            lifecycleScope.launch(Dispatchers.Main){
                                                val msg = "La dirección de la estación $selEstaNom fue actualizada a $nDir satisfactoriamente"
                                                avisoGesEsta(msg)
                                            }
                                            Timer().schedule(1500){
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    retorno()
                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                                    finish()
                                                }
                                            }
                                        }
                                        upDirIPEstaFire.addOnFailureListener {
                                            Toast.makeText(this@ManageSisStationsActivity,"Error: La dirección de la estación $selEstaNom NO fue actualizada",Toast.LENGTH_SHORT).show()
                                        }
                                    }else{
                                        val msg = "Error: La nueva dirección IP de la estación no puede ser igual a la actual"
                                        avisoGesEsta(msg)
                                    }
                                    break
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ManageSisStationsActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                upDirSta.await()
            }
        }
    }
}