package com.example.myapplication

import com.google.firebase.firestore.FieldValue


class PersonModel {
    var ImageUrl:String=""
    var lati:String=""
    var longi:String=""
    var disp:String="No Description Provided By User"
    val createdAt: FieldValue = FieldValue.serverTimestamp()
    var id: String = ""

    constructor(ImageUrl: String, disp: String) {
        this.ImageUrl = ImageUrl
        this.disp = disp
    }
    constructor()

}