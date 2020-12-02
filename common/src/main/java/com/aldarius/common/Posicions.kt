package com.aldarius.common

import kotlinx.serialization.*

@Serializable
data class Posicions (
    var posicions : ArrayList<Posicio> = ArrayList()
)
/*
{
    fun setPosicio(entitat: String = "",
                   momentInicial: String = "",
                   momentFinal: String = "",
                   estatInicial: String = "",
                   estatFinal: String = "",
                   grau: Double = 0.0) {
        posicions.add(Posicio(entitat, momentInicial, momentFinal, estatInicial, estatFinal, grau))
    }
}
*/