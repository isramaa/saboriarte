package com.example.firebase

import android.provider.ContactsContract.CommonDataKinds.Phone
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String = "",
    var email: String = "",
    var password: String = "",
    var name: String = "",
    var lastName: String = "",
    var isAdmin: Boolean = false,
    var photoUrl: String = "",
    var phone: String = "",
var isOwner: Boolean = false

    ){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return true

        other as User
        if (id != other.id) return false
        return true
        //return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
