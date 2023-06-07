package com.example.arosaje

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
class MainActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val resultArray = if (data != null) {
                    val uri = data.data
                    if (uri != null) {
                        arrayOf(uri)
                    } else {
                        emptyArray()
                    }
                } else {
                    emptyArray()
                }
                fileUploadCallback?.onReceiveValue(resultArray)
                fileUploadCallback = null
            } else {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ┌─────────────────────────────┐
        // │          WebView            │
        // └─────────────────────────────┘
        var webView: WebView = findViewById<WebView>(R.id.webview)
        val url_home = getString(R.string.URL_HOME)

        webView.settings.javaScriptEnabled = true
        webView?.clearCache(true)
        webView.loadUrl(url_home)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                fileUploadCallback = filePathCallback

                openFileChooser()

                return true
            }
        }

        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.startsWith("tel:")) {
                        val tel = Intent (Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(tel);
                        return true;
                    } else if(url.contains("mailto:")) {
                        if (view != null) {
                            view.getContext().startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        };
                        return true;
                    } else {
                        if (view != null) {
                            view.loadUrl(url)
                        };
                        return true;
                    }
                }
                webView.settings.userAgentString="app_mobile_v1"
                webView.loadUrl(url.toString());

                return true
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"

        val chooserIntent = Intent.createChooser(intent, "Select File")
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (fileUploadCallback == null) {
                return
            }
            val result = if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                if (uri != null) {
                    arrayOf(uri)
                } else {
                    emptyArray()
                }
            } else {
                emptyArray()
            }
            fileUploadCallback?.onReceiveValue(result)
            fileUploadCallback = null
        }
    }
}