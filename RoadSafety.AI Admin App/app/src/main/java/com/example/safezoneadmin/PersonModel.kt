package com.example.safezoneadmin

class PersonModel {
    var ImageUrl:String=""
    var disp:String=""
    var lati:String=""
    var longi:String=""
    var id:String=""
    constructor()
    constructor(ImageUrl: String, disp: String, lati: String, longi: String, id: String) {
        this.ImageUrl = ImageUrl
        this.disp = disp
        this.lati = lati
        this.longi = longi
        this.id = id
    }


}