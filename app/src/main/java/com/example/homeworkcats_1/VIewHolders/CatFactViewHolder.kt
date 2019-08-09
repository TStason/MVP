package com.example.homeworkcats_1.VIewHolders

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.R
import org.w3c.dom.Text

class CatFactViewHolder(v: View): RecyclerView.ViewHolder(v)
{
    lateinit var catFact: CatFact
    private var catImage: ImageView
    private var catFactText: TextView
    private var catFactOwner: TextView
    private var catFactDateUpdate: TextView
    init {
        catImage = itemView.findViewById(R.id.catImage)
        catFactText = itemView.findViewById(R.id.catFactText)
        catFactOwner = itemView.findViewById(R.id.catFactOwner)
        catFactDateUpdate = itemView.findViewById(R.id.catDateUpdate)
    }
    fun bind(data: CatFact, onClickListener: (CatFact)->Unit?){
        catFact = data
        catFactText.text = catFact.text
        catFactOwner.text = "${catFact.user?.name?.first} ${catFact.user?.name?.last}"
        catFactDateUpdate.text = catFact.updatedAt
        Glide.with(itemView.context)
            .load(catFact.imgUrl)
            .override(300,300)
            .into(catImage)
        itemView.setOnClickListener{
            onClickListener(catFact)
        }
    }

}