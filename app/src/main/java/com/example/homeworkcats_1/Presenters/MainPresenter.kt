package com.example.homeworkcats_1.Presenters

import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.PresenterDelegate
import com.example.homeworkcats_1.RetrofitClient.Repo
import com.example.homeworkcats_1.SecondActivity
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import kotlin.coroutines.suspendCoroutine

class MainPresenter(private val view: WeakReference<PresenterDelegate>) {

    private val TAG = "APPpresenter"
    //state
    private var isRefreshed = false
    //coroutine
    private val parentJob = Job()
    private val mainScope = CoroutineScope(Dispatchers.Main + parentJob)
    //repo
    private lateinit var repoFact: Repo.APIfacts
    private lateinit var repoCats: Repo.APIcats

    init{
        view.get()?.let {
            repoFact = Repo.APIfacts.getApi(it.context)
            repoCats = Repo.APIcats.getApi(it.context)
        }
    }
    fun onClickCard() = { catFact: CatFact ->
        Log.e("APPCardHolder", "Called OnClick by id=${catFact._id}")
        view.get()?.let{
            val intent = Intent(it.context, SecondActivity::class.java)
            intent.putExtra("CatFact", catFact as Parcelable)
            it.context.startActivity(intent)
        }
    }

    fun onRefresh() = mainScope.launch{
        view.get()?.let{
            if (isRefreshed){
                it.stopRefreshing()
                Log.d(TAG, "Already refreshing")
                Toast.makeText(it.context, "Already refreshing", Toast.LENGTH_SHORT).show()
            } else {
                getData().join()
                it.stopRefreshing()
            }
        }
    }

    fun getData() = mainScope.launch{
        view.get()?.let{
            if (isRefreshed){
                Log.d(TAG, "Already refreshing")
                Toast.makeText(it.context, "Already refreshing", Toast.LENGTH_SHORT).show()
                return@launch
            }
            isRefreshed = true
            try{
                val res = this@MainPresenter.repoFact.getFactsAsyncOld().await().all
                res.forEach {
                    it.imgUrl = repoCats.getCatImgAsync().await().file
                    delay(10L)
                }
                it.updateRecycler(res)
            }
            catch (ex: Exception){
                Toast.makeText(it.context, "${ex.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "${ex.message}")
            }
            isRefreshed = false
        }
    }

    fun onStart() = mainScope.launch{
        view.get()?.let{
            if (it.isFirst){
                getData().join()
                it.isFirst = false
            }
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