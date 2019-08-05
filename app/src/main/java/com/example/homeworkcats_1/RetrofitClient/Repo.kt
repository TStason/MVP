package com.example.homeworkcats_1.RetrofitClient

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.example.homeworkcats_1.DataClasses.CatImage
import com.example.homeworkcats_1.DataClasses.CostylClass
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.File
import java.util.concurrent.TimeUnit

class Repo {

    interface APIcats{
        @GET("/meow")
        fun getCatImgAsync(): Call<CatImage>

        companion object Factory {
            private const val baseURL = "https://aws.random.cat"
            fun getApi(context: Context, baseUrl: String= baseURL): APIcats = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(getClient(context))
                .build()
                .create(APIcats::class.java)
        }
    }

    interface APIfacts {

        @GET("/facts")
        fun getFactsAsyncOld(): Call<CostylClass>

        companion object Factory {
            private const val baseURL = "https://cat-fact.herokuapp.com"
            fun getApi(context: Context, baseUrl : String= baseURL): APIfacts = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(HttpClient.getClient(context))
                .build()
                .create(APIfacts::class.java)
        }
    }

    companion object HttpClient {
        private val cacheSize: Long = 10 * 1024 * 1024
        private val HEADER_CACHE_CONTROL = "Cache-Control"
        private val HEADER_PRAGMA = "Pragma"

        private fun hasNetwork(context: Context): Boolean {
            var isConnected= false // Initial Value
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

        private fun getCache(path: String) = Cache(File(path, "HomeworkCats_1LocalStorage"), cacheSize)

        private fun getOfflineInterceptor(context: Context) = Interceptor{
            var request = it.request()
            //Log.e("APPRepo", "Call offline interceptor...")
            //Log.e("APPRepo", "Called URL: ${request}")
            if (!hasNetwork(context)){
                val cacheControl = CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()
                request = request
                    .newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .cacheControl(cacheControl)
                    .build()
            }
            return@Interceptor it.proceed(request)
        }

        private fun getNetworkInterceptor() = Interceptor {
            //Log.e("APPRepo", "Call network interceptor...")
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