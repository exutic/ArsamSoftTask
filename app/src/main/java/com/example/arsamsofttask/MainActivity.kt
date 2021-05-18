package com.example.arsamsofttask

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafeBrowsingThreat.TYPE_POTENTIALLY_HARMFUL_APPLICATION
import com.google.android.gms.safetynet.SafeBrowsingThreat.TYPE_SOCIAL_ENGINEERING
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class MainActivity : AppCompatActivity() {


    private lateinit var webView: WebView

    //AIzaSyBzHhSq5-vqi2mADAYacw3nC9EUz5olvrg
    var urlGoogle = "https://www.google.com"
    val SAFE_BROWSING_API_KEY = "AIzaSyBzHhSq5-vqi2mADAYacw3nC9EUz5olvrg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        SafetyNet.getClient(this).lookupUri(
            urlGoogle,
            SAFE_BROWSING_API_KEY,
            4,
            5
        )
            .addOnSuccessListener(this) { sbResponse ->
                // Indicates communication with the service was successful.
                // Identify any detected threats.
                if (sbResponse.detectedThreats.isEmpty()) {
                    Toast.makeText(applicationContext, "No threats found.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Threats found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { e: Exception ->
                if (e is ApiException) {
                    // An error with the Google Play Services API contains some
                    // additional details.
                    Log.d("arsamSoftTask", "Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
                    Toast.makeText(
                        applicationContext,
                        "\"Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Note: If the status code, s.statusCode,
                    // is SafetyNetstatusCode.SAFE_BROWSING_API_NOT_INITIALIZED,
                    // you need to call initSafeBrowsing(). It means either you
                    // haven't called initSafeBrowsing() before or that it needs
                    // to be called again due to an internal error.
                } else {
                    // A different, unknown type of error occurred.
                    Log.d("arsamSoftTask", "Error: ${e.message}")
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        val btnClick: Button = findViewById(R.id.btnClick)
        btnClick.setOnClickListener(View.OnClickListener {
            Toast.makeText(applicationContext, "I told you", Toast.LENGTH_SHORT).show()
        })

        openGoogle()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun openGoogle()
    {
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        webView.loadUrl(urlGoogle)
    }

    private fun turnOnTask() {
        Tasks.await(SafetyNet.getClient(applicationContext).initSafeBrowsing())
    }

    private suspend fun resumeTask() {
        delay(1000)
        CoroutineScope(Dispatchers.IO).launch {
            turnOnTask()
        }
    }
    override fun onResume() {
        super.onResume()
        CoroutineScope(IO).launch {
            resumeTask()
        }
    }


    private fun shutDown() {
        SafetyNet.getClient(this).shutdownSafeBrowsing()
    }
    private fun onPauseTask() {
        CoroutineScope(IO).launch {
            shutDown()
        }
    }
    override fun onPause() {
        super.onPause()
        CoroutineScope(IO).launch {
            onPauseTask()
        }
    }
}



