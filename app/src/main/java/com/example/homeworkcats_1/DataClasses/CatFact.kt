package com.example.homeworkcats_1.DataClasses

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

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
              var imgUrl: String?=null): Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(UserData::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(_v)
        parcel.writeParcelable(user, flags)
        parcel.writeString(text)
        parcel.writeString(updatedAt)
        parcel.writeString(sendDate)
        parcel.writeByte(if (deleted) 1 else 0)
        parcel.writeString(source)
        parcel.writeByte(if (used) 1 else 0)
        parcel.writeString(type)
        parcel.writeString(imgUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CatFact> {
        override fun createFromParcel(parcel: Parcel): CatFact {
            return CatFact(parcel)
        }

        override fun newArray(size: Int): Array<CatFact?> {
            return arrayOfNulls(size)
        }
    }
}