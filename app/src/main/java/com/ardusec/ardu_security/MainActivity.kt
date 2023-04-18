package com.ardusec.ardu_security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Estableciendo la estructura de la BD en Firebase
        setDBEstruc()

        val context = applicationContext
        Toast.makeText(context, "Bienvenido a Ardu Security", Toast.LENGTH_SHORT).show()

        Timer().schedule(2000){
            val intentLogin = Intent(context, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }
    private fun setDBEstruc(){
        // Variable con relacion a la BD en firebase
        val database = Firebase.database

        /* Estructura de creacion de la entidad alarma
        // Variable que contiene la referencia a establecer
        val myRef = database.getReference("Alarma")
        myRef.child("Estado").setValue(false)*/

        /* Estructura de creacion para la coleccion de archivo con valor generico
        // HashMap es el equivalente a crear un array de 2 elementos
        var archi = HashMap<String, String>()
        // Se añade un registro como par ordenado
        archi["Nom_Archi"] = "ManualPDF"
        archi["Ubi_Save"] = "/CentroMando/home/..."
        // Variable que contiene la referencia a establecer
        val ref = database.getReference("Archivo")
        // Se necesita el metodo push para que firebase genere el id
        var pushVals = ref.push()
        // Se ingresa el registro en la BD
        pushVals.setValue(archi)*/

        /* Estructura de creacion para la coleccion de preguntas
        // HashMap es el equivalente a crear un array de 2 elementos
        var preguntas = HashMap<String, String>()
        // Se añaden los registros como pares ordenados
        //preguntas["Val_Pregunta"] = "Como se llama tu mascota"
        //preguntas["Val_Pregunta"] = "A que ciudad te gustaria viajar"
        //preguntas["Val_Pregunta"] = "Cual es tu comida favorita"
        //preguntas["Val_Pregunta"] = "En donde se conocio tu familia"
        //preguntas["Val_Pregunta"] = "En que escuela secundaria estuviste"
            Tuve que ir descomentando de una por una para que entraran
        // Variable que contiene la referencia a establecer
        val ref = database.getReference("Pregunta")
        // Se necesita el metodo push para que firebase genere el id
        var pushVals = ref.push()
        pushVals.setValue(preguntas)*/

        /* Estructura de los tipos de usuario
        // HashMap es el equivalente a crear un array de 2 elementos
        var tipUsers = HashMap<String, String>()
        // Se añaden los registros como pares ordenados
        //tipUsers["Nom_User_Tip"] = "Administrador"
        tipUsers["Nom_User_Tip"] = "Cliente"
        // Variable que contiene la referencia a establecer
        val ref = database.getReference("User_Tipo")
        // Se necesita el metodo push para que firebase genere el id
        var pushVals = ref.push()
        pushVals.setValue(tipUsers)*/

        /* Estructura de sistema
        // HashMap es el equivalente a crear un array de 2 elementos
        var sisInfo = HashMap<String, String>()
        var sisInfo2 = HashMap<String, Long>()
        // Se añaden los registros como pares ordenados
        sisInfo["Nombre_Sis"] = "SisGen1Prueba"
        sisInfo2["Ulti_Cam_Nom"] = 1681723684445
        // Variable que contiene la referencia a establecer
        val ref = database.getReference("Sistema")
        // Se necesita el metodo push para que firebase genere el id
        var pushVals = ref.push()
        pushVals.setValue(sisInfo)
        pushVals.setValue(sisInfo2)
        Tuve que insertar el segundo arreglo de valores directo en firebase, no se garantiza que esto se pueda mover con programacion
         */

        // HashMap es el equivalente a crear un array de 2 elementos
        val user = HashMap<String, String>()
        // Se añaden los registros como pares ordenados
        user["Nombre"] = "Juan Carlos Hernandez Lopez"
        user["Correo"] = "juancarloscharly@live.com.mx"
        user["Contra"] = "Ingeniero2595!"
        // Variable que contiene la referencia a establecer
        val ref = database.getReference("Usuarios")
        // Se necesita el metodo push para que firebase genere el id
        val pushVals = ref.push()
        pushVals.setValue(user)
    }
}