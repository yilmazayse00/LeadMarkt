package com.leadmarkt.leadmarkt

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.passwordText
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_scan.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        this.getSupportActionBar()?.hide();
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    class NetworkTask(var activity: RegisterActivity) : AsyncTask<Void, Void, Void>(){
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

    fun signUpClicked(view : View) {
        NetworkTask(this).execute()
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        val name = nameText.text.toString().trim()
        val surname = surnameText.text.toString().trim()

        if(email != "" && password != ""){

            val usersMap = hashMapOf<String, Any>()
            usersMap.put("name",name)
            usersMap.put("surname",surname)
            usersMap.put("email",email)

            //Veritabanına kaydediyor
            db.collection("Users").add(usersMap).addOnCompleteListener { task ->
                if (task.isComplete && task.isSuccessful) {
                  //  println("auth current user $auth.currentUser!!.email")
                }
            }.addOnFailureListener{exception ->
                Toast.makeText(applicationContext,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
            }

            //Veritabanından alınan verileri getiriyor
            db.collection("Users").addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        applicationContext,
                        exception.localizedMessage?.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (snapshot != null) {
                        if (!snapshot.isEmpty) {

                            val documents = snapshot.documents
                            for (document in documents) {

                                if (auth.currentUser?.email == email) {
                                }
                                else{
                                }
                            }
                        }
                    }
                }
            }

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val intent = Intent(applicationContext,ScanActivity::class.java)
                    startActivity(intent)
                  //  finish()
                }

            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.localizedMessage?.toString(), Toast.LENGTH_LONG).show()

            }}
        else{
            Toast.makeText(applicationContext,"Lütfen Email adresi ve şifre alanlarını doldurun!",
                Toast.LENGTH_LONG).show()
        }
    }
}
