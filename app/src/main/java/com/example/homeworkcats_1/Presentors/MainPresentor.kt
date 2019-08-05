package com.example.homeworkcats_1.Presentors

import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.MainActivity
import com.example.homeworkcats_1.RetrofitClient.Repo
import com.example.homeworkcats_1.SecondActivity
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.coroutines.suspendCoroutine

class MainPresentor(private val view: MainActivity) {

    private val TAG = "APPpresentor"
    //state
    private var isRefreshed = false
    //coroutine
    private val parentJob = Job()
    private val mainScope = CoroutineScope(Dispatchers.Main + parentJob)
    //repo
    private val repoFact = Repo.APIfacts.getApi(view.applicationContext)
    private val repoCats: Repo.APIcats = Repo.APIcats.getApi(view.applicationContext)

    fun onClickCard() = { catFact: CatFact ->
        Log.e("APPCardHolder", "Called OnClick by id=${catFact._id}")
        val intent = Intent(view.applicationContext, SecondActivity::class.java)
        intent.putExtra("CatFact", catFact as Parcelable)
        view.applicationContext.startActivity(intent)

    }

    fun onRefresh() = mainScope.launch{
        if (isRefreshed){
            view.swipe.isRefreshing = false
            Log.d(TAG, "Already refreshing")
            Toast.makeText(view.applicationContext, "Already refreshing", Toast.LENGTH_SHORT).show()
        } else {
            getData().join()
            view.swipe.isRefreshing = false
        }
    }

    fun getData() = mainScope.launch{
        if (isRefreshed){
            Log.d(TAG, "Already refreshing")
            Toast.makeText(view.applicationContext, "Already refreshing", Toast.LENGTH_SHORT).show()
            return@launch
        }
        isRefreshed = true
        try{
            val res = this@MainPresentor.repoFact.getFactsAsyncOld().await().all
            res.forEach {
                it.imgUrl = repoCats.getCatImgAsync().await().file
                delay(1000L)
            }
            view.dataList.addAll(res)
            view.customAdapter.notifyDataSetChanged()
        }
        catch (ex: Exception){
            Toast.makeText(view.applicationContext, "${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "${ex.message}")
        }
        isRefreshed = false
    }

    fun onStart(){
        if (view.isFirst || view.dataList.isNullOrEmpty()){
            getData()
            view.isFirst = false
        }
    }

    fun onDestroy(){
        Log.d(TAG, "onDestroy")
        parentJob.cancel()
        Log.e(TAG, "Canceled parent job: $parentJob")
    }

    suspend fun <T> Call<T>.await(): T = suspendCoroutine{cont ->
        mainScope.launch(Dispatchers.IO) {
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Log.e("APPRepo", "onFailur: ${t.message}")
                    cont.resumeWith(Result.failure(t))
                }
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    Log.e("APPRepo", "onResponse: $response")
                    if (response.isSuccessful)
                        response.body()?. let {cont.resumeWith(Result.success(it))} ?:
                        cont.resumeWith(Result.failure(IllegalArgumentException("NULL")))
                    else
                        cont.resumeWith(Result.failure(IllegalArgumentException("Error code: ${response.code()}")))
                }
            })
        }
    }
}