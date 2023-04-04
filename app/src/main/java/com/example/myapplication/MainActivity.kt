package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var addressTextView: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scanQRCodeButton: Button = findViewById(R.id.scanQRCode)
        val createAccountButton: Button = findViewById(R.id.createAccountButton)

         sharedPref = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
         editor = sharedPref.edit()
        val my_address = sharedPref.getString("my_address", "")
         addressTextView = findViewById<TextView>(R.id.addressTextView)
addressTextView.text = my_address;

        scanQRCodeButton.setOnClickListener {
            // Code to be executed when the button is clicked
            val intent = Intent(this, ScanQRCodeActivity::class.java)
            startActivity(intent)
        }

        createAccountButton.setOnClickListener {
            // Code to be executed when the button is clicked

            val url = " https://8947-105-51-4-201.eu.ngrok.io/create-account"
            val task = GetAsyncTask()
            task.execute(url)
        }
    }

    private inner class GetAsyncTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String {
            val urlString = urls[0]
            var result = ""

            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection

                try {
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    result = input.readText()
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                Log.e("GetAsyncTask", "Error making GET request: ${e.message}")
            }

            return result
        }

        override fun onPostExecute(result: String) {
            // Use the result here
            Log.d("GetAsyncTask", "Result: $result")
            val json = JSONObject(result)

// Get the mnemonic and address from the JSON
            val mnemonic = json.getString("mnemonic")
            val address = json.getJSONObject("keyfile").getString("address")

// Log the results
            Log.d("JSON", "Mnemonic: $mnemonic")
            Log.d("JSON", "Address: $address")
            addressTextView.text = address
            editor.putString("my_address", address)
            editor.putString("my_mnemonic", mnemonic)
            editor.apply()



        }
    }

}