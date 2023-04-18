package com.ardusec.ardu_security

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ArduSecFirebase {
    private var bd: FirebaseDatabase = Firebase.database
    private var ref: DatabaseReference = bd.reference

    fun getReference(Collection: String): DatabaseReference {
        return ref.child(Collection)
    }

    fun setValueStr(Value: String, Reference: DatabaseReference){
        Reference.setValue(Value)
    }

    fun setValueHashStr(ArrVal: HashMap<String, String>, Reference: DatabaseReference){
        var addReg = Reference.push()
        addReg.setValue(ArrVal)
    }

    fun setValueHashInt(ArrVal: HashMap<String, Int>, Reference: DatabaseReference){
        var addReg = Reference.push()
        addReg.setValue(ArrVal)
    }
}