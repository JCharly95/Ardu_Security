package com.ardusec.ardu_security

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Telephony.Mms.Part.FILENAME
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class ManualUserActivity : AppCompatActivity() {
    private lateinit var pdfReader: ImageView
    private lateinit var barraCarga: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        title = "Manual Digital de Usuario"

        /*// Preparacion de WebView para lectura del manual PDF
        pdfReader = findViewById(R.id.webManSis)
        barraCarga = findViewById(R.id.barChargeMan)

        barraCarga.visibility = View.GONE

        val input: InputStream = this@ManualUserActivity.assets.open("pago.pdf")
        val file: File = File(this@ManualUserActivity.cacheDir, "pago.pdf")
        val output: FileOutputStream = FileOutputStream(file) // where 'file' comes from :


        // Create the page renderer for the PDF document.
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)

// Open the page to be rendered.
        val page = pdfRenderer.openPage(1)

// Render the page to the bitmap.
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
// Use the rendered bitmap.
        pdfReader.setImageBitmap(bitmap)

// Close the page when you are done with it.
        page.close()
// Close the `PdfRenderer` when you are done with it.
        pdfRenderer.close()*/
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