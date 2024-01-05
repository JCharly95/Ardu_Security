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


class ManualUserActivity : AppCompatActivity() {
    // Estableciendo los elementos de interaccion; Seccion Formulario de Seleccion
    private lateinit var pdfViewer: PDFView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnDescarga: Button

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
        PRDownloader.initialize(this@ManualUserActivity)

        // Preparacion de WebView para lectura del manual PDF
        progressBar.isGone = false
        val pdfUrl = "https://observatoriocultural.udgvirtual.udg.mx/repositorio/bitstream/handle/123456789/432/6+Folleto.pdf"
        val filename = "6+Folleto.pdf"

        downloadPdfFromInternet(pdfUrl, getRootDirPath(), filename)
    }

    private fun addListeners(){

    }

    private fun downloadPdfFromInternet(url: String, dirPath: String, fileName: String) {
        PRDownloader.download(url,dirPath,fileName).build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Toast.makeText(this@ManualUserActivity, "downloadComplete", Toast.LENGTH_LONG).show()
                    val downloadedFile = File(dirPath, fileName)
                    progressBar.isGone = true
                    showPdfFromFile(downloadedFile)
                }
                override fun onError(error: Error?) {
                    Toast.makeText(this@ManualUserActivity,"Error in downloading file: $error",Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun showPdfFromFile(file: File) {
        pdfViewer.fromFile(file)
            .password(null)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onPageError { page, _ ->
                Toast.makeText(this@ManualUserActivity,"Error at page: $page", Toast.LENGTH_LONG).show()
            }
            .load()
    }

    private fun getRootDirPath(): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = ContextCompat.getExternalFilesDirs(this@ManualUserActivity,null)[0]
            file.absolutePath
        } else {
            this@ManualUserActivity.filesDir.absolutePath
        }
    }

    /*@Throws(IOException::class)
    private fun openRenderer(context: Context) {
        // In this sample, we read a PDF from the assets directory.
        val file: File = File(context.cacheDir, "pago.pdf")
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            val asset: InputStream = context.assets.open("pago.pdf")
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            asset.close()
            output.close()
        }
        val mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            val mPdfRenderer = PdfRenderer(mFileDescriptor)
        }
    }*/
}