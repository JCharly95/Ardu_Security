package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.github.barteksc.pdfviewer.PDFView
import java.io.File
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.util.Timer
import kotlin.concurrent.schedule


class ManualUserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion; Seccion Formulario de Seleccion
    private lateinit var pdfViewer: PDFView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnDescarga: Button
    private lateinit var pdfURL: String
    private lateinit var nombreArchivo: String
    // Codigo de solicitud de descarga
    private val codDownManu = 195

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@ManualUserActivity, R.color.teal_700)))

        // Configurar el arranque de la interfaz
        setUp()
        // Agregar los listeners
        addListeners()
    }

    private fun setUp(){
        // Titulo de la pantalla
        title = "Manual Digital de Usuario"
        // Relacionando los elementos con su objeto de la interfaz; Seccion Formulario de Seleccion
        pdfViewer = findViewById(R.id.webManSis)
        progressBar = findViewById(R.id.progressBar)
        btnDescarga = findViewById(R.id.btnGetManu)
        // Creando la instancia del PRDownloader para obtenerlo en la vista y posteriormente en la descarga si es el caso
        PRDownloader.initialize(this@ManualUserActivity)
        // Mostrar la barra de progreso en lo que carga el documento en el background
        progressBar.isGone = false

        // Preparacion de PDFView para lectura del manual PDF
        pdfURL = "https://firebasestorage.googleapis.com/v0/b/ardusecurity.appspot.com/o/documentos%2FManual_Usuario_V1.pdf?alt=media&token=cb99205f-a5b6-4413-ae60-8a049557ae8b"
        nombreArchivo = "Manual_Usuario_Ardu_Security.pdf"

        // Funcion de obtencion del archivo de internet (Solo visualizacion)
        getPDFInternet(pdfURL, getRutaRaizDir(), nombreArchivo)
    }

    private fun addListeners(){
        btnDescarga.setOnClickListener {
            // Dado que se almacenará el reporte, se debera verificar si se conceden los permisos de guardado, previo al boton de generacion de reporte. Si se autorizaron los permisos previamente, se mostrara un aviso, sino se solicitaran
            if(checarPermisos()){
                Toast.makeText(this@ManualUserActivity, "Permiso de Almacenamiento Concedido...", Toast.LENGTH_SHORT).show()
                progressBar.isGone = false
                // Cuando los permisos esten autorizados, se procedera con la descarga del manual en el ExternalStorage
                descargarManual(pdfURL, getRutaGuardado())
            }else{
                pedirPermiso()
            }
        }
    }

    private fun getPDFInternet(url: String, rutaDir: String, nomArchi: String) {
        val obteManualInternet = PRDownloader.download(url,rutaDir,nomArchi).build()
        obteManualInternet.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                //Toast.makeText(this@ManualUserActivity, "downloadComplete", Toast.LENGTH_LONG).show()
                val downloadedFile = File(rutaDir, nomArchi)
                // Ocultar la barra de progreso al finalizar la carga del archivo
                progressBar.isGone = true
                // Cargar el archivo obtenido en el visualizador
                mostrarManual(downloadedFile)
            }
            override fun onError(error: Error) {
                Toast.makeText(this@ManualUserActivity,"Error en la obtencion del manual: $error",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRutaRaizDir(): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = ContextCompat.getExternalFilesDirs(this@ManualUserActivity,null)[0]
            file.absolutePath
        } else {
            this@ManualUserActivity.filesDir.absolutePath
        }
    }

    private fun getRutaGuardado(): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            file.absolutePath
        } else {
            Environment.getExternalStorageDirectory().absolutePath
        }
    }

    // Revision de permisos para lectura y escritura en el dispositivo
    private fun checarPermisos(): Boolean {
        // Constante de evaluacion para el checar permiso de escritura en el dispositivo
        val writeStoragePermission = ContextCompat.checkSelfPermission( this@ManualUserActivity, WRITE_EXTERNAL_STORAGE )
        // Constante de evaluacion para el checar permiso de lectura en el dispositivo
        val readStoragePermission = ContextCompat.checkSelfPermission( this@ManualUserActivity, READ_EXTERNAL_STORAGE )
        // Regresa un true si se cuentan con ambos permisos
        return writeStoragePermission == PackageManager.PERMISSION_GRANTED && readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    // Peticion de permisos, se hace un request permision, similar al startActivityResult
    private fun pedirPermiso(){
        ActivityCompat.requestPermissions(this@ManualUserActivity, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), codDownManu )
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == codDownManu) {
            // Se evalua si el arreglo de los permisos no esta vacio
            if (grantResults.isNotEmpty()) {
                // Se evalua si ambos permisos estan autorizados; READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Si los permisos fueron concedidos, se mostrara el mensaje de permisos concedidos
                    Toast.makeText(this@ManualUserActivity, "Permisos Concedidos...", Toast.LENGTH_SHORT).show()
                } else {
                    // En caso contrario, se mostrara el mensaje de permisos denegados y se terminara el activity permisions
                    Toast.makeText(this@ManualUserActivity, "Permisos Denegados...", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun mostrarManual(file: File) {
        pdfViewer.fromFile(file)
            .password(null)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onPageError { page, _ ->
                Toast.makeText(this@ManualUserActivity,"Ocurrio un error en la pagina: $page", Toast.LENGTH_LONG).show()
            }
            .load()
    }

    private fun descargarManual(url: String, rutaDir: String) {
        // Generar el archivo de forma local para buscarlo
        val manLocal = File(rutaDir, nombreArchivo)
        if(!manLocal.exists()){
            // Descargar el manual de firebase storage (otra vez)
            val obteManualInternet = PRDownloader.download(url,rutaDir,nombreArchivo).build()
            obteManualInternet.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    // Generar el archivo en la carpeta de descargas
                    File(rutaDir, nombreArchivo)
                    // Ocultar la barra de progreso al finalizar la carga del archivo
                    progressBar.isGone = true
                    // Mensaje de descarga completada con exito
                    Toast.makeText(this@ManualUserActivity, "El manual ha sido guardado satisfactoriamente en sus descargas", Toast.LENGTH_SHORT).show()
                    // Retornando al dashboard
                    Timer().schedule(1500) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            finish()
                        }
                    }
                }
                override fun onError(error: Error) {
                    Toast.makeText(this@ManualUserActivity,"Error en la obtencion del manual: $error",Toast.LENGTH_LONG).show()
                }
            })
        }else{
            Toast.makeText(this@ManualUserActivity,"Error: El dispositivo ya cuenta con el manual en sus descargas, favor de verificarlo",Toast.LENGTH_LONG).show()
            // Ocultar la barra de progreso otra vez
            progressBar.isGone = true
        }
    }
}