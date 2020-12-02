package com.aldarius.common

import kotlinx.serialization.*
//import java.util.*

@Serializable
data class Aspecte(val ePersona: Entitat,
                   val tipusAspecte: Double,
                   val eActual: Entitat,
                   var distancia: Double,
                   var apliSepa: String,
                   var momentInicial: String,
                   var momentExacte: String,
                   var momentFinal: String,
                   var grauExacteActual: Double)