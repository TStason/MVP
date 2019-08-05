package com.example.homeworkcats_1

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.Presentors.MainPresentor
import com.example.homeworkcats_1.RecyclerAdapters.CatFactRecyclerAdapter

class MainActivity : AppCompatActivity() {

    private val TAG = "APPActivity"

    private lateinit var presentor: MainPresentor
    lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    lateinit var customAdapter: CatFactRecyclerAdapter
    val dataList: ArrayList<CatFact> = arrayListOf()
    var isFirst: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentor = MainPresentor(this)
        savedInstanceState?.let {
            Log.e(TAG, "have savedInstanceState")
            isFirst = it.getBoolean("isFirst", true)
            dataList.addAll(it.getParcelableArrayList("dataList"))
        }
        setContentView(R.layout.activity_main)
        swipe = findViewById(R.id.swipe)
        swipe.setOnRefreshListener {
            presentor.onRefresh()
        }
        recycler = findViewById(R.id.recycler)
        customAdapter = CatFactRecyclerAdapter(dataList, presentor.onClickCard())
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customAdapter
        }
        Log.e(TAG, "Recycler size: ${recycler.adapter?.itemCount}")
    }

    override fun onStart() {
        super.onStart()
        presentor.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            it.putParcelableArrayList("dataList", dataList)
            it.putBoolean("isFirst", isFirst)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presentor.onDestroy()
    }

}
