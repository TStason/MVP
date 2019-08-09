package com.example.homeworkcats_1.RecyclerAdapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.MainActivity
import com.example.homeworkcats_1.R
import com.example.homeworkcats_1.VIewHolders.CatFactViewHolder

class CatFactRecyclerAdapter(val dataList: ArrayList<CatFact>,
                             val onClickListener: (CatFact)-> Unit?
                            ): RecyclerView.Adapter<CatFactViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CatFactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CatFactViewHolder(inflater.inflate(R.layout.cat_fact_holder, parent, false))
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: CatFactViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, onClickListener)
    }

}