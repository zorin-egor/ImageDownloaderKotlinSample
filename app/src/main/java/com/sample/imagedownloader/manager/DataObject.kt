package com.sample.imagedownloader.manager

import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataObject(
    @Expose var id: String? = null,
    @Expose var login: String? = null,
    @Expose var avatar_url: String? = null
) : Parcelable