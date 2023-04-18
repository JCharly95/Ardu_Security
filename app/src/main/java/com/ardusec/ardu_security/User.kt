package com.ardusec.ardu_security

import com.google.firebase.database.DatabaseReference

class User(Nom: String, Ema: String, Pass: String, Typ: String, Ques: String, Ans: String, PinPass: Int, Reference: DatabaseReference){
    private lateinit var ID_User: String
    private val Nombre = Nom
    private val Correo = Ema
    private val Contra = Pass
    private val Tipo = Typ
    private val Pregunta = Ques
    private val Resp = Ans
    private val Pin = PinPass
    private val ref = Reference

    fun getIdUser(){
        ref.orderByChild("Correo").equalTo(Correo)
    }
}