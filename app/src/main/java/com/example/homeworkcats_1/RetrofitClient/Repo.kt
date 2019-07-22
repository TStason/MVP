package com.example.homeworkcats_1.RetrofitClient

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.example.homeworkcats_1.DataClasses.CatFact
import com.example.homeworkcats_1.DataClasses.CatImage
import com.example.homeworkcats_1.DataClasses.CostylClass
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.File
import java.util.concurrent.TimeUnit

class Repo {

    interface APIservice {
        @GET("/facts")
        fun getFactsAsync(): Deferred<Response<CostylClass>>//List<CatFact>

        companion object Factory {
            private const val baseURL = "https://cat-fact.herokuapp.com"
            fun getApi(context: Context, baseUrl : String= baseURL): APIservice = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(HttpClient.getClient(context))
                .build()
                .create(APIservice::class.java)
        }
    }

    interface APIcats{
        @GET("/meow")
        fun getCatImgAsync(): Deferred<Response<CatImage>>

        companion object Factory {
            private const val baseURL = "https://aws.random.cat"
            fun getApi(context: Context, baseUrl: String= baseURL): APIcats = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(getClient(context))
                .build()
                .create(APIcats::class.java)
        }
    }

    companion object HttpClient {
        private val cacheSize: Long = 2 * 1024 * 1024
        private val HEADER_CACHE_CONTROL = "Cache-Control"
        private val HEADER_PRAGMA = "Pragma"

        private fun hasNetwork(context: Context): Boolean {
            var isConnected: Boolean = false // Initial Value
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnected)
                isConnected = true
            return isConnected
        }

        fun getClient(context: Context) = OkHttpClient.Builder()
            .cache(getCache(context.cacheDir.path))
            .addInterceptor(getOfflineInterceptor(context))
            .addNetworkInterceptor(getNetworkInterceptor())
            .build()

        private fun getCache(path: String) = Cache(File(path, "FoodAppLocalStorage"), cacheSize)

        private fun getOfflineInterceptor(context: Context) = Interceptor{
            var request = it.request()
            Log.e("APPRepo", "Call offline interceptor...")
            Log.e("APPRepo", "Called URL: ${request}")
            if (!hasNetwork(context)){
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()

                request = request
                    .newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .cacheControl(cacheControl)
                    .build()
            }
            return@Interceptor it.proceed(request)
        }

        private fun getNetworkInterceptor() = Interceptor {
            Log.e("APPRepo", "Call network interceptor...")
            val response = it.proceed(it.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(5, TimeUnit.SECONDS)
                .build()
            response.newBuilder()
                .removeHeader(HEADER_PRAGMA)
                .removeHeader(HEADER_CACHE_CONTROL)
                .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                .build()
        }

    }

}