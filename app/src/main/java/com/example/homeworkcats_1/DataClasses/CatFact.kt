package com.example.homeworkcats_1.DataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CatFact(val _id: String="",
              val _v: String="",
              val user: UserData?=null,
              val text: String="",
              val updatedAt: String="",
              val sendDate: String="",
              val deleted: Boolean=false,
              val source: String="",
              val used: Boolean=false,
              val type: String="",
              var imgUrl: String?=null): Parcelable