package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class EditDataUsSpActivity : AppCompatActivity() {
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
    private lateinit var campo: String
    private lateinit var user: String
    // Variable para obtener la key vieja, tanto sistema como pregunta
    private lateinit var keyVieja: String

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data_us_sp)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))
        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@EditDataUsSpActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            campo = bundle.getString("campo").toString()
            user = bundle.getString("usuario").toString()
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
        lblHeadSp.text = lblHeadSp.text.toString() + "\n" + campo
        btnConfCamb.text = btnConfCamb.text.toString() + "\n" + campo

        // Actualizar los elementos del formulario acorde al cambio solicitado
        setFormulario()
    }

    private fun addListeners(){
        btnAyuda.setOnClickListener {
            val msg = "Consideraciones de campos: \n\n" +
                    "Nombre;\n" +
                    "* Su nombre no debe tener numeros\n" +
                    "* Su nombre debe tener al menos 10 caracteres\n\n" +
                    "Correo; Formato Aceptado:\n" +
                    "* usuario@dominio.com(.mx)\n\n" +
                    "Contraseña:\n" +
                    "* Extension minima de 8 caracteres\n" +
                    "* Por lo menos una mayuscula\n" +
                    "* Por lo menos un numero\n" +
                    "* Por lo menos  un caracter especial\n\n" +
                    "Administradores; Numero Telefonico:\n" +
                    "* Solo se permiten numeros\n" +
                    "* Lada + Numero ó Tel. Celular\n\n" +
                    "** NOTA: Para el cambio de correo o contraseña, se le " +
                    "solicitara la contraseña como confirmacion de cambio."
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

    private fun rellSpinPregs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rellPregs = async {
                // Obtener el arreglo de strings establecido para las preguntas
                val lstPregs = resources.getStringArray(R.array.lstSavQues)
                var arrPregs = ArrayList<String>()
                arrPregs.addAll(lstPregs)
                // Creando la referencia de la coleccion de preguntas en la BD
                ref = database.getReference("Preguntas")
                // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                ref.get().addOnSuccessListener{ taskGet ->
                    for (objPreg in taskGet.children){
                        objPreg.ref.child("Val_Pregunta").get().addOnSuccessListener { taskAdd ->
                            arrPregs.add(taskAdd.value.toString())
                        }
                    }
                    // Estableciendo el adaptador para el rellenado del spinner
                    val adapPregs = ArrayAdapter(this@EditDataUsSpActivity, android.R.layout.simple_spinner_item, arrPregs)
                    adapPregs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spNPreg.adapter = adapPregs
                }
                    .addOnFailureListener {
                        Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
            }
            rellPregs.await()
        }
    }

    private fun rellSpinSis() {
        lifecycleScope.launch(Dispatchers.IO) {
            val rellSis = async {
                // Obtener el arreglo de strings establecido para los sistemas
                val lstSists = resources.getStringArray(R.array.lstSistems)
                var arrSists = ArrayList<String>()
                arrSists.addAll(lstSists)
                // Creando la referencia de la coleccion de sistemas en la BD
                ref = database.getReference("Sistemas")
                // Agregando un ValueEventListener para operar con las instancias de pregunta
                ref.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        for (objSis in dataSnapshot.children){
                            objSis.ref.child("nombre_Sis").get().addOnSuccessListener { taskGet ->
                                arrSists.add(taskGet.value.toString())
                            }
                        }
                        // Estableciendo el adaptador para el rellenado del spinner
                        val adapSis = ArrayAdapter(this@EditDataUsSpActivity, android.R.layout.simple_spinner_item, arrSists)
                        adapSis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spNSis.adapter = adapSis
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            rellSis.await()
        }
    }

    private fun rellTipos() {
        val adapter = ArrayAdapter.createFromResource(this@EditDataUsSpActivity, R.array.lstTipUs, android.R.layout.simple_spinner_item)
        spNTipo.adapter = adapter
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
                        // Creando la referencia de la coleccion de preguntas en la BD
                        ref = database.getReference("Preguntas")
                        // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                        ref.get().addOnSuccessListener{ taskGetPreg ->
                            for (objPreg in taskGetPreg.children){
                                val refPregUs = objPreg.child("usuarios").ref
                                refPregUs.get().addOnSuccessListener { taskGetUser ->
                                    for(objUs in taskGetUser.children){
                                        if(objUs.key.toString() == user){
                                            keyVieja = objPreg.key.toString()
                                            txtValVie.text = objPreg.child("Val_Pregunta").value.toString()
                                            break
                                        }
                                    }
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getPreg.await()
                }
            }
            "Sistema" -> {
                // Mostrar el spinner de los sistemas y rellenarlo
                mateCarSis.isGone = false
                rellSpinSis()
                // Buscar en la BD la informacion del sistema actual del usuario
                lifecycleScope.launch(Dispatchers.IO) {
                    val getSis = async {
                        // Creando la referencia de la coleccion de sistemas en la BD
                        ref = database.getReference("Sistemas")
                        // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                        ref.get().addOnSuccessListener{ taskGetSis ->
                            for (objSis in taskGetSis.children){
                                val refSisUs = objSis.child("usuarios").ref
                                refSisUs.get().addOnSuccessListener { taskGetUser ->
                                    for(objUs in taskGetUser.children){
                                        if(objUs.key.toString() == user){
                                            keyVieja = objSis.key.toString()
                                            txtValVie.text = objSis.child("nombre_Sis").value.toString()
                                            break
                                        }
                                    }
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getSis.await()
                }
            }
            "Tipo" -> {
                mateCarTip.isGone = false
                rellTipos()
                // Buscar en la BD la informacion del tipo de usuario actual del usuario
                lifecycleScope.launch(Dispatchers.IO) {
                    val getTip = async {
                        // Creando la referencia de la coleccion de usuarios en la BD
                        ref = database.getReference("Usuarios")
                        // Ya que las preguntas son valores estaticos y no se cambiaran con el tiempo, se optará por usar Get para una sola toma de valores
                        ref.get().addOnSuccessListener{ taskGetSis ->
                            for (objUs in taskGetSis.children){
                                if(objUs.key.toString() == user){
                                    txtValVie.text = objUs.child("tipo_Usuario").value.toString()
                                }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this@EditDataUsSpActivity,"Error: Datos parcialmente obtenidos",Toast.LENGTH_SHORT).show()
                            }
                    }
                    getTip.await()
                }
            }
        }
    }

    // Validaciones de campos
    private fun validarSelPreg(lista: Spinner): Boolean {
        return if(lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su pregunta clave"){
            true
        }else{
            avisoActu("Error: Favor de seleccionar una pregunta")
            false
        }
    }
    private fun validarSelSis(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su Sistema") {
            true
        }else{
            avisoActu("Error: Favor de seleccionar un sistema")
            false
        }
    }

    private fun validarSelTipo(lista: Spinner): Boolean {
        return if (lista.selectedItemPosition != 0 || lista.selectedItem.toString() != "Seleccione su tipo de usuario") {
            true
        }else{
            avisoActu("Error: Favor de seleccionar un tipo de usuario")
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
                                // Creando la referencia al subArreglo de los usuarios en las preguntas
                                val refPregUs = objPreg.child("usuarios").ref
                                refPregUs.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (objUs in snapshot.children) {
                                            if(objUs.key.toString() == user) {
                                                Log.w("Key del Usuario en la pregunta", objUs.key.toString())
                                                objUs.ref.removeValue()
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@EditDataUsSpActivity,"El usuario no fue encontrado en la relacion",Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"No se pudieron ver las preguntas",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delPregUs.await()
            // Luego se establecera la nueva relacion con la pregunta actualizada
            val setPregUs = async {
                // Creando la relacion del usuario con la pregunta en la entidad Preguntas
                ref = database.getReference("Preguntas")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objPreg in dataSnapshot.children) {
                            if (objPreg.child("Val_Pregunta").value.toString() == pregunta)
                                objPreg.ref.child("usuarios").child(user).setValue(true)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setPregUs.await()
            // Y finalmente se establecera el nuevo valor de la pregunta en el usuario
            val setUsPreg = async {
                // Actualizando el valor en la entidad de usuarios
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                objUser.ref.child("pregunta_Seg").setValue("pregunta${listPregs.selectedItemPosition}").addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Su pregunta fue actualizada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataUsSpActivity, "Error: Su pregunta no pudo ser actualizada", Toast.LENGTH_SHORT).show()
                                        spNPreg.setSelection(0)
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setUsPreg.await()
        }
    }
    private fun actSis(listSis: Spinner){
        // Valor textual del nuevo sistema
        val sistema = listSis.selectedItem.toString()
        // Corrutina con las acciones a realizar en la BD
        lifecycleScope.launch(Dispatchers.IO) {
            // Primero se borrará el registro actual del usuario en la entidad de los sistemas
            val delSisUs = async {
                // Creando la referencia de la coleccion de preguntas en la BD
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
                                        Toast.makeText(this@EditDataUsSpActivity,"El usuario no fue encontrado en la relacion",Toast.LENGTH_SHORT).show()
                                    }
                                })
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"No se pudieron ver los sistemas",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            delSisUs.await()
            // Luego se establecera la nueva relacion con el sistema actualizado
            val setSisUs = async {
                // Creando la relacion del usuario con el sistema en la entidad Sistemas
                ref = database.getReference("Sistemas")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objSis in dataSnapshot.children) {
                            if (objSis.child("nombre_Sis").value.toString() == sistema)
                                objSis.ref.child("usuarios").child(user).setValue(true)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setSisUs.await()
            // Y finalmente se establecera el nuevo valor del sistema en el usuario
            val setUsSis = async {
                // Actualizando el valor en la entidad de usuarios
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                val sisUser = objUser.ref.child("sistemas")
                                sisUser.addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(sistems in snapshot.children){
                                            if(sistems.value.toString() == keyVieja){
                                                sistems.ref.setValue("sistema${listSis.selectedItemPosition}").addOnSuccessListener {
                                                    Toast.makeText(this@EditDataUsSpActivity, "Su sistema fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                                    Timer().schedule(1500){
                                                        lifecycleScope.launch(Dispatchers.Main){
                                                            retorno()
                                                            finish()
                                                        }
                                                    }
                                                }
                                                    .addOnFailureListener {
                                                        Toast.makeText(this@EditDataUsSpActivity, "Error: Su sistema no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                                        spNSis.setSelection(0)
                                                    }
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setUsSis.await()
        }
    }

    private fun actTipo(listTipo: Spinner){
        // Valor textual del nuevo tipo
        val tipo = listTipo.selectedItem.toString()
        // Corrutina con las acciones a realizar en la BD
        lifecycleScope.launch(Dispatchers.IO) {
            val setTipo = async {
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUs in dataSnapshot.children) {
                            if(objUs.key.toString() == user){
                                objUs.ref.child("tipo_Usuario").setValue(tipo).addOnSuccessListener {
                                    Toast.makeText(this@EditDataUsSpActivity, "Su tipo fue actualizado satisfactoriamente", Toast.LENGTH_SHORT).show()
                                    Timer().schedule(1500){
                                        lifecycleScope.launch(Dispatchers.Main){
                                            retorno()
                                            finish()
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditDataUsSpActivity, "Error: Su tipo no pudo ser actualizado", Toast.LENGTH_SHORT).show()
                                        spNSis.setSelection(0)
                                    }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditDataUsSpActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            setTipo.await()
        }
    }
}