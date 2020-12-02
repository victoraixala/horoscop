package com.aldarius.novapersona

import android.text.TextUtils.indexOf
import androidx.lifecycle.ViewModel
import com.aldarius.common.Entitat
import com.aldarius.common.Persona
import kotlinx.serialization.json.Json
import swisseph.SweConst
import swisseph.SweConst.SE_AST_OFFSET
import swisseph.SweDate
import swisseph.SwissEph
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter


class NovaPersonaViewModel : ViewModel() {
    var tNom = ""
    var tDia = ""
    var tHora = ""
    var tLatitud = ""
    var tNS = ""
    var tLongitud = ""
    var tEW = ""
    var tAltitud = ""
    var tZona = ""
    var tRutaAssets = ""
    var tRutaPersones = ""
    private var dades = ""
    private var persona: Persona = Persona()
    private var grauFortuna = 0.0
    private var grauInfortuni = 0.0

    internal fun gravarNatal(etNom: String,
                    etDia: String,
                    etHora: String,
                    etLatitud: String,
                    etNS: String,
                    etLongitud: String,
                    etEW: String,
                    etAltitud: String,
                    etZona: String,
                    rutaAssets: String,
                    rutaPersones: String): String {
        /*
        etNom!!.text = "Víctor Aixalà"
        etDia!!.text = "16/2/1982"
        etHora!!.text = "12:45:00"
        etLatitud!!.text = "41:24:49"
        etLongitud!!.text = "2:8:33"
        etAltitud!!.text = "129"
        */
        tNom = etNom
        tDia = etDia
        tHora = etHora
        tLatitud = etLatitud
        tNS = etNS
        tLongitud = etLongitud
        tEW = etEW
        tAltitud = etAltitud
        tZona = etZona
        tRutaAssets = rutaAssets
        tRutaPersones = rutaPersones
        if (validarEntrada() == "") {
            dades = calcularDadesNatals()
        }
        return dades
    }

    private fun validarEntrada(): String {

        var tValidacio: String = ""

        if (tNom.trim().isEmpty()) {tValidacio = R.string.erInputNom.toString()}

        if (tValidacio == "" && tDia.trim().isEmpty()) {tValidacio = R.string.erInputDia.toString()}

        if (tValidacio == "") {tValidacio = validarData(tDia)}

        if (tValidacio == "" && tHora.trim().isEmpty()) {tValidacio = R.string.erInputHora.toString()}

        if (tValidacio == "") {tValidacio = validarHora(tHora)}

        if (tValidacio == "" && tLatitud.trim().isEmpty()) {tValidacio = R.string.erInputLatitud.toString()}

        if (tValidacio == "") {tValidacio = validarCoordenada(tLatitud)}

        if (tValidacio == "" && tLongitud.trim().isEmpty()) {tValidacio = R.string.erInputLongitud.toString()}

        if (tValidacio == "") {tValidacio = validarCoordenada(tLongitud)}

        if (tValidacio == "" && tAltitud.trim().isEmpty()) {tValidacio = R.string.erInputAltitud.toString()}

        return tValidacio
    }

    private fun validarData(tDia: String): String {

        var c = ""
        val data: Array<String>
        var delimitador = ""
        if (tDia.contains("/")) {
            delimitador = "/"
        } else {
            if (tDia.contains("-")) {
                delimitador = "-"
            }
        }
        data = tDia.split(delimitador.toRegex(), 3).toTypedArray()
        val dma = IntArray(3)
        val mesosDe30 = intArrayOf(4, 6, 9, 11)
        for (n in 0..2) {
            dma[n] = Integer.parseInt(data[n])
        }
        if (dma[0] > 31 || dma[1] > 12 || dma[2] == 0) {
            c = R.string.erInputDiaI.toString()
        }
        if (c != "") {
            if (dma[1] == 2) {
                if (dma[2] % 4 == 0) {
                    // any de traspàs
                    if (dma[0] > 29) {
                        c = R.string.erInputDiaI.toString()
                    }
                } else {
                    // any normal
                    if (dma[0] > 28) {
                        c = R.string.erInputDiaI.toString()
                    }
                }
            } else {
                for (n in mesosDe30) {
                    if (n == dma[1] && dma[0] > 30) {
                        c = R.string.erInputDiaI.toString()
                    }
                }
            }
        }

        return c
    }

    private fun validarHora(hora: String): String {
        var c = ""
        val h: Array<String>
        var delimitador = ""
        if (hora.contains(":")) {
            delimitador = ":"
        } else {
            if (hora.contains(".")) {
                delimitador = "."
            }
        }
        h = hora.split(delimitador.toRegex(), 3).toTypedArray()
        val hms = IntArray(3)
        for (n in 0..2) {
            hms[n] = Integer.parseInt(h[n])
        }
        if (hms[0] > 23 || hms[1] > 59 || hms[2] > 59) {
            c = R.string.erInputHoraI.toString()
        }
        return c
    }

    private fun validarCoordenada(coord: String): String {
        var c = ""
        val h: Array<String>
        var delimitador = ""
        if (coord.contains(":")) {
            delimitador = ":"
        } else {
            if (coord.contains(".")) {
                delimitador = "."
            }
        }
        h = coord.split(delimitador.toRegex(), 3).toTypedArray()
        if (h[0] == "" || h[1] == "" || h[2] == "") {
            c = R.string.erInputLatitudI.toString()
        }
        return c
    }

    private fun calcularDadesNatals(): String {

        // separem la data
        val data: Array<String>
        var delimitador = ""
        if (tDia.contains("/")) {
            delimitador = "/"
        } else {
            if (tDia.contains("-")) {
                delimitador = "-"
            }
        }
        data = tDia.split(delimitador.toRegex(), 3).toTypedArray()
        val dia = Integer.parseInt(data[0])
        val mes = Integer.parseInt(data[1])
        val any = Integer.parseInt(data[2])

        // separem la latitud
        val lat: Array<String>
        if (tLatitud.contains(":")) {
            delimitador = ":"
        } else {
            if (tLatitud.contains(".")) {
                delimitador = "."
            }
        }
        lat = tLatitud.split(delimitador.toRegex(), 3).toTypedArray()
        val latG = Integer.parseInt(lat[0]).toDouble()
        val latM = Integer.parseInt(lat[1]).toDouble()
        val latS = Integer.parseInt(lat[2]).toDouble()

        var latitud: Double
        if (tNS == "N") {
            latitud = latG + latM / 60.0 + latS / 3600.0
        } else {
            latitud = -latG - latM / 60.0 - latS / 3600.0
        }

        // separem la longitud
        val lon: Array<String>
        if (tLongitud.contains(":")) {
            delimitador = ":"
        } else {
            if (tLongitud.contains(".")) {
                delimitador = "."
            }
        }
        lon = tLongitud.split(delimitador.toRegex(), 3).toTypedArray()
        val lonG = Integer.parseInt(lon[0]).toDouble()
        val lonM = Integer.parseInt(lon[1]).toDouble()
        val lonS = Integer.parseInt(lon[2]).toDouble()

        val longitud: Double
        if (tEW == "E") {
            longitud = lonG + lonM / 60.0 + lonS / 3600.0
        } else {
            longitud = -lonG - lonM / 60.0 - lonS / 3600.0
        }

        val altitud = java.lang.Double.parseDouble(tAltitud)

        // separem l'hora
        val horaN: Array<String>
        if (tHora.contains(":")) {
            delimitador = ":"
        } else {
            if (tHora.contains(".")) {
                delimitador = "."
            }
        }
        horaN = tHora.split(delimitador.toRegex(), 3).toTypedArray()
        val h = Integer.parseInt(horaN[0]).toDouble()
        val m = Integer.parseInt(horaN[1]).toDouble()
        val s = Integer.parseInt(horaN[2]).toDouble()

        // tractem la zona horària
        var zona = java.lang.Double.parseDouble(tZona.substring(0, 3))
        when (tZona.substring(4, 6)) {
            "30" -> zona += 0.5
            "45" -> zona += 0.75
        }

        val hora = h + m / 60.0 + s / 3600.0 - zona
        // 12:45 - 1h per la zona horària (+01:00)
        //double hour = 12 + 45.0 / 60.0 - 1;

        /*Instances of utility classes */
        val sw = SwissEph(tRutaAssets)
        val sd = SweDate(any, mes, dia, hora)

        // Set sidereal mode:
        //        sw.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
        // utilitzo geocèntric
        sw.swe_set_topo(longitud, latitud, altitud)

        persona.nom = tNom
        persona.zona = zona

        dades = gravarData(sd, zona)
        dades += "\n" + "\n" + gravarCoordenades(latitud, longitud, altitud)
        dades += "\n" + gravarCases(sw, sd, latitud, longitud)
        dades += "\n" + "\n" + gravarPlanetesNodesLilithParts(sw, sd)
        dades += "\n" + gravarAsteroides(sw, sd)
        dades += "\n" + gravarVertex(sw, sd, latitud, longitud)
        //dades += "\n" + gravarEstrellesFixes(sw, sd)

        //////////////////////////////////////////////
        // Output ayanamsa value:
        //////////////////////////////////////////////
        //dades += "\n" + ayanamsa(sw, sd)

        //////////////////////////////////////////////
        // Output lagna:
        //////////////////////////////////////////////
        //dades += getLagnainfo(sw, sd, longitud, latitud)

        return dades
    }

    private fun gravarData(sd: SweDate, zona: Double): String {
        // hora arreglada!!!
        val hora = sd.hour.toInt()
        val min = ((sd.hour - hora) * 60).toInt()
        val sec = (((sd.hour - hora) * 60 - min) * 60).toInt()

        persona.dataNaixement = tDia
        /*
        persona.dataNaixement = String.format(
            "%02d-%02d-%4d",
            sd.day,
            sd.month,
            sd.year
        )
        */

        persona.horaNaixement = tHora
        /*
        persona.horaNaixement = String.format(
            "%2d:%02d:%02dh UTC",
            (hora + zona).toInt(),
            min,
            sec
        )
        */

        return persona.dataNaixement + " " + persona.horaNaixement
    }

    private fun gravarCoordenades(latitud: Double, longitud: Double, altitud: Double): String {
        persona.latitud = latitud
        persona.longitud = longitud
        persona.altitud = altitud
        return "Latitud: " + aGMS(latitud,false) + (if (latitud > 0) "N" else "S") + "\n" +
                "Longitud:" + aGMS(longitud,false) + (if (longitud > 0) "E" else "W") + "\n" +
                "Altitud: " + altitud
    }

    private fun gravarCases(sw: SwissEph, sd: SweDate, latitud: Double, longitud: Double): String {
        // cims de les cases

        val cims = DoubleArray(13)
        val punts = DoubleArray(10)
        var s = ""

        sw.swe_houses(sd.julDay,
            0,
            latitud,
            longitud,
            'P'.toInt(),
            cims,
            punts)

        s += "AC: " + aGMS(punts[0],true) + "\n"
        persona.setEntitat("AC", punts[0], " ", "Angle", "")
        s += "C2: " + aGMS(cims[2],true) + "\n"
        persona.setEntitat("C2", cims[2], " ", "Cim", "")
        s += "C3: " + aGMS(cims[3],true) + "\n"
        persona.setEntitat("C3", cims[3], " ", "Cim", "")
        s += "IC: " + aGMS(cims[4],true) + "\n"
        persona.setEntitat("IC", cims[4], " ", "Angle", "")
        s += "C5: " + aGMS(cims[5],true) + "\n"
        persona.setEntitat("C5", cims[5], " ", "Cim", "")
        s += "C6: " + aGMS(cims[6],true) + "\n"
        persona.setEntitat("C6", cims[6], " ", "Cim", "")
        s += "DC: " + aGMS(cims[7],true) + "\n"
        persona.setEntitat("DC", cims[7], " ", "Angle", "")
        s += "C8: " + aGMS(cims[8],true) + "\n"
        persona.setEntitat("C8", cims[8], " ", "Cim", "")
        s += "C9: " + aGMS(cims[9],true) + "\n"
        persona.setEntitat("C9", cims[9], " ", "Cim", "")
        s += "MC:  " + aGMS(punts[1],true) + "\n"
        persona.setEntitat("MC", punts[1], " ", "Angle", "")
        s += "C11: " + aGMS(cims[11],true) + "\n"
        persona.setEntitat("C11", cims[11], " ", "Cim", "")
        s += "C12: " + aGMS(cims[12],true) + "\n"
        persona.setEntitat("C12", cims[12], " ", "Cim", "")
        //s += "armc: " + aGMS(punts[2]) + "\n";
        //s += "ascendent equatorial: " + aGMS(punts[4]) + "\n";
        //s += "co-ascendent (Walter Koch): " + aGMS(punts[5]) + "\n";
        //s += "co-ascendent (Michael Munkasey): " + aGMS(punts[6]) + "\n";
        //s += "ascendent polar (Michael Munkasey): " + aGMS(punts[7]) + "\n";
        return s
    }

    private fun gravarPlanetesNodesLilithParts(sw: SwissEph, sd: SweDate): String {
        val xp = DoubleArray(6)
        var grausNN = 0.0
        var s = ""
        val serr = StringBuffer()

        val planetes = intArrayOf(
            SweConst.SE_SUN,
            SweConst.SE_MOON,
            SweConst.SE_MERCURY,
            SweConst.SE_VENUS,
            SweConst.SE_MARS,
            SweConst.SE_JUPITER,
            SweConst.SE_SATURN,
            SweConst.SE_URANUS,
            SweConst.SE_NEPTUNE,
            SweConst.SE_PLUTO,
            SweConst.SE_MEAN_NODE,
            // Black Moon Lilith (mitja)
            SweConst.SE_MEAN_APOG,
            // Black Moon Lilith (true)
            //SweConst.SE_OSCU_APOG
            // a carta-natal té els mateixos orbes que els planetes
            SweConst.SE_CHIRON
        )

        val flags = SweConst.SEFLG_SWIEPH or
                //SweConst.SEFLG_SIDEREAL |
                //SweConst.SEFLG_NONUT |  // util per sideral
                SweConst.SEFLG_TOPOCTR or // però jo faig geocèntric
                SweConst.SEFLG_SPEED      // directe o retrògrad

        for (pl in planetes.indices) {
            val planet = planetes[pl]
            var nomPlaneta = sw.swe_get_planet_name(planet)
            var tipus: String

            val ret = sw.swe_calc_ut(
                sd.julDay,
                planet,
                flags,
                xp,
                serr
            )

            if (ret != flags) {
                if (serr.length > 0) {
                    System.err.println("Compte!: $serr")
                } else {
                    System.err.println(
                        String.format("Compte, s'utilitzen diferents opcions (0x%x)", ret)
                    )
                }
            }

            when (nomPlaneta) {
                "mean Apogee" -> {
                    nomPlaneta = "Lilith"
                    tipus = nomPlaneta
                }
                "mean Node" -> {
                    nomPlaneta = "Node Nord"
                    grausNN = xp[0]
                    tipus = "Node"
                }
                else -> {tipus = "Planeta"}
            }

            persona.setEntitat(nomPlaneta,
                xp[0],
                if (xp[3] < 0) "R" else "D",
                tipus,
                calcularCasa(xp[0])
                )

            s += String.format(
                "%s %s %s %s\n",
                nomPlaneta,
                aGMS(xp[0],true),
                calcularCasa(xp[0]),
                if (xp[3] < 0) "R" else "D"
            )
        }

        // Fortuna / Infortuni
        val casaSol = persona.entitats[persona.entitats.indexOf(
            persona.entitats.first {it.nom == "Sun"})].casa
        if (casaSol == "C7" ||
            casaSol == "C8" ||
            casaSol == "C9" ||
            casaSol == "C10" ||
            casaSol == "C11" ||
            casaSol == "C12") {
            // diurn: Fortuna = Ascendent + Lluna - Sol / Infortuni = Ascendent + Mart - Saturn
            grauFortuna = persona.entitats[persona.entitats.indexOf(
                persona.entitats.first {it.nom == "AC"})].grau +
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Moon"})].grau -
                persona.entitats[persona.entitats.indexOf(
                persona.entitats.first {it.nom == "Sun"})].grau
            grauInfortuni = persona.entitats[persona.entitats.indexOf(
                persona.entitats.first {it.nom == "AC"})].grau +
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Mars"})].grau -
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Saturn"})].grau
        }
        else {
            // nocturn: Fortuna = Ascendent + Sol - Lluna / Infortuni = Ascendent + Saturn - Mart
            grauFortuna = persona.entitats[persona.entitats.indexOf(
                persona.entitats.first {it.nom == "AC"})].grau +
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Sun"})].grau -
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Moon"})].grau
            grauInfortuni = persona.entitats[persona.entitats.indexOf(
                persona.entitats.first {it.nom == "AC"})].grau +
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Saturn"})].grau -
                    persona.entitats[persona.entitats.indexOf(
                        persona.entitats.first {it.nom == "Mars"})].grau
        }

        // grava Fortuna
        persona.setEntitat("Fortuna",
            grauFortuna,
            " ",
            "Part",
            calcularCasa(grauFortuna))

        s += String.format(
            "%s %s %s\n",
            "Fortuna",
            aGMS(grauFortuna,true),
            calcularCasa(grauFortuna)
        )

        // grava Infortuni
        persona.setEntitat("Infortuni",
            grauInfortuni,
            " ",
            "Part",
            calcularCasa(grauInfortuni))

        s += String.format(
            "%s %s %s\n",
            "Infortuni",
            aGMS(grauInfortuni,true),
            calcularCasa(grauInfortuni)
        )

        // Node Sud
        xp[0] = (grausNN + 180.0) % 360

        persona.setEntitat("Node Sud",
            xp[0],
            "R",
            "Node",
            calcularCasa(xp[0]))

        s += String.format(
            "%s %s %s %s\n",
            "Node Sud",
            aGMS(xp[0],true),
            calcularCasa(xp[0]),
            if (xp[3] < 0) "R" else "D"
        )

        return s
    }

    private fun gravarAsteroides(sw: SwissEph, sd: SweDate): String {
        val xp = DoubleArray(6)
        var s = ""
        val serr = StringBuffer()
        /*
        val asteroides = intArrayOf(
            // Alma
            390,
            // Amor
            1221,
            // Aphrodite
            1388,
            // Apollo: different from Witte's Apollon
            1862,
            // Child
            4580,
            // Cruithne: "second moon" of earth
            3753,
            // Cupido: different from Witte's Cupido
            763,
            // Damocles: highly eccentric orbit betw. Mars and Uranus
            3553,
            // Eris
            136199,
            // Eros
            433,
            // Hidalgo
            944,
            // Ixion
            28978,
            // Kama
            1387,
            // Lilith: not identical with Dark Moon 'Lilith'
            1181,
            // Nessus: third named Centaur (beween Saturn and Pluto)
            7066,
            // Orcus
            90482,
            // Poseidon: Greek Neptune (different from Witte's Poseidon)
            4341,
            // Quaoar
            50000,
            // Sedna
            90377,
            // Valentine
            447,
            // Varuna
            20000,
            // Vulcano: fire god (different from Witte's Vulkanus and intramercurian Vulcan)
            4464,
            // Zeus: Greek Jupiter (different from Witte's Zeus)
            5731
        )
        */

        // dono per fet que els orbes dels transneptunians són com els asteroides
        val planets = intArrayOf(
            SweConst.SE_CERES,
            SweConst.SE_PALLAS,
            SweConst.SE_JUNO,
            SweConst.SE_VESTA
            /*
            SweConst.SE_PHOLUS,
            //SweConst.SE_INTP_APOG,
            //SweConst.SE_INTP_PERG,
            SweConst.SE_CUPIDO,
            SweConst.SE_HADES,
            SweConst.SE_ZEUS,
            SweConst.SE_KRONOS,
            SweConst.SE_APOLLON,
            SweConst.SE_ADMETOS,
            SweConst.SE_VULKANUS,
            SweConst.SE_POSEIDON,
            SweConst.SE_ISIS,
            SweConst.SE_NIBIRU,
            SweConst.SE_HARRINGTON,
            SweConst.SE_NEPTUNE_LEVERRIER,
            SweConst.SE_NEPTUNE_ADAMS,
            SweConst.SE_PLUTO_LOWELL,
            SweConst.SE_PLUTO_PICKERING
            */
        )

        val flags = SweConst.SEFLG_SWIEPH or
                //SweConst.SEFLG_SIDEREAL |
                //SweConst.SEFLG_NONUT |  // util per sideral
                SweConst.SEFLG_TOPOCTR or // però jo faig geocèntric
                SweConst.SEFLG_SPEED      // directe o retrògrad

        for (pl in planets.indices) {
            val planet = planets[pl]
            var nomPlaneta = sw.swe_get_planet_name(planet)

            val ret = sw.swe_calc_ut(
                sd.julDay,
                planet,
                flags,
                xp,
                serr
            )

            if (ret != flags) {
                if (serr.length > 0) {
                    System.err.println("Compte!: $serr")
                } else {
                    System.err.println(
                        String.format("Compte, s'utilitzen diferents opcions (0x%x)", ret)
                    )
                }
            }

            persona.setEntitat(nomPlaneta,
                xp[0],
                if (xp[3] < 0) "R" else "D",
                "Asteroide",
                calcularCasa(xp[0]))

            s += String.format(
                "%s %s %s %s\n",
                nomPlaneta,
                aGMS(xp[0],true),
                calcularCasa(xp[0]),
                if (xp[3] < 0) "R" else "D"
            )
        }

        /*
        // asteroides reals
        var nomAsteroide: String
        for (i in asteroides.indices) {
            val asteroide = SE_AST_OFFSET + asteroides[i]
            nomAsteroide = sw.swe_get_planet_name(asteroide)

            when (nomAsteroide) {
                "Lilith" -> nomAsteroide = "Lilith ast."
                "? Nessus" -> nomAsteroide = "Nessus"
            }

            sw.swe_calc_ut(
                sd.julDay,
                asteroide,
                flags,
                xp,
                serr
            )

            persona.setEntitat(
            nomAsteroide,
            xp[0],
            calcularCasa(xp[0]),
            if (xp[3] < 0) "R" else "D",
            "Asteroide")

            s += String.format(
                "%s %s %s %s %s %s\n",
                nomAsteroide,
                aGMS(xp[0]),
                calcularCasa(xp[0]),
                if (xp[3] < 0) "R" else "D",
                "Asteroide",
                serr
            )
        }
        */
        return s
    }

    private fun gravarVertex(
        sw: SwissEph, sd: SweDate, latitud: Double,
        longitud: Double
    ): String {

        // cims de les cases
        val cims = DoubleArray(13)
        val punts = DoubleArray(10)
        var s = ""

        sw.swe_houses(
            sd.julDay,
            0,
            latitud,
            longitud,
            'P'.toInt(),
            cims,
            punts
        )

        s += "Vertex: " +
                aGMS(punts[3],true) + " " +
                calcularCasa(punts[3]) + "\n"

        persona.setEntitat("Vertex",
            punts[3],
            " ",
            "Vertex",
            calcularCasa(punts[3]))
        return s
    }

    /*
    private fun gravarEstrellesFixes(sw: SwissEph, sd: SweDate): String {

        var i: Int
        val xfs = DoubleArray(6)
        val serr = StringBuffer()
        val estrella = StringBuffer()
        var s = ""
        val estrelles = arrayOf(
            "Acrux",
            "Aldebaran",
            "Algol",
            "Altair",
            "Antares",
            "Arcturus",
            "Betelgeuse",
            "Capella",
            "Castor",
            "Deneb",
            "Denebola",
            "Fomalhaut",
            "Gal. Center",
            "Pollux",
            "Procyon",
            "Ras Algethi",
            "Regulus",
            "Rigel",
            "Sirius",
            "Spica",
            "Vega"
        )

        for (i in 0 until estrelles.size) {
            estrella.delete(0, estrella.length)
            estrella.append(estrelles[i])

            sw.swe_fixstar_ut(estrella,
                sd.julDay,
                1,
                xfs,
                serr)
            /*
            System.err.println("estrella: " + estrella);
            System.err.println("xfs[0]: " + xfs[0]);
            System.err.println("xfs[1]: " + xfs[1]);
            System.err.println("xfs[2]: " + xfs[2]);
            System.err.println("serr: " + serr);
            */
            s += String.format("%s %s %f %f %s %s\n",
                estrella,
                aGMS(xfs[0]),
                calcularCasa(xfs[0]),
                "Estrella",
                serr)

            persona.setEntitat(estrella.toString(),
            xfs[0],
            calcularCasa(xfs[0]),
            " ",
            "Estrella")
        }
        return s
    }
    */

    private fun calcularCasa(grau: Double): String {
        var casa = ""
        loop@ for (i in persona.entitats.indexOf(persona.entitats.first {it.nom == "AC"})
            .. persona.entitats.indexOf(persona.entitats.first {it.nom == "C12"})-1) {
            if (persona.entitats[i].grau > persona.entitats[i+1].grau &&
                (grau >= persona.entitats[i].grau ||
                        grau < persona.entitats[i+1].grau)) {
                casa = persona.entitats[i].nom
                break@loop
            }
            if (persona.entitats[i].grau < persona.entitats[i+1].grau &&
                (grau >= persona.entitats[i].grau &&
                        grau < persona.entitats[i+1].grau)) {
                casa = persona.entitats[i].nom
                break@loop
            }
        }
        if (casa == "") {casa = "C12"}
        if (casa == "AC") {casa = "C1"}
        if (casa == "IC") {casa = "C4"}
        if (casa == "DC") {casa = "C7"}
        if (casa == "MC") {casa = "C10"}

        return casa
    }

    fun gravarPersona(ruta: String, nomFitxer: String, dades: String) {
        val fitxer = File(ruta, nomFitxer)
        val osw = OutputStreamWriter(FileOutputStream(fitxer))
        osw.write(dades)
        osw.flush()
        osw.close()
        // fitxer SER
        val fitxerSER = FileOutputStream(tRutaPersones + "/" + nomFitxer + ".ser")
        val oos = ObjectOutputStream(fitxerSER)
        oos.writeObject(Json.stringify(Persona.serializer(), persona))
        oos.close()
        fitxerSER.close()
    }

    private fun aGMS(d: Double, calcularSigne: Boolean): String {
        val signes = listOf(
            "Aries",
            "Taure",
            "Bessons",
            "Cranc",
            "Leo",
            "Verge",
            "Balança",
            "Escorpí",
            "Sagitari",
            "Capricorn",
            "Aquari",
            "Peixos"
        )

        var g = d
        g += 0.5 / 3600.0 / 10000.0
        var deg = g.toInt()
        g = (g - deg) * 60
        val min = g.toInt()
        g = (g - min) * 60
        val sec = g.toInt()

        if (calcularSigne) {
            val index = ((deg - (deg % 30)) / 30)
            val signe = signes[index]
            deg -= (index * 30)
            return String.format("%3d°%02d'%02d\" %s", deg, min, sec, signe)
        }
        else {
            return String.format("%3d°%02d'%02d\"", deg, min, sec)
        }
    }
}
