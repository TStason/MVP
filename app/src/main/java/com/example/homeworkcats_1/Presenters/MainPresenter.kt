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

class MainPresenter(private var view: WeakReference<PresenterDelegate>?) {
    private val TAG = "APPpresenter"
    //state
    private var isRefreshed = false
    //coroutines
    private lateinit var parentJob: Job
    private lateinit var presenterScope: CoroutineScope
    //repo
    private lateinit var repoFact: Repo.APIfacts
    private lateinit var repoCats: Repo.APIcats

    init{
        setRepos()
        setPresenterScope()
    }

    private fun setRepos(){
        view?.get()?.let {
            repoFact = Repo().getRepo<Repo.APIfacts>("https://cat-fact.herokuapp.com", it.context)
            repoCats = Repo().getRepo<Repo.APIcats>("https://aws.random.cat", it.context)
        }
    }

    private fun setPresenterScope(){
        if (!::presenterScope.isInitialized || !::parentJob.isInitialized || parentJob.isCancelled){
            parentJob = SupervisorJob()
            parentJob.invokeOnCompletion {
                it?.message.let {
                    var msg = it
                    if (msg.isNullOrBlank()){
                        msg = "Unknown cancelling error."
                    }
                    Log.e(TAG, "$parentJob was cancelled. Reason: $msg")
                }
            }
            presenterScope = CoroutineScope(Dispatchers.Default + parentJob)
            Log.d(TAG, "PresenterScope = $presenterScope, mainJob = $parentJob")
        }
    }

    fun attachView(newView: WeakReference<PresenterDelegate>){
        //Log.e(TAG, "OldView: $view, NewView: $newView")
        view = newView
        setRepos()
        setPresenterScope()
    }

    fun onClickCard() = { catFact: CatFact ->
        Log.e("APPCardHolder", "Called OnClick by id=${catFact}")
        view?.get()?.let{ delegate ->
            val intent = Intent(delegate.context, SecondActivity::class.java)
            intent.putExtra("CatFact", catFact as Parcelable)
            delegate.context.startActivity(intent)
        }
    }

    private fun toastOnUI(text: String?) = presenterScope.launch(Dispatchers.Main){
        view?.get()?.let{
            Toast.makeText(it.context, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun onRefresh() {
        getData()
    }

    private fun updateRecycler(a: Array<CatFact>) {
        Log.e(TAG, "Receiver: $view")
        view?.get()?.let { delegate ->
            delegate.updateRecycler(a)
            delegate.stopRefreshing()
            toastOnUI("Update ended")
        }
    }

    private fun getData() = presenterScope.launch{
        if (isRefreshed) {
            Log.d(TAG, "Already refreshing")
            toastOnUI("Already refreshing")
            return@launch
        }
        isRefreshed = true
        try {
            val res = repoFact.getFactsAsyncOld().await().all
            Log.e(TAG, "Ended load facts")
            res.forEach { fact ->
                fact.imgUrl = repoCats.getCatImgAsync().await().file
            }
            Log.e(TAG, "Ended load img")
            withContext(Dispatchers.Main){
                updateRecycler(res)
            }
        } catch (ex: Exception) {
            toastOnUI(ex.message)
            Log.e(TAG, "${ex.message}")
        } finally {
            Log.e(TAG, "Finally")
            isRefreshed = false
        }
    }

    fun onStart() {
        view?.get()?.let{ delegate ->
            if (delegate.isFirst){
                delegate.isFirst = false
                getData()
            }
        }
    }

    private fun cancelAllJob(){
        presenterScope.coroutineContext.cancelChildren(CancellationException("Requester on destroyed"))
    }

    fun onDestroy() {
        Log.d(TAG, "onDestroy")
        view = null
        //cancelAllJob()
    }

    private suspend fun <T> Call<T>.await(): T = suspendCoroutine{cont ->
        presenterScope.launch(Dispatchers.IO) {
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    Log.e("APPRepo", "onFailur: ${t.message}")
                    cont.resumeWith(Result.failure(t))
                }
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    //Log.d("APPRepo", "onResponse: $response")
                    if (response.isSuccessful)
                        response.body()?.let {cont.resumeWith(Result.success(it))} ?:
                        cont.resumeWith(Result.failure(IllegalArgumentException("NULL")))
                    else
                        cont.resumeWith(Result.failure(IllegalArgumentException("Error code: ${response.code()}")))
                }
            })
        }
    }
}