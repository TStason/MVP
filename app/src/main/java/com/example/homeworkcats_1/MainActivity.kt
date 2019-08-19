package com.example.homeworkcats_1

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.Presenters.MainPresenter
import com.example.homeworkcats_1.RecyclerAdapters.CatFactRecyclerAdapter
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), PresenterDelegate {

    private val TAG = "APPActivity"
    override lateinit var context: Context
    override var isFirst: Boolean = true

    private lateinit var presenter: MainPresenter
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var customAdapter: CatFactRecyclerAdapter
    private val dataList: ArrayList<CatFact> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.applicationContext
        attachPresenter()
        Log.e(TAG, "$presenter")
        savedInstanceState?.let {
            Log.e(TAG, "have savedInstanceState")
            isFirst = it.getBoolean("isFirst", true)
            dataList.addAll(it.getParcelableArrayList("dataList"))
        }
        setContentView(R.layout.activity_main)
        swipe = findViewById(R.id.swipe)
        swipe.setOnRefreshListener {
            presenter.onRefresh()
        }
        recycler = findViewById(R.id.recycler)
        customAdapter = CatFactRecyclerAdapter(dataList, presenter.onClickCard())
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customAdapter
        }
        Log.e(TAG, "Recycler size: ${recycler.adapter?.itemCount}")
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
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
        presenter.onDestroy()
    }

    private fun attachPresenter() {
        presenter = (lastCustomNonConfigurationInstance as? MainPresenter)?.apply {
            this.attachView(WeakReference(this@MainActivity))
        } ?: MainPresenter(WeakReference(this))
    }

    override fun onRetainCustomNonConfigurationInstance(): Any = presenter

    override fun updateRecycler(a: Array<CatFact>) {
        dataList.clear()
        dataList.addAll(a)
        customAdapter.notifyDataSetChanged()
    }

    override fun stopRefreshing() {
        swipe.isRefreshing = false
    }
}
