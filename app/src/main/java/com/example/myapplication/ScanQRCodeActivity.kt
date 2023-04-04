package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ScanQRCodeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var scanBtn: Button
    private lateinit var messageText: TextView
    private lateinit var messageFormat: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qrcode)

        // referencing and initializing
        // the button and textviews
        scanBtn = findViewById(R.id.scanBtn)
        messageText = findViewById(R.id.textContent)
        messageFormat = findViewById(R.id.textFormat)
        sharedPref = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)

        // adding listener to the button
        scanBtn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        // we need to create the object
        // of IntentIntegrator class
        // which is the class of QR library
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setPrompt("Scan a barcode or QR Code")
        intentIntegrator.setOrientationLocked(true)
        intentIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                //messageText.text = intentResult.contents
                messageFormat.text = intentResult.formatName
                val parts = intentResult.contents.split("-")
                val mnemonic = parts[0]
                val collectionId = parts[1]
                val senderAddress = parts[2]
                messageText.text = "mnemonic : "+ mnemonic + " collectionId : "+ collectionId + " sender : " + senderAddress
                val url = " https://8947-105-51-4-201.eu.ngrok.io/transfer-token"
                val my_address = sharedPref.getString("my_address", "")
               // val my_mnemonic = sharedPref.getString("my_mnemonic", "")
            //    val collection_id = sharedPref.getString("collection_id", "")
           //     val sender_address = sharedPref.getString("sender_address", "")
               // https://51f8-105-51-4-201.in.ngrok.io

                val task = PostAsyncTask(url, my_address, mnemonic,collectionId,senderAddress)
                task.execute()

               // val task = GetAsyncTask()



            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private inner class PostAsyncTask(private val urlString: String, private val my_address: String?, private val mnemonic: String,  private val collectionId: String, private  val senderAddress:String) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg voids: Void): String {
            var result = ""

            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json")

                val jsonBody = JSONObject()
                jsonBody.put("my_address", my_address)
                jsonBody.put("mnemonic", mnemonic)
                jsonBody.put("collection_id", collectionId)
                jsonBody.put("sender_address", senderAddress)

                val outputStreamWriter = OutputStreamWriter(urlConnection.outputStream)
                outputStreamWriter.write(jsonBody.toString())
                outputStreamWriter.flush()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    result = input.readText()
                    Log.d("TAG", result)
                } else {
                    Log.e("PostAsyncTask", "Error making POST request, response code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("PostAsyncTask", "Error making POST request: ${e.message}")
            }

            return result
        }

        override fun onPostExecute(result: String) {
            // Use the result here
            Log.d("PostAsyncTask", "Result: $result")
        }
    }




}


