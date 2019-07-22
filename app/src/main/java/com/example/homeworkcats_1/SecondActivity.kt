package com.example.homeworkcats_1

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.homeworkcats_1.DataClasses.CatFact

class SecondActivity: AppCompatActivity() {

    private var catFact: CatFact? = null
    private lateinit var catImage: ImageView
    private lateinit var catFactOwner: TextView
    private lateinit var catFactText: TextView
    private var isFirst: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        catImage = findViewById(R.id.catImage)
        catFactOwner = findViewById(R.id.catFactOwner)
        catFactText = findViewById(R.id.catFactText)
        savedInstanceState?.let {
            catFact = it.getParcelable("CatFact")
            isFirst = it.getBoolean("isFirst")
        }
        if (isFirst){
            catFact = intent.getParcelableExtra<CatFact>("CatFact")
            isFirst = false
        }
        catFact?.let {
            catFactText.text = it.text
            catFactOwner.text = "${it.user?.name?.first} ${it.user?.name?.last}"
            Glide.with(this)
                .load(it.imgUrl)
                .into(catImage)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("APPSecondActivity", "onStart() second activity")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable("CatFact", catFact)
        outState?.putBoolean("isFirst", isFirst)
    }
}