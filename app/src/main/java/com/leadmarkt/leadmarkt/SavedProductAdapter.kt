package com.leadmarkt.leadmarkt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class SavedProductAdapter(private val favTitleArray: ArrayList<String>,
                          private val favPriceArray: ArrayList<String>,
                          private val favImageArray: ArrayList<String>):
    RecyclerView.Adapter<SavedProductAdapter.FavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.saved_product_recycler_view_row,parent,false)

        return FavViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favTitleArray.size
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.recyclerFavTitle?.text = favTitleArray[position]
        holder.recyclerFavPrice?.text = favPriceArray[position]
        Picasso.get().load(favImageArray[position]).into(holder.recyclerFavImage)
    }

    class FavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var recyclerFavTitle: TextView? = null
        var recyclerFavPrice: TextView? = null
        var recyclerFavImage: ImageView? = null

        init {
            recyclerFavTitle = view.findViewById(R.id.recyclerFavTitle)
            recyclerFavPrice = view.findViewById(R.id.recyclerFavPrice)
            recyclerFavImage = view.findViewById(R.id.recyclerFavImage)

            itemView.setOnClickListener {v: View ->
                val position:Int = adapterPosition
            }
        }
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        favTitleArray.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
    }
}