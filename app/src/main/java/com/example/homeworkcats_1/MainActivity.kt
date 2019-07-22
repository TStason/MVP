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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var presentor: MainPresentor
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var customAdapter: CatFactRecyclerAdapter
    private val dataList: ArrayList<CatFact> = arrayListOf()
    var isFirst: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presentor = MainPresentor(this.applicationContext)

        savedInstanceState?.let {
            Log.e("APPActivity", "have savedInstanceState")
            isFirst = it.getBoolean("isFirst", true)
            dataList.addAll(it.getParcelableArrayList("dataList"))
        }

        setContentView(R.layout.activity_main)
        swipe = findViewById(R.id.swipe)
        swipe.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                dataList.addAll(presentor.onRefresh())
                swipe.isRefreshing = false
            }
        }
        recycler = findViewById(R.id.recycler)
        customAdapter = CatFactRecyclerAdapter(dataList, presentor.setOnClickCard())
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customAdapter
        }
        Log.e("APPActivity", "Recycler size: ${recycler.adapter?.itemCount}")

    }

    override fun onStart() {
        super.onStart()
        if (isFirst || dataList.isNullOrEmpty()){
            GlobalScope.launch(Dispatchers.Main) {
                dataList.clear()
                dataList.addAll(presentor.getFacts())
                Log.e("APPActivity", "data receive ${dataList.size}")
                customAdapter.notifyDataSetChanged()
            }
            isFirst = false
        }
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
