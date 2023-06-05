package com.example.arosaje

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ┌─────────────────────────────┐
        // │          WebView            │
        // └─────────────────────────────┘
        var webView: WebView = findViewById<WebView>(R.id.webview)



        val url = getString(R.string.URL_LOGIN)
        var webChromeClient: WebChromeClient

        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        webView.webChromeClient = object : WebChromeClient() {}
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                webView.settings.userAgentString="app_mobile_v1"
                webView.loadUrl(url.toString());
                return true
            }
        }
    }

    // ┌─────────────────────────────┐
    // │          WebView            │
    // └─────────────────────────────┘
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var webView: WebView = findViewById<WebView>(R.id.webview)

        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            webView.goBack()
            return true;
        }

        return super.onKeyDown(keyCode, event)
    }


}