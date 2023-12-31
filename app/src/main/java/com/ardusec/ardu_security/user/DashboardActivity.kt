package com.ardusec.ardu_security.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.ardusec.ardu_security.MainActivity
import com.ardusec.ardu_security.ManualUserActivity
import com.ardusec.ardu_security.R
import com.ardusec.ardu_security.admin.ManageSisActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule


class DashboardActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion
    private lateinit var btnAyuda: ImageButton
    private lateinit var btnMenEsta: ImageButton
    private lateinit var btnMenRepo: ImageButton
    private lateinit var btnMenAjus: ImageButton
    private lateinit var btnMenManu: ImageButton
    private lateinit var linLayGesSis: LinearLayout
    private lateinit var btnMenGesSis: ImageButton
    private lateinit var btnCerSes: ImageButton
    // Elementos del bundle de acceso/registro
    private lateinit var bundle: Bundle
    private lateinit var user: String
    private lateinit var tipo: String
    private lateinit var sistema: String
    // Instancias de Firebase; Database y ReferenciaDB
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var notifications: FirebaseMessaging
    // Verificando el permiso de notificaciones de la aplicacion
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this@DashboardActivity, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@DashboardActivity,"Ardu Security no puede enviarle notificaciones hasta que autorice el permiso adecuado",Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_dashboard)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@DashboardActivity,R.color.teal_700)))
        //Obteniendo los valores de acceso/registro
        if(intent.extras == null) {
            tipo = "Cliente"
            Toast.makeText(this@DashboardActivity, "Error: no se pudo obtener la informacion del usuario", Toast.LENGTH_SHORT).show()
        }else{
            bundle = intent.extras!!
            user = bundle.getString("username").toString()
        }

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
        // Configurando el backPressed
        onBackPressedDispatcher.addCallback(this, presAtrasCallback)
    }

    private val presAtrasCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val msg = "Boton deshabilitado.\n\n " +
                "Si desea regresar al inicio o iniciar sesion, favor de cerrar su sesión antes."
            avisoDash(msg)
        }
    }

    private fun setUp() {
        // Titulo de la pantalla
        title = "Dashboard"
        // Relacionando los elementos con su objeto de la interfaz
        btnAyuda = findViewById(R.id.btnInfoDash)
        btnMenEsta = findViewById(R.id.btnStats)
        btnMenRepo = findViewById(R.id.btnGenRep)
        btnMenAjus = findViewById(R.id.btnAjuste)
        btnMenManu = findViewById(R.id.btnManUs)
        linLayGesSis = findViewById(R.id.linLayBtnGesSis)
        btnMenGesSis = findViewById(R.id.btnGesSis)
        btnCerSes = findViewById(R.id.btnCerrSes)
        // Inicializando instancia hacia el nodo raiz de la BD, la de la autenticacion y el servicio de mensajes
        database = Firebase.database
        auth = FirebaseAuth.getInstance()
        notifications = FirebaseMessaging.getInstance()
        // Inicializando las variables de tipo y sistema
        tipo = ""
        sistema = ""
        // Estableciendo Admin
        setInfo()
    }

    private fun addListeners() {
        // Agregar los listener
        btnAyuda.setOnClickListener {
            val msg = "--Descripciones de los elementos en pantalla:\n\n" +
                "* Boton Estaciones: Lleva al usuario a la ventana de visualización de las estaciones. \n\n" +
                "* Boton Reportes: Lleva al usuario a la ventana de generación de reportes. \n\n" +
                "* Boton Ajustes: Lleva al usuario a la ventana de ajustes de información y otros elementos relacionados con el usuario. \n\n" +
                "* Boton Manual de Usuario: Lleva al usuario a la ventana de visualizacion del manual de usuario. \n\n" +
                "* Boton Gestionar Sistema: Lleva a los administradores a la ventana de gestion del sistema. \n\n" +
                "* Boton Cerrar Sesion: Cierra la sesion actual y redigire al usuario al inicio de sesion."
            avisoDash(msg)
        }
        btnMenEsta.setOnClickListener {
            val statsActi = Intent(this@DashboardActivity, MenuStationsActivity::class.java).apply {
                putExtra("username", user)
                putExtra("sistema", sistema)
            }
            startActivity(statsActi)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnMenRepo.setOnClickListener {
            val reportActi = Intent(this@DashboardActivity, GenReportsActivity::class.java).apply {
                putExtra("username", user)
                putExtra("sistema", sistema)
            }
            startActivity(reportActi)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnMenAjus.setOnClickListener {
            val settingActi = Intent(this@DashboardActivity, SettingsActivity::class.java).apply {
                putExtra("username", user)
                putExtra("sistema", sistema)
            }
            startActivity(settingActi)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnMenManu.setOnClickListener {
            val manuActi = Intent(this@DashboardActivity, ManualUserActivity::class.java)
            startActivity(manuActi)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnMenGesSis.setOnClickListener {
            val gesSisActi = Intent(this@DashboardActivity, ManageSisActivity::class.java).apply {
                putExtra("username", user)
                putExtra("sistema", sistema)
            }
            startActivity(gesSisActi)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnCerSes.setOnClickListener {
            // Cerrar Sesion en Firebase
            auth.signOut()
            // Lanzar la app hacia la primera ventana
            Toast.makeText(this@DashboardActivity, "Cerrando Sesion... Espere un momento", Toast.LENGTH_SHORT).show()
            //avisoDash("Cerrando Sesion... Espere un momento")
            Timer().schedule(1500){
                val endActi = Intent(this@DashboardActivity, MainActivity::class.java)
                startActivity(endActi)
                finish()
            }
        }
    }

    private fun avisoDash(mensaje: String) {
        val aviso = AlertDialog.Builder(this@DashboardActivity)
        aviso.setTitle("Aviso")
        aviso.setMessage(mensaje)
        aviso.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = aviso.create()
        dialog.show()
    }

    private fun setInfo(){
        lifecycleScope.launch(Dispatchers.IO){
            val infoFire = async {
                // Buscando al usuario en la BD
                ref = database.getReference("Usuarios")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (objUser in dataSnapshot.children) {
                            if(objUser.key.toString() == user){
                                // Mostrar el boton de gestion de sistema si es administrador
                                if(objUser.child("tipo_Usuario").value.toString() == "Administrador"){
                                    linLayGesSis.isGone = false
                                }
                                // Estableciendo el sistema del usuario
                                sistema = objUser.child("sistema_Rel").value.toString()
                                // Token de notificaciones del dispositivo de usuario
                                notifications.token.addOnCompleteListener { taskTokNoti ->
                                    if(!taskTokNoti.isSuccessful){
                                        Toast.makeText(this@DashboardActivity, "No se pudo obtener el token para las notificaciones de Ardu Security", Toast.LENGTH_SHORT).show()
                                    }
                                    objUser.child("tokenNotificaciones").ref.setValue(taskTokNoti.result)
                                    Toast.makeText(this@DashboardActivity, "Token de notificaciones Ardu Security creado", Toast.LENGTH_SHORT).show()
                                }
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DashboardActivity, "Error: Usuario innacesible", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            infoFire.await()
        }

        // Despues de la configuracion inicial se pregunta por el permiso de notificaciones
        preguntarPermisoNotificaciones()
    }

    private fun preguntarPermisoNotificaciones(){
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@DashboardActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                Toast.makeText(this@DashboardActivity, "Su dispositivo podrá recibir notificaciones de la aplicación", Toast.LENGTH_SHORT).show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}