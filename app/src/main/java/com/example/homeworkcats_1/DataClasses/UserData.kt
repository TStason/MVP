package com.example.homeworkcats_1.DataClasses

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class UserData(val _id: String="",
               val _v: String="",
               val createdAt: String="",
               val updatedAt: String="",
               val email: String="",
               val name: Name?=null,
               val isAdmin: Boolean=false): Serializable, Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Name::class.java.classLoader),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(_v)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeString(email)
        parcel.writeParcelable(name, flags)
        parcel.writeByte(if (isAdmin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }

    class Name(val first: String="",
                val last: String=""): Serializable, Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(first)
            parcel.writeString(last)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Name> {
            override fun createFromParcel(parcel: Parcel): Name {
                return Name(parcel)
            }

            override fun newArray(size: Int): Array<Name?> {
                return arrayOfNulls(size)
            }
        }
    }
}