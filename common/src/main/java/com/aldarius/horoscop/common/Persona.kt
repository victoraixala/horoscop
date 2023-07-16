package com.aldarius.horoscop.common

class Persona (
    var nom: String = "",
    var dataNaixement: String = "",
    var horaNaixement: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var altitud: Double = 0.0,
    var zona: Double = 0.0,
    var entitats : ArrayList<Entitat> = ArrayList()
) {
    fun setEntitat(nom: String,
                   grau: Double,
                   retrograd: String,
                   tipus: String,
                   casa: String) {
        entitats.add(Entitat(nom, grau, retrograd, tipus, casa))
    }
}
