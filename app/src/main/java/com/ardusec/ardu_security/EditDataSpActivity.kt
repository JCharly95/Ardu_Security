package com.ardusec.ardu_security

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.user.DashboardActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class EditDataSpActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var lblHeadSp: TextView
    private lateinit var btnAyuda: ImageButton
    private lateinit var txtValVie: TextView
    private lateinit var spNPreg: AppCompatSpinner
    private lateinit var mateCarPregs: MaterialCardView
    private lateinit var spNSis: AppCompatSpinner
    private lateinit var mateCarSis: MaterialCardView
    private lateinit var spNTipo: AppCompatSpinner
    private lateinit var mateCarTip: MaterialCardView
    private lateinit var btnConfCamb: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var sistema: String
    private lateinit var campo: String
    private lateinit var keyVieja: String
    private lateinit var tipo: String

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_sp)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.teal_700)))
        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@EditDataSpActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("usuario").toString()
            sistema = bundle.getString("sistema").toString()
            campo = bundle.getString("campo").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Actualizar Informacion"
        // Relacionando los elementos con su objeto de la interfaz
        lblHeadSp = findViewById(R.id.lblHeadEditSp)
        btnAyuda = findViewById(R.id.btnInfoActuSp)
        txtValVie = findViewById(R.id.txtValVieSp)
        mateCarPregs = findViewById(R.id.linLayNewPreg)
        spNPreg = findViewById(R.id.spUpdPregSegu)
        mateCarSis = findViewById(R.id.linLayNewSis)
        spNSis = findViewById(R.id.spUpdSis)
        mateCarTip = findViewById(R.id.linLayNewTipo)
        spNTipo = findViewById(R.id.spUpdTipoUser)
        btnConfCamb = findViewById(R.id.btnConfChgSp)
        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        auth = FirebaseAuth.getInstance()
        database = Firebase.database
        // Establecer el encabezado y el boton, acorde al campo a actualizar
        val titPant = "${lblHeadSp.text}\n$campo"
        val btnText = "${btnConfCamb.text}\n$campo"
        lblHeadSp.text = titPant
        btnConfCamb.text = btnText
        // Estableciendo la key vieja para el cambio de sistema
        keyVieja = ""
        tipo = ""

        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun addListeners(){
        btnAyuda.setOnClickListener {
            val msg = "Instrucciones: \n\n" +
                    "* Se mostrará el valor actual solicitado para cambiar y deberá seleccionar el nuevo valor a establecer en la lista desplegable;\n\n" +
                    "- NOTA: Si no puede concluir con el proceso, revise el mensaje de error mostrado en pantalla o regrese a la vista anterior\n"
            avisoActu(msg)
        }
        btnConfCamb.setOnClickListener{
            lifecycleScope.launch(Dispatchers.IO) {
                val confChg = async {
                    when (campo) {
                        "Pregunta" -> {
                            if(validarSelPreg(spNPreg))
                                actPreg(spNPreg)
                        }
                        "Sistema" -> {
                            if(validarSelSis(spNSis))
                                actSis(spNSis)
                        }
                        "Tipo" -> {
                            if(validarSelTipo(spNTipo))
                                actTipo(spNTipo)
                        }
                        else -> {
                            avisoActu("Error: El campo seleccionado no fue encontrado")
                        }
                    }
                }
                confChg.await()
            }
        }
    }

    private fun rellSpinPregs(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellPregs = async {
                // Obtener el arreglo de strings establecido para las preguntas
                val lstPregs = resources.getStringArray(R.array.lstSavQues)
                val arrPregs = ArrayList<String>()
                arrPregs.addAll(lstPregs)
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objPreg in dataSnapshot.children){
                            arrPregs.add(objPreg.child("Val_Pregunta").value.toString())
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapPregs = ArrayAdapter(this@EditDataSpActivity, android.R.layout.simple_spinner_item, arrPregs)
                        adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spNPreg.adapter = adapPregs
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity, "Error: Consulta Incompleta; Causa: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellPregs.await()
        }
    }

    private fun rellSpinSis(){
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                val arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children){
                            arrSists.add(objSis.child("nombre_Sis").value.toString())
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(this@EditDataSpActivity, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spNSis.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity, "Error: Consulta Incompleta; Causa: $databaseError", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellSis.await()
        }
    }

    private fun rellTipos() {
        lifecycleScope.launch(Dispatchers.IO){
            val setTipos = async {
                // Obtener el arreglo de strings establecido para los tipos y agregarlos a un ArrayList de Strings
                val lstTipos = resources.getStringArray(R.array.lstTipUs)
                val arrTipos = ArrayList<String>()
                arrTipos.addAll(lstTipos)
                // Estableciendo el adaptador para el rellenado del spinner
                val adadTipos = ArrayAdapter(this@EditDataSpActivity, android.R.layout.simple_spinner_item, arrTipos)
                adadTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spNSis.adapter = adadTipos
            }
            setTipos.await()
        }
    }

    private fun avisoActu(mensaje: String) {
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

    private fun setFormulario(){
        when(campo){
            "Pregunta" -> {
                // Mostrar el spinner de las preguntas y rellenarlo
                mateCarPregs.isGone = false
                rellSpinPregs()
                // Buscar en la BD la informacion del sistema actual del usuario
                lifecycleScope.launch(Dispatchers.IO) {
                    val getPreg = async {
                        ref = database.getReference("Preguntas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (objPreg in dataSnapshot.children){
                                    val refPregUs = objPreg.child("usuarios").ref
                                    refPregUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(objUs in snapshot.children){
                                                if(objUs.key.toString() == user){
                                                    txtValVie.text = objPreg.child("Val_Pregunta").value.toString()
                                                    break
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getPreg.await()
                }
            }
            "Sistema" -> {
                // Mostrar los spinner de sistemas y tipo, luego rellenarlos
                mateCarSis.isGone = false
                rellSpinSis()
                // Buscar en la BD la informacion del sistema actual del usuario
                lifecycleScope.launch(Dispatchers.IO) {
                    val getSis = async {
                        // Creando la referencia de la coleccion de sistemas en la BD
                        ref = database.getReference("Sistemas")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objSis in dataSnapshot.children){
                                    val refSisUs = objSis.child("usuarios").ref
                                    refSisUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(objUs in snapshot.children){
                                                if(objUs.key.toString() == user){
                                                    keyVieja = objSis.key.toString()
                                                    txtValVie.text = objSis.child("nombre_Sis").value.toString()
                                                    break
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getSis.await()
                    val getTipo = async {
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children){
                                    if(objUs.key.toString() == user){
                                        tipo = objUs.child("tipo_Usuario").value.toString()
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getTipo.await()
                }
            }
            "Tipo" -> {
                mateCarTip.isGone = false
                rellTipos()
                // Buscar en la BD la informacion del tipo de usuario actual del usuario
                lifecycleScope.launch(Dispatchers.IO) {
                    val getUsTipo = async {
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children){
                                    if(objUs.key.toString() == user){
                                        txtValVie.text = objUs.child("tipo_Usuario").value.toString()
                                        break
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditDataSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    getUsTipo.await()
                }
            }
        }
    }

    // Validaciones de campos
    private fun validarSelPreg(lista: Spinner): Boolean {
        return if(lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su pregunta clave"){
            true
        }else{
            lifecycleScope.launch(Dispatchers.Main){
                avisoActu("Error: Favor de seleccionar una pregunta")
            }
            false
        }
    }
    private fun validarSelSis(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su Sistema") {
            true
        }else{
            lifecycleScope.launch(Dispatchers.Main){
                avisoActu("Error: Favor de seleccionar un sistema")
            }
            false
        }
    }

    private fun validarSelTipo(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su tipo de usuario") {
            true
        }else{
            lifecycleScope.launch(Dispatchers.Main){
                avisoActu("Error: Favor de seleccionar un tipo de usuario")
            }
            false
        }
    }

    //Actualizacion de campos
    private fun actPreg(listPregs: Spinner){
        // Valor textual de la nueva pregunta
        val pregunta = listPregs.selectedItem.toString()
        // Corrutina con las acciones a realizar en la BD
        lifecycleScope.launch(Dispatchers.IO) {
            // Primero se borrará el registro actual del usuario en la entidad de las preguntas
            val delPregUs = async {
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objPreg in dataSnapshot.children){
                            if(objPreg.child("Val_Pregunta").value.toString() == txtValVie.text.toString()){
                                // Creando la referencia al subarreglo de los usuarios en las preguntas y eliminando la relacion vieja
                                val refPregUs = objPreg.child("usuarios").ref
                                refPregUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (objUs in snapshot.children) {
                                            if(objUs.key.toString() == user) {
                                                objUs.ref.removeValue()
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@EditDataSpActivity,"El usuario no fue encontrado en la relacion",Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                            // Luego se establecera la nueva relacion con la pregunta actualizada
                            if (objPreg.child("Val_Pregunta").value.toString() == pregunta) {
                                objPreg.ref.child("usuarios").child(user).setValue(true)
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity,"No se pudieron ver las preguntas",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delPregUs.await()
            // Y finalmente se establecera el nuevo valor de la pregunta en el usuario
            val setUsPreg = async {
                // Actualizando el valor en la entidad de usuarios
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                val setNPreg = objUser.ref.child("pregunta_Seg").setValue("pregunta${listPregs.selectedItemPosition}")
                                setNPreg.addOnSuccessListener {
                                    Timer().schedule(1000) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            chgPanta()
                                            Toast.makeText(this@EditDataSpActivity, "Su pregunta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    Timer().schedule(3000) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            retorno()
                                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                            finish()
                                        }
                                    }
                                }
                                setNPreg.addOnFailureListener {
                                    Toast.makeText(this@EditDataSpActivity, "Error: Su pregunta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                    spNPreg.setSelection(0)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity,"Datos recuperados parcialmente",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setUsPreg.await()
        }
    }
    private fun actSis(listSis: Spinner){
        // Valor textual del nuevo sistema
        val sistema = listSis.selectedItem.toString()
        var keyNueva = ""
        // Corrutina con las acciones a realizar en la BD
        lifecycleScope.launch(Dispatchers.IO) {
            // Primero se borrará el registro actual del usuario en la entidad de los sistemas
            val delSisUs = async {
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSis in dataSnapshot.children){
                            if(objSis.key.toString() == keyVieja){
                                // Creando la referencia al subArreglo de los usuarios en los sistemas
                                val refSisUs = objSis.child("usuarios").ref
                                refSisUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (objUs in snapshot.children) {
                                            if(objUs.key.toString() == user) {
                                                objUs.ref.removeValue()
                                                break
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@EditDataSpActivity,"El usuario no fue encontrado en la relacion",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                            // Luego se establecera la nueva relacion con el sistema actualizado
                            if (objSis.child("nombre_Sis").value.toString() == sistema) {
                                objSis.ref.child("usuarios").child(user).setValue(true)
                                keyNueva = objSis.key.toString()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity,"No se pudieron ver los sistemas",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delSisUs.await()
            // Y finalmente se establecera el nuevo valor del sistema en el usuario
            val setUsSis = async {
                // Actualizando el valor en la entidad de usuarios
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                val setSisUs = objUser.child("sistema_Rel").ref.setValue(keyNueva)
                                setSisUs.addOnSuccessListener {
                                    Timer().schedule(1000) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            chgPanta()
                                            Toast.makeText(this@EditDataSpActivity, "Su sistema fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    Timer().schedule(3000) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            val setNSisActi = Intent(this@EditDataSpActivity, DashboardActivity::class.java).apply {
                                                putExtra("username", user)
                                                putExtra("tipo", tipo)
                                            }
                                            startActivity(setNSisActi)
                                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                            finish()
                                        }
                                    }
                                }
                                setSisUs.addOnFailureListener {
                                    Toast.makeText(this@EditDataSpActivity, "Error: Su sistema no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                    spNSis.setSelection(0)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity,"Datos recuperados parcialmente",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setUsSis.await()
        }
    }

    private fun actTipo(listTipo: Spinner){
        // Valor textual del nuevo tipo
        val tipoSel = listTipo.selectedItem.toString()
        // Corrutina con las acciones a realizar en la BD
        lifecycleScope.launch(Dispatchers.IO) {
            val setTipo = async {
                // Creando la referencia de la coleccion de usuarios en la BD
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                val setNTipo = objUser.child("tipo_Usuario").ref.setValue(tipoSel)
                                setNTipo.addOnSuccessListener {
                                    Timer().schedule(1000) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            chgPanta()
                                            Toast.makeText(this@EditDataSpActivity, "El tipo de usuario fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    Timer().schedule(3000) {
                                        lifecycleScope.launch(Dispatchers.Main){
                                            val setNTipoActi = Intent(this@EditDataSpActivity, DashboardActivity::class.java).apply {
                                                putExtra("username", user)
                                                putExtra("tipo", tipoSel)
                                            }
                                            startActivity(setNTipoActi)
                                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                            finish()
                                        }
                                    }
                                }
                                setNTipo.addOnFailureListener {
                                    Toast.makeText(this@EditDataSpActivity, "Error: Su tipo de usuario no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                    spNTipo.setSelection(0)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataSpActivity,"Datos recuperados parcialmente",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setTipo.await()
        }
    }

    private fun chgPanta(){
        val builder = AlertDialog.Builder(this@EditDataSpActivity).create()
        val view = layoutInflater.inflate(R.layout.charge_transition,null)
        builder.setView(view)
        builder.show()
        Timer().schedule(2000){
            builder.dismiss()
        }
    }
}