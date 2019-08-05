package com.example.homeworkcats_1.DataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class UserData(val _id: String="",
               val _v: String="",
               val createdAt: String="",
               val updatedAt: String="",
               val email: String="",
               val name: Name?=null,
               val isAdmin: Boolean=false): Parcelable{

    @Parcelize
    class Name(val first: String="",
                val last: String=""): Parcelable


}