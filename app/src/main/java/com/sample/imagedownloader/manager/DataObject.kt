package com.sample.imagedownloader.manager

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose

class DataObject() : Parcelable {

    companion object CREATOR : Parcelable.Creator<DataObject> {
        override fun createFromParcel(parcel: Parcel): DataObject {
            return DataObject(parcel)
        }

        override fun newArray(size: Int): Array<DataObject?> {
            return arrayOfNulls(size)
        }
    }

    @Expose
    var id: String? = null

    @Expose
    var login: String? = null

    @Expose
    var avatar_url: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        login = parcel.readString()
        avatar_url = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(login)
        parcel.writeString(avatar_url)
    }

    override fun describeContents(): Int {
        return 0
    }

}