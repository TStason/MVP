package com.example.homeworkcats_1

import android.content.Context
import com.example.homeworkcats_1.DataClasses.CatFact

interface PresenterDelegate {
    val context: Context
    var isFirst: Boolean
    fun updateRecycler(a: Array<CatFact>)
    fun stopRefreshing()
}