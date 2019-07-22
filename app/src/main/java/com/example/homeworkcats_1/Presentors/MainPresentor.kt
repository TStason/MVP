package com.example.homeworkcats_1.Presentors

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.bumptech.glide.Glide
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.RetrofitClient.Repo
import com.example.homeworkcats_1.SecondActivity
import kotlinx.coroutines.*
import java.lang.Exception

class MainPresentor(private val applicationContext: Context) {

    private val repoFact: Repo.APIservice = Repo.APIservice.getApi(applicationContext)
    private val repoCats: Repo.APIcats = Repo.APIcats.getApi(applicationContext)
    private val jobList: ArrayList<Job> = arrayListOf()

    fun setOnClickCard() = {catFact: CatFact ->
        Log.e("APPCardHolder", "Called OnClick by id=${catFact._id}")
        val intent: Intent = Intent(applicationContext, SecondActivity::class.java)
        intent.putExtra("CatFact", catFact as Parcelable)
        applicationContext.startActivity(intent)

    }

    suspend fun getFacts(): List<CatFact>{
        val res = mutableListOf<CatFact>()
        val job1 = GlobalScope.launch(Dispatchers.Main) {
            try {
            val job = repoFact.getFactsAsync().await()
            if (job.isSuccessful){
                Log.d("APPpresentor", "Response${job.body()}")
                job.body()?.let{
                    res.addAll(it.all)}
            }
            else
                Log.d("APPpresentor", "Response${job.code()}")
            }catch (ex:Exception){
                Log.d("APPpresentor", "Error: ${ex.message}")
            }
        }
        jobList.add(job1)
        job1.join()
        Log.d("APPpresentor", "end receive data from facts repo")

        val job2 = GlobalScope.launch {
            res.forEach {
                try{
                    val job = repoCats.getCatImgAsync().await()
                    delay(100L)
                    if (job.isSuccessful) {
                        it.imgUrl = job.body()?.file
                    }
                } catch (ex: Exception){ Log.d("APPpresentor", "Error: ${ex.message}") }
            }
        }
        jobList.add(job2)
        job2.join()
        Log.d("APPpresentor", "end receive data from cats repo")
        res.forEach {
            Glide.with(applicationContext)
                .load(it.imgUrl)
            }
        return res
    }

    suspend fun onRefresh(): List<CatFact>{
        val res = arrayListOf<CatFact>()
        val job = GlobalScope.launch(Dispatchers.Main) {
            try{
                res.addAll(getFacts())
            } catch (ex: Exception){ Log.d("APPpresentor", "Error: ${ex.message}") }
        }
        jobList.add(job)
        job.join()
        return res
    }

    fun onDestroy(){
        jobList.forEach {
            it.cancelChildren()
            it.cancel()
            Log.e("APPPresentor", "Canceled job: ${it}")
        }
    }

}