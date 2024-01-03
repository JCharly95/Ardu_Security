package com.ardusec.ardu_security

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ahmer.pdfium.PdfDocument
import com.ahmer.pdfviewer.PDFView
import com.ahmer.pdfviewer.listener.OnLoadCompleteListener
import com.ahmer.pdfviewer.listener.OnPageChangeListener
import com.ahmer.pdfviewer.scroll.DefaultScrollHandle


class ManualUserActivity : AppCompatActivity(), OnPageChangeListener,OnLoadCompleteListener {
    private val TAG = ManualUserActivity::class.java.simpleName
    val SAMPLE_FILE = "pago.pdf"
    var pdfView: PDFView? = null
    var pageNumber = 0
    var pdfFileName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_user)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_700)))

        title = "Manual Digital de Usuario"

        pdfView = findViewById(R.id.webManSis);
        displayFromAsset(SAMPLE_FILE);

        /*val webView = WebView(this@ManualUserActivity)
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        Toast.makeText(this,"....hello....", Toast.LENGTH_SHORT).show()
        webView.loadUrl("https://profefily.com/wp-content/uploads/2019/10/F%C3%ADsica-conceptos-y-aplicaciones-Tippens.pdf");*/

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

    private fun displayFromAsset(assetFileName: String) {
        pdfFileName = assetFileName
        pdfView!!.fromAsset(SAMPLE_FILE)
            .defaultPage(pageNumber)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        title = String.format("%s %s / %s", pdfFileName, page + 1, pageCount)
    }

    override fun loadComplete(nbPages: Int) {
        val meta: PdfDocument.Meta? = pdfView!!.getDocumentMeta()
        printBookmarksTree(pdfView!!.getTableOfContents(), "-")
    }

    fun printBookmarksTree(tree: List<PdfDocument.Bookmark?>, sep: String) {
        for (b in tree) {
            Log.e(TAG, java.lang.String.format("%s %s, p %d", sep, b!!.title, b.pageIdx))
            if (b.hasChildren()) {
                printBookmarksTree(b.children, "$sep-")
            }
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