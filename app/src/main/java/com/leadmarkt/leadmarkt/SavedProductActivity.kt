package com.leadmarkt.leadmarkt

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_saved_product.recycler_view

class SavedProductActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    var favTitle : ArrayList<String> = ArrayList()
    var favPrice : ArrayList<String> = ArrayList()
    var favImage : ArrayList<String> = ArrayList()
    var adapter : SavedProductAdapter? = null
    var rm = 0
    var pos = 0

    private lateinit var deleteIcon: Drawable
    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF3C30"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_product)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        generateFavList()

        // actionbar
        val actionbar = supportActionBar
        actionbar!!.title = "Kaydedilenler"
        actionbar.setDisplayHomeAsUpEnabled(true)

        var layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager

        adapter = SavedProductAdapter(favTitle,favPrice,favImage)
        recycler_view.adapter = adapter

        //swipeToDelete
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view)
    }

    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
    {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            rm = 1
            pos = viewHolder.adapterPosition
          // Toast.makeText(applicationContext,pos.toString(), Toast.LENGTH_LONG).show()
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

    private fun removeItemm(viewHolder: RecyclerView.ViewHolder) {
        val authEmail = auth.currentUser!!.email.toString()

        db.collection("Favorites").addSnapshotListener { snapshot, exception ->
            if (exception != null) {
            } else {
                if (snapshot != null) {
                    if (!snapshot.isEmpty) {
                        val documents = snapshot.documents
                        for (document in documents) {
                            val title = document.get("title") as String
                            val email = document.get("email") as String

                            if (email == authEmail && pos != favTitle.size) {

                                if (title == favTitle[pos]) {
                                    val dPath = document.id
                                    if (rm == 1) {
                                        db.collection("Favorites").document(dPath)
                                            .delete()
                                            (adapter as SavedProductAdapter).removeItem(viewHolder)
                                        rm = 0
                                       // Toast.makeText(applicationContext,favTitle[0], Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onBackPressed() {
        startActivity(Intent(this@SavedProductActivity, ScanActivity::class.java))
    }

    private fun generateFavList() {
        db.collection("Favorites").addSnapshotListener{ snapshot, exception ->
            if (exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage?.toString(), Toast.LENGTH_LONG).show()
            }
            else{
                if (snapshot!=null){
                    if(!snapshot.isEmpty){
                        favTitle.clear()
                        favPrice.clear()
                        favImage.clear()
                        val documents = snapshot.documents
                        for(document in documents ){

                            val email = document.get("email") as String
                            //val barcode = document.get("barcode") as String

                            if (email == auth.currentUser!!.email)
                            {
                                val title = document.get("title") as String
                                val price = document.get("price") as String
                                val image = document.get("image") as? String

                                favTitle.add(title)
                                favPrice.add(price)
                                if (image != null) {
                                    favImage.add(image)
                                }
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }
}
