package com.liner.findme.network.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class PhotoData(val data: ByteArray) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoData

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        val result = 31 + data.contentHashCode()
        return result
    }
}
