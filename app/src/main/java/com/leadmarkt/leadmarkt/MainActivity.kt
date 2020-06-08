package com.leadmarkt.leadmarkt

import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            this.getSupportActionBar()?.hide();

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(applicationContext,ScanActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    class NetworkTask(var activity: MainActivity) : AsyncTask<Void, Void, Void>(){
        var dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)
        override fun onPreExecute() {
            val view = activity.layoutInflater.inflate(R.layout.login_progress_bar,null)
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.show()
            super.onPreExecute()
        }
        override fun doInBackground(vararg params: Void?): Void? {
            Thread.sleep(3000)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            dialog.dismiss()
        }
    }

    fun signInClicked(view : View) {
        NetworkTask(this).execute()
        if(userEmailText.text.toString() != "" && passwordText.text.toString() != ""){
        auth.signInWithEmailAndPassword(userEmailText.text.toString().trim(),passwordText.text.toString().trim()).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                //Signed In
                Toast.makeText(applicationContext,"Welcome: ${auth.currentUser?.email.toString()}",Toast.LENGTH_LONG).show()
                val intent = Intent(applicationContext,ScanActivity::class.java)
                startActivity(intent)
              //  finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext,exception.localizedMessage.toString(), Toast.LENGTH_LONG).show()
        }

        }
        else{
            Toast.makeText(applicationContext,"Lütfen Email adresi ve şifre alanlarını doldurun!",Toast.LENGTH_LONG).show()
        }}

    fun nav_signUpClicked(view : View) {
                val intent = Intent(applicationContext,RegisterActivity::class.java)
                startActivity(intent)
               finish()
    }
}



