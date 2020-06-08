package com.leadmarkt.leadmarkt
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    var userName : ArrayList<String> = ArrayList()
    var userComment : ArrayList<String> = ArrayList()
    var userDate : ArrayList<String> = ArrayList()
    var adapter : ProductAdapter? = null
    var i = 0
    var a = 0
    var c = 0
    var vis = 0
    var rm = 0
    var tmp : Float = 0F
    var tmp2 : Float = 0F
    var tmp3 : Float = 0F
    private lateinit var deleteIcon: Drawable
    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF3C30"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        // actionbar
        val actionbar = supportActionBar
        actionbar!!.title = "LeadMarkt"
        actionbar.setDisplayHomeAsUpEnabled(true)

        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        getDataFromFirestore()

        favButton.setAnimation("saved.json")
        notFound.setAnimation("notfound.json")
        notFound.visibility = INVISIBLE

        //get intent
        val intent2 = intent
        val textviewtext = intent2.getStringExtra("barcodenumbertext")
        //print(textviewtext)
        if(textviewtext != null) {
            barcodeTextView.text = textviewtext.toString()
        }

        //RecyclerView
        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = ProductAdapter(userName,userComment,userDate)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.favs){
            val intent = Intent(applicationContext,
                SavedProductActivity::class.java)
            startActivity(intent)
            finish()
        }
        if (item.itemId == R.id.logout){
            auth.signOut()
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            rm = 1
            (adapter as ProductAdapter).removeItem(viewHolder)
            removeItemm(viewHolder)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
            val iconMargin2 = (itemView.height - deleteIcon.intrinsicHeight) / 2

            if (dX > 0){
                swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                deleteIcon.setBounds(itemView.left + iconMargin2 , itemView.top + iconMargin2, itemView.left + iconMargin2 + deleteIcon.intrinsicWidth , itemView.bottom - iconMargin2)
                swipeBackground.draw(c)
            }else{
                swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth , itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)
                swipeBackground.draw(c)
            }
            c.save()
            // *******icon rowun üstüne geliyordu *********
            if(dX > 0) {
                c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                deleteIcon.draw(c)
            }else{
                c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                deleteIcon.draw(c)
                //*********************************************
            }
            c.restore()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    fun removeItemm(viewHolder: RecyclerView.ViewHolder) {
        val authEmail = auth.currentUser!!.email.toString()
        db.collection("Comment").addSnapshotListener { snapshot, exception ->
            if (exception != null) {
            } else {
                if (snapshot != null) {
                    if (!snapshot.isEmpty) {
                        val documents = snapshot.documents
                        for (document in documents) {
                            val barcode = document.get("barcode") as? String
                            if (barcode.toString() == barcodeTextView.text.toString()) {
                                val email = document.get("email") as? String
                                val dPath = document.id
                                if (authEmail == email && rm ==1) {
                                    db.collection("Comment").document(dPath)
                                        .delete()
                                    rm = 0
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    //Back Press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onBackPressed() {
        startActivity(Intent(this@ProductActivity, ScanActivity::class.java))
    }

    //COMMENT
    fun addComment (view: View){
        //   val comment = commentEditText.text.toString().trim()
        val usermail = auth.currentUser?.email.toString()

        db.collection("Users").addSnapshotListener { snapshot, exception ->
            if (exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
            }else{
                if (snapshot!=null){
                    if(!snapshot.isEmpty){
                        val documentsUser = snapshot.documents
                        val currentMail = auth.currentUser?.email.toString()

                        for(documentUser in documentsUser ){

                            val emailFB = documentUser.get("email") as? String

                            if (emailFB == currentMail) {
                                val commentMap = hashMapOf<String, Any>()
                                val name = documentUser.get("name") as? String
                                val surname = documentUser.get("surname") as? String
                                val comment = commentEditText.text.toString().trim()

                                if (commentEditText.text.toString() != "" ) {
                                    if (name != null) {
                                        commentMap.put("name", name)
                                    }
                                    if (surname != null) {
                                        commentMap.put("surname", surname)
                                    }
                                    if (comment != "") {
                                        commentMap.put("comment", comment)
                                    }
                                    commentMap.put("comment", comment)
                                    commentMap.put("barcode", barcodeTextView.text.toString())
                                    commentMap.put("date", Timestamp.now())
                                    commentMap.put("email",currentMail)

                                    //CommentAuth
                                    val authEmail = auth.currentUser!!.email.toString()
                                    db.collection("Comment").addSnapshotListener { snapshot, exception ->
                                        if (exception != null){
                                            Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                                        }else{
                                            if (snapshot!=null){
                                                if(!snapshot.isEmpty){

                                                    val documents = snapshot.documents
                                                    for(document in documents ) {
                                                        val email = document.get("email") as? String
                                                        val barcode = document.get("barcode") as? String
                                                        if (barcode == barcodeTextView.text.toString()){
                                                        if(email == authEmail){
                                                            c = 1
                                                        }}
                                                    }}}}}
                                    //CommentAuth
                                    if (c==0){
                                    db.collection("Comment").add(commentMap).addOnCompleteListener { task ->
                                        if (task.isComplete && task.isSuccessful) {
                                            //  println("auth current user $auth.currentUser!!.email")
                                            commentEditText.text = null
                                            val imm =
                                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                            imm.hideSoftInputFromWindow(
                                                currentFocus!!.windowToken,
                                                0
                                            )
                                            getDataFromFirestore()
                                        }
                                    }.addOnFailureListener{exception ->
                                        Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                                    }}
                                    else{
                                        Toast.makeText(this@ProductActivity, "Bu ürünü daha önce yorumladınız!", Toast.LENGTH_SHORT).show()
                                    }
                                }}}}}}}}

    fun getDataFromFirestore() {
        val authEmail = auth.currentUser!!.email.toString()

        db.collection("Barcode").addSnapshotListener { snapshot, exception ->
            if (exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
            }else{
                if (snapshot!=null){
                    if(!snapshot.isEmpty){
                        val documents = snapshot.documents

                        for(document in documents ){

                            val barcode = document.get("barcode") as? String
                            val price = document.get("price") as? String
                            val name = document.get("title") as? String
                            val img= document.get("image") as? String

                            if (barcode.toString() == barcodeTextView.text.toString()) {
                                vis = 1
                                rBar.visibility = VISIBLE
                                button.visibility = VISIBLE
                                favButton.visibility = VISIBLE

                                Picasso.get().load(img).into(imageView)
                                titleTextView.text = name.toString()
                                priceTextView.text = price.toString()

                                //Rate auth
                                db.collection("Rating").addSnapshotListener { snapshot, exception ->
                                    if (exception != null){
                                        Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                                    }else{
                                        if (snapshot!=null){
                                            if(!snapshot.isEmpty){

                                                val documents = snapshot.documents
                                                for(document in documents ) {
                                                    val email = document.get("email") as? String
                                                    if(email == authEmail){
                                                        a = 1
                                                    }
                                                }}}}}

                                val rBar = findViewById<RatingBar>(R.id.rBar)
                                if (rBar != null) {
                                    val button = findViewById<Button>(R.id.button)
                                    button?.setOnClickListener {
                                        var ratePoint = rBar.rating

                                        //Refresh Page
                                        val ProductActivity = intent
                                        finish()
                                        startActivity(ProductActivity)

                                        val RatingMap = hashMapOf<String, Any>()
                                        val rating = ratePoint
                                        val email = auth.currentUser!!.email.toString()
                                        val barcode = barcodeTextView.text.toString()

                                        RatingMap.put("rate", rating)
                                        RatingMap.put("email", email)
                                        RatingMap.put("barcode", barcode)


                                        if (a == 0){
                                            db.collection("Rating").add(RatingMap).addOnCompleteListener { task ->

                                            }.addOnFailureListener { exception ->
                                                Toast.makeText(
                                                    applicationContext,
                                                    exception.localizedMessage?.toString(),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }}
                                        else{
                                            Toast.makeText(this@ProductActivity, "Bu ürünü daha önce puanladınız!", Toast.LENGTH_SHORT).show()
                                        }
                                    }}

                                db.collection("Rating").addSnapshotListener { snapshot, exception ->
                                    if (exception != null){
                                        Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                                    }else{
                                        if (snapshot!=null){
                                            if(!snapshot.isEmpty){
                                                val documents = snapshot.documents
                                                i = 0
                                                for(document in documents ) {
                                                    i ++
                                                    //val barcode = document.get("barcode") as? String
                                                    val email = document.get("email") as? String
                                                    val rate = document.get("rate") as Number

                                                    tmp2 = tmp3
                                                    tmp= rate.toFloat()
                                                    tmp3 = tmp + tmp2
                                                }
                                            }
                                            val sum = tmp3/i
                                            rBar.rating = sum
                                        }}}

                                //Comment
                                db.collection("Comment").orderBy("date",
                                    Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
                                    if (exception != null){
                                        Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                                    }else {
                                        if (snapshot!=null)
                                            if(!snapshot.isEmpty){
                                                userName.clear()
                                                userComment.clear()
                                                userDate.clear()

                                                val documents = snapshot.documents
                                                for(document in documents ){


                                                    val name = document.get("name") as? String
                                                    val surname = document.get("surname") as? String
                                                    val comment = document.get("comment") as? String
                                                    val barcodeNo = document.get("barcode") as? String
                                                    val timestamp = document.get("date") as Timestamp
                                                    val date = timestamp.toDate()

                                                    if (barcodeNo.toString() == barcodeTextView.text.toString() ) {

                                                        if (name != null) {
                                                            userName.add(name +" "+ surname)
                                                        }

                                                        if (comment != null) {
                                                            userComment.add(comment)
                                                        }
                                                        userDate.add(date.toString())
                                                        adapter!!.notifyDataSetChanged()
                                                    }
                                                }}}}
                            }
                           else{
                                var vis = 0
                            }
                        }
                        if (vis == 0){
                        rBar.visibility = android.view.View.INVISIBLE
                        button.visibility = android.view.View.INVISIBLE
                        favButton.visibility = android.view.View.INVISIBLE
                        linearLayout5.visibility = android.view.View.INVISIBLE
                        nfText.text = "Ürün bulunamadı.\n Yetkililere bildirilmiştir.\n En kısa sürede eklenecektir."
                            val notFoundMap = hashMapOf<String, Any>()
                            notFoundMap.put("barcode", barcodeTextView.text.toString())

                            db.collection("BarcodeNotFound").add(notFoundMap).addOnCompleteListener { task ->
                                if (task.isComplete && task.isSuccessful) {
                                    notFound.visibility = VISIBLE
                                    notFound.playAnimation()
                                }
                            }.addOnFailureListener{exception ->
                                Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
                            }
                        }
                    }}}}}

    //Adding current item to clients favorite list
    fun addFav(view: View) {
        val authMail = auth.currentUser?.email.toString()
        val map = hashMapOf<String, String>()

        // Barcode verisi alma
        db.collection("Barcode").addSnapshotListener { snapshot, exception ->
            if (exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage?.toString(),Toast.LENGTH_LONG).show()
            }else{
                if (snapshot!=null){
                    if(!snapshot.isEmpty){
                        val documents = snapshot.documents

                        for(document in documents ){

                            val barcode = document.get("barcode") as String
                            val img= document.get("image") as String

                            if (barcode== barcodeTextView.text.toString()) {
                                map.put("image", img.toString())}}
                        // Barcode verisi alma//
                        map.put("email", authMail)
                        map.put("barcode", barcodeTextView.text.toString())
                        map.put("title", titleTextView.text.toString())
                        map.put("price", priceTextView.text.toString())

                        db.collection("Favorites").add(map)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    favButton.playAnimation()
                                    Toast.makeText(this@ProductActivity, "Ürün başarıyla kaydedildi.", Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    applicationContext,
                                    exception.localizedMessage?.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }}}}}}