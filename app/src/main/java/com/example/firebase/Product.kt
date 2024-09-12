package com.example.firebase
import com.google.firebase.firestore.IgnoreExtraProperties

//@IgnoreExtraProperties indica que las propiedades no
//explícitamente definidas en la clase no deben ser serializadas ni deserializadas por Firebase
@IgnoreExtraProperties
class Product(
    var id: String = "",
    var nameProduct: String = "",
    var barcode: String = "",
    var description: String = "",
    var quantity: Double = 0.0,
    var minimumQuantity: Double = 0.0,
    var photoUrl: String = "",
    var unitType: String = "kg"
){
    override fun equals(other: Any?): Boolean {
        if (this === other)return true
        if (javaClass != other?.javaClass) return false

        other as Product
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}