package com.ardusec.ardu_security

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

class ArduSecurityLite(context: Context, name: String, factory: CursorFactory?, version: Int): SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(bd: SQLiteDatabase) {
        // Creacion de las tablas adecuadas de la base de datos para la interaccion del usuario
        bd.execSQL("CREATE TABLE Archivo (ID_Archivo int primary key,Nom_Archi text,Ubi_Save text)")
        bd.execSQL("CREATE TABLE Nombre_Sis (ID_NomSis int primary key,Val_NomSis text,Admin_NomSis_ID int)")
        bd.execSQL("CREATE TABLE Pregunta (ID_Pregunta int primary key,Val_Pregunta text)")
        bd.execSQL("CREATE TABLE Tel_Admin (ID_Tel_Admin int primary key,Val_Telefono int,Admin_ID int)")
        bd.execSQL("CREATE TABLE User_Tipo (ID_UserTip int primary key,Nom_UserTip text)")
        bd.execSQL("CREATE TABLE Usuarios (ID_User int primary key,Nombre text,Correo text,Contrasenia text,UserTip_ID int,Pregunta_ID int,Resp_Preg_Seg text,Pin_Pass int)")

    }

    override fun onUpgrade(bd: SQLiteDatabase, oldVer: Int, newVer: Int) {

    }
}