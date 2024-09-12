package com.example.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
//Conexion de nuestra base de datos
class UserDAO {
    private val db = FirebaseFirestore.getInstance()

    fun getAllUsers(): MutableList<User>{
        var userList: MutableList<User> = mutableListOf()
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                for (user in result) {
                    userList.add(user.toObject<User>())
                }
            }
        println("Usuarios en async ${userList.size}")
        return userList


    }
}