package com.example.firebase

//función llamada "getErrorString".
//La función toma una cadena de texto opcional llamada "error" y
//devuelve una cadena de texto en español que describe el error que se produjo
class ExceptionsMessages {
    fun getErrorString(error: String? = ""): String{
        return when(error){
            "The email address is badly formatted." -> "El formato de la dirección de correo electronico no es valido."
            "The password is invalid or the user does not have a password." -> "El usuario y/o la contraseña esta mal."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "El usuario y/o la contraseña esta mal."
            else -> "Algo ha salido mal: ${error}"
        }
    }
}