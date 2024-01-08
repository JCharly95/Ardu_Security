package com.ardusec.ardu_security.user

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.R
import com.google.android.material.textfield.TextInputEditText
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

class CommentsActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var username: TextInputEditText
    private lateinit var comment: TextInputEditText
    private lateinit var btnComentario: Button
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    // Bundle para extras y saber que campo sera actualizado
    private lateinit var bundle: Bundle
    private lateinit var user: String
    // Dataclass para comentarios
    data class Comentario(val contenido: String, val usuario: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_comments)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,
            R.color.teal_700
        )))

        //Obteniendo el campo
        if(intent.extras == null){
            Toast.makeText(this@CommentsActivity, "Error: no se pudo obtener el campo solicitado", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Opiniones/Comentarios"
        // Relacionando los elementos de la pantalla
        username = findViewById(R.id.txtUserComm)
        username.setText(user)
        comment = findViewById(R.id.txtComentario)
        btnComentario = findViewById(R.id.btnConfGenOp)

        // Inicializando instancia hacia el nodo raiz de la BD y la de la autenticacion
        database = Firebase.database
    }

    private fun avisoComments() {
        val mensaje = "Favor de ingresar su opinion antes de enviarla, gracias"
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

    private fun addListeners() {
        btnComentario.setOnClickListener {
            if(comment.text!!.isNotBlank()){
                lifecycleScope.launch(Dispatchers.IO) {
                    val setComm = async {
                        val nComm = Comentario(comment.text.toString(), user)
                        ref = database.getReference("Comentarios")
                        val key = ref.push().key
                        ref.child(key.toString()).setValue(nComm)
                        // Usuario
                        ref = database.getReference("Usuarios")
                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for(objUs in dataSnapshot.children){
                                    if(objUs.key.toString() == user){
                                        objUs.ref.child("comentarios").child(key.toString()).setValue(true).addOnSuccessListener {
                                            Toast.makeText(this@CommentsActivity, "Su opinion fue enviada satisfactoriamente", Toast.LENGTH_SHORT).show()
                                            Timer().schedule(1500){
                                                lifecycleScope.launch(Dispatchers.Main){
                                                    retorno()
                                                    finish()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@CommentsActivity,"Datos recuperados parcialmente o sin recuperar",Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    setComm.await()
                }
            }else {
                avisoComments()
            }
        }
    }
}