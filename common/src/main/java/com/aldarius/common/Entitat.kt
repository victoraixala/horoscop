package com.aldarius.common

import kotlinx.serialization.*

@Serializable
data class Entitat(val nom: String,
                   val grau: Double,
                   var retrograd: String,
                   val tipus: String,
                   var casa: String) {
    // el tipus de cos celest és d'ús intern i per tant no cal traduir-lo amb fitxers de recursos
    //private val id = 0
    //private val nom = ""
    //val grau = 0.0
    //private val retrograd = ' '
}
