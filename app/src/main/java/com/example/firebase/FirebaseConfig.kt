package com.example.firebase

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings

class FirebaseConfig: Application() {
    /*En el método onCreate(), se define un objeto firestoreSettings que se utiliza
    para habilitar la persistencia de datos en Firestore. La persistencia de datos
    permite que la aplicación almacene en caché los datos recuperados de Firestore,
    lo que significa que los usuarios pueden acceder a los datos incluso si no tienen conexión a Internet.*/
    override fun onCreate() {
        super.onCreate()
        val setting = firestoreSettings {
            isPersistenceEnabled = true
        }
        FirebaseFirestore.getInstance().firestoreSettings = setting
    }
}