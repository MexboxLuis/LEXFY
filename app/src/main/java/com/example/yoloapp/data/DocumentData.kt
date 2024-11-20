package com.example.yoloapp.data

import java.util.Date

import android.os.Parcel
import android.os.Parcelable


data class DocumentData(
    val documentId: String,
    val date: Date,
    val imageUrl: String,
    val text: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        Date(parcel.readLong()),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentId)
        parcel.writeLong(date.time)
        parcel.writeString(imageUrl)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DocumentData> {
        override fun createFromParcel(parcel: Parcel): DocumentData {
            return DocumentData(parcel)
        }

        override fun newArray(size: Int): Array<DocumentData?> {
            return arrayOfNulls(size)
        }
    }
}
