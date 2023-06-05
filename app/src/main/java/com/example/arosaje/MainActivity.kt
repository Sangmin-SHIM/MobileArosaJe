package com.example.arosaje

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import android.Manifest

class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1
    val CAMERA_REQUEST_CODE = 101
    private var mCameraPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Crée un nom de fichier image unique
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
        // Sauvegarde un fichier: chemin pour l'utilisation avec ACTION_VIEW intents
        mCameraPhotoPath = image.absolutePath
        return image
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById<WebView>(R.id.webview)
        val url = getString(R.string.URL_LOGIN)
        webView?.settings?.javaScriptEnabled = true
        webView?.clearCache(true)
        webView?.loadUrl(url)
        webView?.webChromeClient = object : WebChromeClient() {
            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, so request it
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), 101)
                }
                mUploadMessage = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent?.resolveActivity(packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                    } catch (ex: IOException) {
                        Log.e(TAG, "Unable to create Image File", ex)
                    }

                    if (photoFile != null) {
                        mCameraPhotoPath = Uri.fromFile(photoFile).toString()
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }

                }

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "image/*"
                val intentArray: Array<Intent?>
                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
                return true
            }
        }

        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                webView?.settings?.userAgentString = "app_mobile_v1"
                webView?.loadUrl(url.toString())
                return true
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView?.canGoBack() == true) {
            webView?.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage == null) {
                return // Si uploadMessage est null, on ne fait rien
            }

            var results: Array<Uri>? = null
            // Vérifie si la réponse contient une data
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    // Si l'intention n'est pas nulle, récupère l'Uri de la photo
                    val dataString = intent.dataString
                    val clipData = intent.clipData
                    if (clipData != null) {
                        results = Array(clipData.itemCount) {
                            clipData.getItemAt(it).uri
                        }
                    }
                    // Si dataString pourrait être rasé en haut du code, alors il contient l'Uri de la photo
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                } else if (intent == null || resultCode != RESULT_OK) {
                    // L'intention est nulle, ce qui signifie probablement que la photo a été prise avec la caméra
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                }
            }

            mUploadMessage?.onReceiveValue(results)
            mUploadMessage = null
        }
    }
}