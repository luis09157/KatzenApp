package com.example.katzen.PDF

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.katzen.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ConvertPDF (activity: Activity) {
    val activity:Activity = activity
    companion object{
        val REQUEST_CODE = 1232
    }
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            }
        }
    }

    fun convertXmlToPdf() {
        // Inflate the XML layout file
        val view: View = LayoutInflater.from(activity).inflate(R.layout.fragment_autorizo_pdf, null)
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.getDisplay()?.getRealMetrics(displayMetrics)
        } else activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        )
        Log.d("mylog", "Width Now " + view.measuredWidth)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        // Create a new PdfDocument instance
        val document = PdfDocument()

        // Obtain the width and height of the view
        val viewWidth = view.getMeasuredWidth();
        val viewHeight = view.getMeasuredHeight();
        //val viewWidth = 1920
        //val viewHeight = 1280


        //Log.d("mylog", "Width: " + viewWidth);
        // Create a PageInfo object specifying the page attributes
        val pageInfo = PageInfo.Builder(viewWidth, viewHeight, 1).create()

        // Start a new page
        val page = document.startPage(pageInfo)

        // Get the Canvas object to draw on the page
        val canvas = page.canvas

        // Create a Paint object for styling the view
        val paint = Paint()
        paint.color = Color.WHITE

        // Draw the view on the canvas
        view.draw(canvas)

        // Finish the page
        document.finishPage(page)

        // Specify the path and filename of the output PDF file
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "exampleXML.pdf"
        val filePath = File(downloadsDir, fileName)
        try {
            // Save the document to a file
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            // PDF conversion successful
            Toast.makeText(activity, "XML to PDF Conversion Successful", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            // Error occurred while converting to PDF
        }
    }
}