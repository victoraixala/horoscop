package com.aldarius.common

import java.util.*


class FilaAspecte(
    id: Int,
    detall: ArrayList<Detall>?,
    imatgePlanetaActual: Int,
    nomPlanetaActual: String,
    imatgeAspecte: Int,
    imatgePlanetaNatal: Int,
    nomPlanetaNatal: String
) {
    var id = -1

    private var detall: ArrayList<Detall>? = null
    var imatgePlanetaActual = -1
    var nomPlanetaActual = ""
    var imatgeAspecte = -1
    var imatgePlanetaNatal = -1
    var nomPlanetaNatal = ""

    fun getDetall(): ArrayList<Detall>? {
        return detall
    }

    fun setDetall(detall: ArrayList<Detall>?) {
        this.detall = detall
    }

    init {
        this.id = id
        this.detall = detall
        this.imatgePlanetaActual = imatgePlanetaActual
        this.nomPlanetaActual = nomPlanetaActual
        this.imatgeAspecte = imatgeAspecte
        this.imatgePlanetaNatal = imatgePlanetaNatal
        this.nomPlanetaNatal = nomPlanetaNatal
    }
}