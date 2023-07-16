package com.aldarius.horoscop.transits

import androidx.lifecycle.ViewModel
import com.aldarius.horoscop.common.Aspecte
import com.aldarius.horoscop.common.Entitat
import com.aldarius.horoscop.common.Persona
import com.aldarius.horoscop.common.Posicio
import com.aldarius.horoscop.common.Posicions
import java.io.*
import java.util.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import kotlin.collections.HashMap
import kotlin.math.abs


class TransitsViewModel : ViewModel() {

    lateinit var sw: SwissEph
    lateinit var sd: SweDate
    lateinit var persona: Persona
    var posicions = Posicions()
    val transit = Persona()
    val flags = SweConst.SEFLG_SWIEPH or
            //SweConst.SEFLG_SIDEREAL |
            //SweConst.SEFLG_NONUT |  // util per sideral
            SweConst.SEFLG_TOPOCTR or // però jo faig geocèntric
            SweConst.SEFLG_SPEED      // directe o retrògrad
    // TODO anar modificant marge si falla d'un segon angular (precisió anterior era 0,000000277)
    val marge = 0.000000777
    private var h = 0
    private var m = 0
    private var s = 0
    private var vLatitud = 0.0
    private var vLongitud = 0.0
    private var vAltitud = 0.0
    private var grauFortuna = 0.0
    private var grauInfortuni = 0.0
    private var asp = ArrayList<Aspecte>()
    private var dh = Calendar.getInstance(TimeZone.getDefault())

    // TODO possibilitat de fixar orbes a configuració
    private val orbes: Map<Double, Double> = object : HashMap<Double, Double>() {
        init {
            put(0.0, 3.0)
            put(60.0, 1.0)
            put(300.0, 1.0)
            put(90.0, 2.0)
            put(270.0, 2.0)
            put(120.0, 1.0)
            put(240.0,1.0)
            put(180.0, 2.0)
        }
    }

    private val velocitats: Map<String, Double> = object: HashMap<String, Double>() {
        init {
            put("Mercury", 4.09)
            put("Venus", 1.6)
            put("Mars", 0.52)
            put("Jupiter", 0.08)
            put("Saturn", 0.03)
            put("Uranus", 0.012)
            put("Neptune", 0.0077)
            put("Pluto", 0.00397)
            put("Chiron",0.3)
            put("Ceres",0.3)
            put("Pallas",0.3)
            put("Juno",0.3)
            put("Vesta",0.3)
        }
    }

    private val aspectes: Map<Double, String> = object : HashMap<Double, String>() {
        init {
            put(0.0, "conjuncio")
            put(60.0, "sextil")
            put(300.0, "sextil")
            put(90.0, "quadratura")
            put(270.0, "quadratura")
            put(120.0, "trigon")
            put(240.0, "trigon")
            put(180.0, "oposicio")
        }
    }

    fun fitxerPosicions(ruta: String) {
        try {
            val fitxer = File(ruta + "/posicions.ser")
            if (fitxer.exists()) {
                val fitxerSER = FileInputStream(ruta + "/posicions.ser")
                val ois = ObjectInputStream(fitxerSER)
                // oPosicions és el contingut del JSON
                val oPosicions = ois.readObject() as String
                // p és la instància de l'objecte Posicions
                //posicions = Json.parse(Posicions.serializer(), oPosicions)
                ois.close()
                fitxerSER.close()
                posicions.posicions = gravarPosicions(
                    String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        dh.get(Calendar.YEAR) - 3,
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    ),
                    String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        dh.get(Calendar.YEAR) + 3,
                        dh.get(Calendar.MONTH),
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    ),
                    ruta)
            }
            else {
                posicions.posicions = gravarPosicions(
                    String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        dh.get(Calendar.YEAR) - 3,
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    ),
                    String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        dh.get(Calendar.YEAR) + 3,
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    ),
                    ruta)
                val fitxerSER = FileOutputStream(ruta + "/posicions.ser")
                val oos = ObjectOutputStream(fitxerSER)
                //oos.writeObject(Json.stringify(Posicions.serializer(), posicions))
                oos.close()
                fitxerSER.close()
                println("Posicions gravades")
            }
        } catch (i: IOException) {
            i.printStackTrace()
        } catch (c: ClassNotFoundException) {
            println("No s\'ha trobat el fitxer de posicions")
            c.printStackTrace()
        }
    }

    private fun gravarPosicions(momentInicial: String,
                                momentFinal: String,
                                ruta: String): ArrayList<Posicio> {
        // crea un fitxer intermedi pels planetes que es poden posar retrògrads, i també cada vegada
        // que passen de Peixos a Àries (Quiró ha estat realment divertit!)
        var posicions: ArrayList<Posicio> = ArrayList()
        var nd: SweDate?
        var dhN: String
        var dataActual : String
        var nomPlaneta: String
        var nouEstat: String
        var hora: Double
        var grauAnterior: Double
        var velocitatAnterior: Double
        val xp = DoubleArray(6)
        var sortir: Boolean
        var grau0: Boolean
        var canviVelocitat: Boolean
        var mesAcotat: Boolean
        var setmanaAcotada: Boolean
        var diaAcotat: Boolean
        var horaAcotada: Boolean
        var minutAcotat: Boolean
        var saltPerSegons: Boolean
        var increment: Int
        var posicioActual: Posicio
        var filtre: List<Posicio>

        val planetes = listOf(
            SweConst.SE_MERCURY,
            SweConst.SE_VENUS,
            SweConst.SE_MARS,
            SweConst.SE_JUPITER,
            SweConst.SE_SATURN,
            SweConst.SE_URANUS,
            SweConst.SE_NEPTUNE,
            SweConst.SE_PLUTO,
            SweConst.SE_CHIRON,
            SweConst.SE_CERES,
            SweConst.SE_PALLAS,
            SweConst.SE_JUNO,
            SweConst.SE_VESTA
        )

        sw = SwissEph(ruta)
        // Les dades varien depenent de les coordenades, així que la base serà
        // el meu lloc de naixement ("¡Porque yo lo valgo!")
        sw.swe_set_topo(41.4136111, 2.1425, 129.0)

        for (planet in planetes) {
            nomPlaneta = sw.swe_get_planet_name(planet)
            // Moment inicial
            filtre = posicions.filter {
                it.entitat == nomPlaneta
            }
            if (filtre.isEmpty()) {
                // en cas que no hagi cap registre informat per aquest planeta
                h = momentInicial.substring(8, 10).toInt()
                m = momentInicial.substring(10, 12).toInt()
                s = momentInicial.substring(12, 14).toInt()
                hora = h + (m / 60.0) + (s / 3600.0)
                nd = SweDate(
                    momentInicial.substring(0, 4).toInt(),
                    momentInicial.substring(4, 6).toInt(),
                    momentInicial.substring(6, 8).toInt(),
                    hora)
                posicioActual = Posicio(
                    nomPlaneta,
                    momentInicial,
                    "",
                    if (xp[3] > 0) {
                        "D"
                    } else {
                        "R"
                    },
                    "",
                    xp[0],
                    0.0,
                    0)
                posicions.add(posicioActual)
            } else {
                // si ja hi ha registres informats per aquest planeta
                posicioActual = filtre[filtre.lastIndex]
                h = posicioActual.momentFinal.substring(8, 10).toInt()
                m = posicioActual.momentFinal.substring(10, 12).toInt()
                s = posicioActual.momentFinal.substring(12, 14).toInt()
                hora = h + (m / 60.0) + (s / 3600.0)
                nd = SweDate(
                    posicioActual.momentFinal.substring(0, 4).toInt(),
                    posicioActual.momentFinal.substring(4, 6).toInt(),
                    posicioActual.momentFinal.substring(6, 8).toInt(),
                    hora)
            }
            dh.set(nd.year, nd.month, nd.day, h, m, s)
            // càlcul inicial per guardar variables
            sw.swe_calc_ut(
                nd.julDay,
                planet,
                flags,
                xp,
                null
            )

            sortir = false
            grau0 = false
            canviVelocitat = false
            mesAcotat = false
            setmanaAcotada = false
            diaAcotat = false
            horaAcotada = false
            minutAcotat = false
            saltPerSegons = false
            increment = 0

            while (!sortir) {
                dataActual = String.format(
                    "%04d%02d%02d%02d%02d%02d",
                    if(nd!!.month == 0) {dh.get(Calendar.YEAR)-1} else {dh.get(Calendar.YEAR)},
                    if(nd!!.month == 0) {12} else {dh.get(Calendar.MONTH)},
                    dh.get(Calendar.DAY_OF_MONTH),
                    dh.get(Calendar.HOUR_OF_DAY),
                    dh.get(Calendar.MINUTE),
                    dh.get(Calendar.SECOND)
                )
                if (momentFinal.toLong() - posicioActual.momentInicial.toLong() < 30000000) {
                    mesAcotat = true
                    saltPerSegons = false
                }
                if (!mesAcotat) {
                    if (dataActual > momentFinal) {
                        increment = -2
                        mesAcotat = true
                    }
                    dh.add(Calendar.MONTH, increment)
                    saltPerSegons = false
                } else {
                    if (!setmanaAcotada) {
                        if (dataActual > momentFinal) {
                            setmanaAcotada = true
                        }
                        dh.add(Calendar.WEEK_OF_YEAR, increment)
                    } else {
                        if (!diaAcotat) {
                            if (dataActual > momentFinal) {
                                diaAcotat = true
                            }
                            dh.add(Calendar.DATE, increment)
                        } else {
                            if (!horaAcotada) {
                                if (dataActual > momentFinal) {
                                    horaAcotada = true
                                }
                                dh.add(Calendar.HOUR_OF_DAY, increment)
                            } else {
                                if (!minutAcotat) {
                                    if (dataActual > momentFinal) {
                                        minutAcotat = true
                                    }
                                    dh.add(Calendar.MINUTE, increment)
                                } else {
                                    dh.add(Calendar.SECOND, increment)
                                    saltPerSegons = true
                                }
                            }
                        }
                    }
                }
                posicions[posicions.size - 1].iteracio += 1
                h = dh.get(Calendar.HOUR_OF_DAY)
                m = dh.get(Calendar.MINUTE)
                s = dh.get(Calendar.SECOND)
                hora = h + (m / 60.0) + (s / 3600.0)
                nd = SweDate(
                    dh.get(Calendar.YEAR),
                    dh.get(Calendar.MONTH),
                    dh.get(Calendar.DAY_OF_MONTH),
                    hora
                )
                grauAnterior = xp[0]
                velocitatAnterior = xp[3]
                sw.swe_calc_ut(
                    nd.julDay,
                    planet,
                    flags,
                    xp,
                    null
                )

                if (!canviVelocitat
                    && ((grauAnterior - xp[0] > 300)
                            || (grauAnterior - xp[0] < -300))) {
                    grau0 = true
                }

                if (!grau0
                    && (posicioActual.estatInicial == "R" && xp[3] > 0.0)
                    || (posicioActual.estatInicial == "D" && xp[3] < 0.0)) {
                    canviVelocitat = true
                }
                
                if (grau0 || !canviVelocitat) {
                    if (xp[0] > 350.0) {
                        increment = 1
                    } else {
                        increment = -1
                    }
                    if ((grauAnterior - xp[0] > 300)
                        || (grauAnterior - xp[0] < -300)) {
                        if (!mesAcotat) {
                            mesAcotat = true
                        } else {
                            if (!setmanaAcotada) {
                                setmanaAcotada = true
                            } else {
                                if (!diaAcotat) {
                                    diaAcotat = true
                                } else {
                                    if (!horaAcotada) {
                                        horaAcotada = true
                                    } else {
                                        if (!minutAcotat) {
                                            minutAcotat = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                if (canviVelocitat || !grau0) {
                    if ((posicioActual.estatInicial == "R" && xp[3] > 0.0)
                        || (posicioActual.estatInicial == "D" && xp[3] < 0.0)
                    ) {
                        increment = -1
                    } else {
                        increment = 1
                    }
                    if (((xp[3] > 0.0 && velocitatAnterior < 0.0)
                                || xp[3] < 0.0 && velocitatAnterior > 0.0)
                        || posicioActual.iteracio == 1
                    ) {
                        if (!mesAcotat) {
                            mesAcotat = true
                        } else {
                            if (!setmanaAcotada) {
                                setmanaAcotada = true
                            } else {
                                if (!diaAcotat) {
                                    diaAcotat = true
                                } else {
                                    if (!horaAcotada) {
                                        horaAcotada = true
                                    } else {
                                        if (!minutAcotat) {
                                            minutAcotat = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                if (saltPerSegons &&
                    // ha arribat al final del zodíac en sentit directe
                    ((xp[3] > 0.0 && grauAnterior - xp[0] > 300)
                            // ha arribat al final del zodíac en sentit retrògrad
                            || (xp[3] < 0.0 && grauAnterior - xp[0] < -300)
                            // passa de directe a retrògrad
                            || (velocitatAnterior > 0.0 && xp[3] < 0.0)
                            // passa de retrògrad a directe
                            || (velocitatAnterior < 0.0 && xp[3] > 0.0)
                            // arriba al moment final per aquest planeta
                            || dataActual > momentFinal)
                ) {
                    grau0 = false
                    canviVelocitat = false
                    mesAcotat = false
                    setmanaAcotada = false
                    diaAcotat = false
                    horaAcotada = false
                    minutAcotat = false
                    saltPerSegons = false

                    dhN = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        if(nd.month == 0) {dh.get(Calendar.YEAR)-1} else {dh.get(Calendar.YEAR)},
                        if(nd.month == 0) {12} else {dh.get(Calendar.MONTH)},
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND) + 1
                    )

                    posicioActual.estatFinal = (
                            if ((xp[3] > 0.0 && grauAnterior > 359.0 && xp[0] > 0.0 && xp[0] < 1.0)
                                || (xp[3] < 0.0 && grauAnterior > 0.0 && grauAnterior < 1.00 && xp[0] > 359.0)
                            ) {
                                posicioActual.estatInicial
                            } else {
                                if (posicioActual.estatInicial == "D") {
                                    "R"
                                } else {
                                    "D"
                                }
                            })

                    posicioActual.grauFinal = if (
                        (velocitatAnterior > 0.0 && xp[3] < 0.0)
                        || (velocitatAnterior < 0.0 && xp[3] > 0.0)
                    ) {xp[0]} else {360.0}


                    if (dhN > momentFinal) {
                        sortir = true
                        posicioActual.estatFinal = (
                                if (xp[3] > 0.0) {"D"} else {"R"}
                                )
                        posicioActual.grauFinal = xp[0]
                    }

                    posicioActual.momentFinal = dhN

                    if (!sortir) {
                        nouEstat =
                            if ((xp[3] > 0.0 && grauAnterior > 359.0 && xp[0] > 0.0 && xp[0] < 1.0)
                                || (xp[3] < 0.0 && grauAnterior > 0.0 && grauAnterior < 1.00 && xp[0] > 359.0)
                            ) {
                                posicioActual.estatInicial
                            } else {
                                if (posicioActual.estatInicial == "D") {
                                    "R"
                                } else {
                                    "D"
                                }
                            }

                        posicioActual = Posicio(
                            nomPlaneta,
                            dhN,
                            "",
                            nouEstat,
                            "",
                            if (
                                (velocitatAnterior > 0.0 && xp[3] < 0.0)
                                || (velocitatAnterior < 0.0 && xp[3] > 0.0)
                            ) {xp[0]} else {0.0},
                            0.0,
                            0
                        )
                        increment = 1
                        posicions.add(posicioActual)
                    }
                }
            }
        }
        return posicions
    }

    fun recuperarPersones(ruta: String): ArrayList<String> {
        val persones = ArrayList<String>()
        val r = File(ruta)
        val fitxers = r.listFiles()
        for (i in fitxers.indices) {
            if (fitxers[i].toString().endsWith(".ser")) {
                persones.add(
                    fitxers[i].toString().substring(
                        r.toString().length + 1,
                        fitxers[i].toString().length - 8
                    )
                )
            }
        }
        return persones
    }

    fun carregarPersona(ruta: String, nomPersona: String): Persona? {
        try {
            val fitxerSER = FileInputStream(ruta + "/" + nomPersona + ".txt.ser")
            val ois = ObjectInputStream(fitxerSER)
            // oPersona és el contingut del JSON
            val oPersona = ois.readObject() as String
            // p és la instància de l'objecte Persona
            //persona = Json.parse(Persona.serializer(), oPersona)
            ois.close()
            fitxerSER.close()
            return persona
        } catch (i: IOException) {
            i.printStackTrace()
            return null
        } catch (c: ClassNotFoundException) {
            println("No s\'ha trobat el fitxer de la persona")
            c.printStackTrace()
            return null
        }
    }

    fun calcular(
        moment: String,
        latitud: Double,
        longitud: Double,
        altitud: Double,
        zona: Double
    ): Persona {
        // Pels trànsits es retornarà un objecte Persona (el text no)

        // separem la data
        val diaN = moment.substring(0, 10)
        transit.dataNaixement = diaN
        val data: Array<String>
        var delimitador = ""
        if (diaN.contains("/")) {
            delimitador = "/"
        } else {
            if (diaN.contains("-")) {
                delimitador = "-"
            }
        }

        data = diaN.split(delimitador.toRegex(), 3).toTypedArray()
        val dia = Integer.parseInt(data[0])
        val mes = Integer.parseInt(data[1])
        val any = Integer.parseInt(data[2])

        transit.latitud = latitud
        transit.longitud = longitud
        transit.altitud = altitud
        vLatitud = latitud
        vLongitud = longitud
        vAltitud = altitud


        // separem l'hora
        val horaT = moment.substring(11, 19)
        transit.horaNaixement = horaT
        val horaN: Array<String>
        if (horaT.contains(":")) {
            delimitador = ":"
        } else {
            if (horaT.contains(".")) {
                delimitador = "."
            }
        }
        horaN = horaT.split(delimitador.toRegex(), 3).toTypedArray()
        h = Integer.parseInt(horaN[0])
        m = Integer.parseInt(horaN[1])
        s = Integer.parseInt(horaN[2])

        transit.zona = zona

        // 12:45 - 1h per la zona horària (+01:00)
        // double hora = 12 + 45.0 / 60.0 - 1
        val hora = h + m / 60.0 + s / 3600.0 - zona
        sd = SweDate(any, mes, dia, hora)
        sw.swe_set_topo(longitud, latitud, altitud)

        gravarData(transit, sd)
        gravarCoordenades(transit, longitud, latitud, altitud)
        gravarPlanetesNodesLilith(transit, sw, sd)
        gravarAsteroides(transit, sw, sd)
        gravarCases(transit, sw, sd, latitud, longitud)
        //gravarVertex(transit, sw, sd, latitud, longitud)
        //gravarEstrellesFixes(transit, sw, sd)

        return transit
    }

    private fun gravarData(t: Persona, sd: SweDate) {
        val hora = sd.hour.toInt()
        val min = ((sd.hour - hora) * 60).toInt()
        val sec = (((sd.hour - hora) * 60 - min) * 60).toInt()

        t.dataNaixement = String.format(
            "%4d-%02d-%02d",
            sd.year,
            sd.month,
            sd.day
        )

        t.horaNaixement = String.format(
            "%d:%02d:%02dh UTC\n",
            hora,
            min,
            sec
        )
    }

    private fun gravarCoordenades(t: Persona, longitud: Double, latitud: Double, altitud: Double) {
        t.latitud = latitud
        t.longitud = longitud
        t.altitud = altitud
        println("Latitud: $latitud")
        println("Longitud: $longitud")
        println("Altitud: $altitud")
    }

    private fun gravarPlanetesNodesLilith(t: Persona, sw: SwissEph, sd: SweDate) {
        val xp = DoubleArray(6)
        var grausNN = 0.0
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

        for (pl in planetes.indices) {
            val planet = planetes[pl]
            var nomPlaneta = sw.swe_get_planet_name(planet)
            var tipus: String

            val ret = sw.swe_calc_ut(
                sd.julDay,
                planet,
                flags,
                // array of 6 doubles for longitude, latitude, distance, speed in long.,
                // speed in lat., and speed in dist.
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

            t.setEntitat(nomPlaneta,
                xp[0],
                if (xp[3] < 0) "R" else "D",
                tipus,
                "")
        }

        // Node Sud
        xp[0] = (grausNN + 180.0) % 360

        t.setEntitat("Node Sud",
            xp[0],
            "R",
            "Node",
            "")
    }

    private fun gravarAsteroides(t: Persona, sw: SwissEph, sd: SweDate) {
        val xp = DoubleArray(6)
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

            t.setEntitat(nomPlaneta,
                xp[0],
                if (xp[3] < 0) "R" else "D",
                "Asteroide",
                "")

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

            t.setEntitat(nomAsteroide, xp[0], if (xp[3] < 0) "R" else "D", "Asteroide")

        }
        */
    }


    private fun gravarCases(
        t: Persona, sw: SwissEph, sd: SweDate, latitud: Double,
        longitud: Double
    ) {

        // cims de les cases
        val cims = DoubleArray(13)
        val punts = DoubleArray(10)

        sw.swe_houses(
            sd.julDay,
            0,
            latitud,
            longitud,
            'P'.toInt(),
            cims,
            punts
        )

        t.setEntitat("AC", punts[0], " ", "Angle", "")
        t.setEntitat("C2", cims[2], " ", "Cim", "")
        t.setEntitat("C3", cims[3], " ", "Cim", "")
        t.setEntitat("IC", cims[4], " ", "Angle", "")
        t.setEntitat("C5", cims[5], " ", "Cim", "")
        t.setEntitat("C6", cims[6], " ", "Cim", "")
        t.setEntitat("DC", cims[7], " ", "Angle", "")
        t.setEntitat("C8", cims[8], " ", "Cim", "")
        t.setEntitat("C9", cims[9], " ", "Cim", "")
        t.setEntitat("MC", punts[1], " ", "Angle", "")
        t.setEntitat("C11", cims[11], " ", "Cim", "")
        t.setEntitat("C12", cims[12], " ", "Cim", "")
        //s += "armc: " + aGMS(punts[2]) + "\n";
        //s += "ascendent equatorial: " + aGMS(punts[4]) + "\n";
        //s += "co-ascendent (Walter Koch): " + aGMS(punts[5]) + "\n";
        //s += "co-ascendent (Michael Munkasey): " + aGMS(punts[6]) + "\n";
        //s += "ascendent polar (Michael Munkasey): " + aGMS(punts[7]) + "\n";
    }

    internal fun buscarAspectes(persona: Persona, actuals: Persona): ArrayList<Aspecte> {
        var d: Double
        var marcaDeTemps: List<String>

        for (ePersona in persona.entitats.iterator()) {
            for (eActual in actuals.entitats.iterator()) {
                if (esUnAspecteValid(ePersona,eActual)) {
                    d = distancia(ePersona.grau, eActual.grau)
                    for ((key, value) in orbes) {
                        if (abs(key - d) <= value) {
                            val aspecte = Aspecte(ePersona,
                                key,
                                eActual,
                                d - key,
                                "",
                                "",
                                "",
                                "",
                                0.0)
                            marcaDeTemps = aspecteExacte(aspecte)
                            aspecte.apliSepa = marcaDeTemps[0]
                            if (aspecte.apliSepa != "P") {
                                aspecte.momentExacte = marcaDeTemps[1]
                                aspecte.eActual.casa = marcaDeTemps[2]
                                aspecte.grauExacteActual = marcaDeTemps[3].toDouble()
                                aspecte.momentInicial = marcaDeTemps[4]
                                aspecte.momentFinal = marcaDeTemps[5]
                            }
                            else {
                                aspecte.momentExacte = marcaDeTemps[1]
                                aspecte.eActual.casa = calcularCasa(eActual.grau, persona)
                                aspecte.grauExacteActual = aspecte.eActual.grau
                                aspecte.momentInicial = marcaDeTemps[4]
                                aspecte.momentFinal = marcaDeTemps[5]
                            }
                            asp.add(aspecte)
                        }
                    }
                }
            }
        }


        if (asp.filter {
                it.eActual.nom == "Fortuna"
                        || it.eActual.nom == "Infortuni"}.isEmpty()) {
            dh = Calendar.getInstance(TimeZone.getDefault())
            val momentInicial = String.format(
                "%04d%02d%02d%02d%02d%02d",
                dh.get(Calendar.YEAR),
                (dh.get(Calendar.MONTH) + 1),
                dh.get(Calendar.DAY_OF_MONTH),
                dh.get(Calendar.HOUR_OF_DAY),
                dh.get(Calendar.MINUTE),
                dh.get(Calendar.SECOND)
            )
            parts(momentInicial)
        } else {
            val maxMomentFinal = asp.filter {
                it.eActual.nom == "Fortuna"
                        || it.eActual.nom == "Infortuni"}.maxBy { it.momentFinal }!!.momentFinal
            parts(maxMomentFinal)
        }

        return asp
    }
    /*
    coordenades de Google:
    Latitud: 37:25:19 N
    Longitud: 122:05:02 W
    -08:00
    L'hora del nd és UT
    */

    private fun esUnAspecteValid(ePersona: Entitat, eActual: Entitat): Boolean {
        var esValid = true
        if (ePersona.tipus == "Cim"
            || eActual.tipus == "Cim"
            || eActual.tipus == "Angle"
            || eActual.tipus == "Estrella"
            || eActual.tipus == "Vertex"
            || eActual.tipus == "Node") {
            esValid = false
        }
        return esValid
    }

    private fun aspecteExacte(aspecte: Aspecte): List<String> {
        // per saber quan el moment inicial / exacte / final d'un aspecte
        var nd : SweDate?
        val marcaDeTemps = ArrayList<String>()
        var increment: Int
        var segonsDelPeriode: Long
        var dhN: String
        var factor1 = 0.0
        val orbe: Double = orbes[aspecte.tipusAspecte] ?: error("")
        var hora: Double
        var grauObjectiu: Double = if (aspecte.ePersona.grau >= aspecte.eActual.grau) {
            aspecte.ePersona.grau - aspecte.tipusAspecte
        } else {
            aspecte.ePersona.grau + aspecte.tipusAspecte
        }
        var planet = 0
        var sortir: Boolean
        val xp = DoubleArray(6)
        var filtre: List<Posicio>
        var posicio: Posicio?
        lateinit var momentIni: GregorianCalendar
        lateinit var momentFi: GregorianCalendar

        when (aspecte.eActual.nom) {
            "Sun" -> {
                planet = SweConst.SE_SUN
            }
            "Moon" -> {
                planet = SweConst.SE_MOON
            }
            "Mercury" -> {
                planet = SweConst.SE_MERCURY
            }
            "Venus" -> {
                planet = SweConst.SE_VENUS
            }
            "Mars" -> {
                planet = SweConst.SE_MARS
            }
            "Jupiter" -> {
                planet = SweConst.SE_JUPITER
            }
            "Saturn" -> {
                planet = SweConst.SE_SATURN
            }
            "Uranus" -> {
                planet = SweConst.SE_URANUS
            }
            "Neptune" -> {
                planet = SweConst.SE_NEPTUNE
            }
            "Pluto" -> {
                planet = SweConst.SE_PLUTO
            }
            "Node Nord" -> {
                // Node Nord
                planet = SweConst.SE_MEAN_NODE
            }
            "Node Sud" -> {
                // Node Sud
                planet = SweConst.SE_MEAN_NODE
            }
            "Lilith" -> {
                // Black Moon Lilith (mitja)
                planet = SweConst.SE_MEAN_APOG
            }
            "Chiron" -> {
                planet = SweConst.SE_CHIRON
            }
        }

        dh.isLenient = false
        // 1. Moment exacte
        dh = Calendar.getInstance(TimeZone.getDefault())

        if (grauObjectiu < 0.0) {
            grauObjectiu += 360.0
        }
        if (grauObjectiu > 360.0) {
            grauObjectiu -= 360.0
        }

        // busquem al fitxer de posicions
        if (aspecte.eActual.nom != "Sun" &&
            aspecte.eActual.nom != "Moon" &&
                aspecte.eActual.nom != "Lilith") {
            filtre = posicions.posicions.filter {
                it.entitat == aspecte.eActual.nom &&
                        ((it.estatInicial == "D" &&
                                grauObjectiu >= it.grauInicial &&
                                grauObjectiu <= it.grauFinal) ||
                                (it.estatInicial == "R" &&
                                        grauObjectiu <= it.grauInicial &&
                                        grauObjectiu >= it.grauFinal))
            }
            if (filtre.size < 3) {
                posicio = filtre[filtre.lastIndex]
            } else {
                val ara = String.format(
                "%04d%02d%02d%02d%02d%02d",
                if(dh.get(Calendar.MONTH) == 0) {dh.get(Calendar.YEAR)-1} else {dh.get(Calendar.YEAR)},
                if(dh.get(Calendar.MONTH) == 0) {12} else {dh.get(Calendar.MONTH) + 1},
                dh.get(Calendar.DAY_OF_MONTH),
                dh.get(Calendar.HOUR_OF_DAY),
                dh.get(Calendar.MINUTE),
                dh.get(Calendar.SECOND)
                )
                filtre = filtre.filter {
                    it.momentInicial <= ara
                }
                posicio = filtre[filtre.lastIndex]
            }

            momentIni = GregorianCalendar(
                posicio.momentInicial.substring(0, 4).toInt(),
                posicio.momentInicial.substring(4, 6).toInt() - 1,
                posicio.momentInicial.substring(6, 8).toInt(),
                posicio.momentInicial.substring(8, 10).toInt(),
                posicio.momentInicial.substring(10, 12).toInt(),
                posicio.momentInicial.substring(12, 14).toInt()
            )
            momentFi = GregorianCalendar(
                posicio.momentFinal.substring(0, 4).toInt(),
                posicio.momentFinal.substring(4, 6).toInt() - 1,
                posicio.momentFinal.substring(6, 8).toInt(),
                posicio.momentFinal.substring(8, 10).toInt(),
                posicio.momentFinal.substring(10, 12).toInt(),
                posicio.momentFinal.substring(12, 14).toInt()
            )
            dh = momentIni
            segonsDelPeriode = (momentFi.timeInMillis - momentIni.timeInMillis) / 1000
            factor1 = (grauObjectiu - posicio.grauInicial) * segonsDelPeriode /
                    (posicio.grauFinal - posicio.grauInicial)
        } else {
            if (aspecte.eActual.nom == "Sun") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000011408
            }
            if (aspecte.eActual.nom == "Moon") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000152513
            }
            if (aspecte.eActual.nom == "Lilith") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000001268
            }
        }

        dh.add(Calendar.SECOND, factor1.toInt())

        hora = dh.get(Calendar.HOUR_OF_DAY) +
                (dh.get(Calendar.MINUTE) / 60.0) +
                (dh.get(Calendar.SECOND) / 3600.0)
        nd = SweDate(
            dh.get(Calendar.YEAR),
            dh.get(Calendar.MONTH) + 1,
            dh.get(Calendar.DAY_OF_MONTH),
            hora)
        sw.swe_calc_ut(
            nd.julDay,
            planet,
            flags,
            // array of 6 doubles for longitude, latitude, distance,
            // speed in long., speed in lat., and speed in dist.
            xp,
            null)

        if (abs(aspecte.distancia) >= marge) {
            sortir = false
            while (!sortir) {
                increment = ((grauObjectiu - xp[0]) / (xp[3] / 86400)).toInt()
                if (increment != 0) {
                    dh.add(Calendar.SECOND, increment)

                    hora = dh.get(Calendar.HOUR_OF_DAY) +
                            (dh.get(Calendar.MINUTE) / 60.0) +
                            (dh.get(Calendar.SECOND) / 3600.0)
                    nd = SweDate(
                        dh.get(Calendar.YEAR),
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        hora
                    )
                    sw.swe_calc_ut(
                        nd.julDay,
                        planet,
                        flags,
                        xp,
                        null
                    )
                }

                if ((abs(aspecte.ePersona.grau - xp[0] - aspecte.tipusAspecte)
                            < marge) || increment == 0
                ) {
                    // TODO caldria comprovar la zona horaria al mòbil (està en UTC)


                    val araN = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        sd!!.year,
                        sd.month,
                        sd.day,
                        h,
                        m,
                        s
                    )

                    dhN = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        nd!!.year,
                        nd.month,
                        nd.day,
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    )

                    if (araN < dhN) {
                        marcaDeTemps.add("A")
                    } else {
                        marcaDeTemps.add("S")
                    }

                    marcaDeTemps.add(dhN)
                    marcaDeTemps.add(calcularCasa(xp[0], persona))
                    marcaDeTemps.add(xp[0].toString())
                    if (aspecte.eActual.retrograd == "D" && xp[3] < 0) {
                        aspecte.eActual.retrograd = "R"
                    }
                    if (aspecte.eActual.retrograd == "R" && xp[3] > 0) {
                        aspecte.eActual.retrograd = "D"
                    }
                    sortir = true
                }
            }
        }

        // 2. Moment inicial
        dh = Calendar.getInstance(TimeZone.getDefault())
        grauObjectiu -= orbe
        if (grauObjectiu < 0.0) {
            grauObjectiu += 360.0
        }

        if (aspecte.eActual.nom != "Sun" &&
            aspecte.eActual.nom != "Moon" &&
            aspecte.eActual.nom != "Lilith") {
            filtre = posicions.posicions.filter {
                it.entitat == aspecte.eActual.nom &&
                        ((it.estatInicial == "D" &&
                                grauObjectiu >= it.grauInicial &&
                                grauObjectiu <= it.grauFinal) ||
                                (it.estatInicial == "R" &&
                                        grauObjectiu <= it.grauInicial &&
                                        grauObjectiu >= it.grauFinal))
            }
            if (filtre.size < 3) {
                posicio = filtre[filtre.lastIndex]
            } else {
                val ara = String.format(
                    "%04d%02d%02d%02d%02d%02d",
                    if(dh.get(Calendar.MONTH) == 0) {dh.get(Calendar.YEAR)-1} else {dh.get(Calendar.YEAR)},
                    if(dh.get(Calendar.MONTH) == 0) {12} else {dh.get(Calendar.MONTH) + 1},
                    dh.get(Calendar.DAY_OF_MONTH),
                    dh.get(Calendar.HOUR_OF_DAY),
                    dh.get(Calendar.MINUTE),
                    dh.get(Calendar.SECOND)
                )
                filtre = filtre.filter {
                    it.momentInicial <= ara
                }
                posicio = filtre[filtre.lastIndex]
            }

            momentIni = GregorianCalendar(
                posicio.momentInicial.substring(0, 4).toInt(),
                posicio.momentInicial.substring(4, 6).toInt() - 1,
                posicio.momentInicial.substring(6, 8).toInt(),
                posicio.momentInicial.substring(8, 10).toInt(),
                posicio.momentInicial.substring(10, 12).toInt(),
                posicio.momentInicial.substring(12, 14).toInt()
            )
            momentFi = GregorianCalendar(
                posicio.momentFinal.substring(0, 4).toInt(),
                posicio.momentFinal.substring(4, 6).toInt() - 1,
                posicio.momentFinal.substring(6, 8).toInt(),
                posicio.momentFinal.substring(8, 10).toInt(),
                posicio.momentFinal.substring(10, 12).toInt(),
                posicio.momentFinal.substring(12, 14).toInt()
            )
            dh = momentIni
            segonsDelPeriode = (momentFi.timeInMillis - momentIni.timeInMillis) / 1000
            factor1 = (grauObjectiu - posicio.grauInicial) * segonsDelPeriode /
                    (posicio.grauFinal - posicio.grauInicial)
        } else {
            posicio = null
            if (aspecte.eActual.nom == "Sun") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000011408
            }
            if (aspecte.eActual.nom == "Moon") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000152513
            }
            if (aspecte.eActual.nom == "Lilith") {
                factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000001268
            }
        }

        dh.add(Calendar.SECOND, factor1.toInt())

        hora = dh.get(Calendar.HOUR_OF_DAY) +
                (dh.get(Calendar.MINUTE) / 60.0) +
                (dh.get(Calendar.SECOND) / 3600.0)
        nd = SweDate(dh.get(Calendar.YEAR),
            dh.get(Calendar.MONTH) + 1,
            dh.get(Calendar.DAY_OF_MONTH),
            hora)
        sw.swe_calc_ut(
            nd.julDay,
            planet,
            flags,
            xp,
            null)

        if (abs(grauObjectiu - xp[0]) > marge) {
            sortir = false
            while (!sortir) {
                increment = ((grauObjectiu - xp[0]) / (xp[3] / 86400)).toInt()

                if (increment != 0) {
                    dh.add(Calendar.SECOND, increment)

                    hora = dh.get(Calendar.HOUR_OF_DAY) +
                            (dh.get(Calendar.MINUTE) / 60.0) +
                            (dh.get(Calendar.SECOND) / 3600.0)
                    nd = SweDate(dh.get(Calendar.YEAR),
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        hora)
                    sw.swe_calc_ut(
                        nd.julDay,
                        planet,
                        flags,
                        xp,
                        null)
                }

                if (abs(grauObjectiu - xp[0]) < marge &&
                            (
                                    aspecte.eActual.nom == "Sun"
                                            || aspecte.eActual.nom == "Moon"
                                            || aspecte.eActual.nom == "Lilith"
                                            || !(nd!!.year < posicio?.momentInicial!!.substring(0,4).toInt())
                            )
                    || increment == 0) {
                    dhN = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        nd!!.year,
                        nd.month,
                        nd.day,
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND))

                    marcaDeTemps.add(dhN)
                    //marcaDeTemps.add(calcularCasa(xp[0], persona))
                    sortir = true
                }
            }
        }

        // 3. Moment final
        dh = Calendar.getInstance(TimeZone.getDefault())
        grauObjectiu += orbe * 2
        if (grauObjectiu > 360.0) {
            grauObjectiu -= 360.0
        }

        if (aspecte.eActual.nom != "Sun" &&
            aspecte.eActual.nom != "Moon" &&
            aspecte.eActual.nom != "Lilith") {
                filtre = posicions.posicions.filter {
                    it.entitat == aspecte.eActual.nom &&
                            ((it.estatInicial == "D" &&
                                    grauObjectiu >= it.grauInicial &&
                                    grauObjectiu <= it.grauFinal) ||
                            (it.estatInicial == "R" &&
                                    grauObjectiu <= it.grauInicial &&
                                    grauObjectiu >= it.grauFinal))
                }
                if (filtre.size < 3) {
                    posicio = filtre[filtre.lastIndex]
                } else {
                    val ara = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        if(dh.get(Calendar.MONTH) == 0) {dh.get(Calendar.YEAR)-1} else {dh.get(Calendar.YEAR)},
                        if(dh.get(Calendar.MONTH) == 0) {12} else {dh.get(Calendar.MONTH) + 1},
                        dh.get(Calendar.DAY_OF_MONTH),
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND)
                    )
                    if (aspecte.eActual.nom != "Pluto") {
                        filtre = filtre.filter {
                            it.momentInicial >= ara ||
                                    (it.momentInicial < ara &&
                                    it.momentFinal >= ara)
                        }
                        posicio = filtre[0]
                    } else {
                        if (filtre.filter {
                                it.momentInicial >= ara
                            }.isNotEmpty()) {
                            filtre = filtre.filter {
                                it.momentInicial >= ara
                            }
                            posicio = filtre[0]
                        } else {
                            filtre = filtre.filter {
                                it.momentInicial <= ara
                            }
                            posicio = filtre[filtre.lastIndex]
                        }
                    }
                }

                momentIni = GregorianCalendar(
                    posicio.momentInicial.substring(0, 4).toInt(),
                    posicio.momentInicial.substring(4, 6).toInt() - 1,
                    posicio.momentInicial.substring(6, 8).toInt(),
                    posicio.momentInicial.substring(8, 10).toInt(),
                    posicio.momentInicial.substring(10, 12).toInt(),
                    posicio.momentInicial.substring(12, 14).toInt()
                )
                momentFi = GregorianCalendar(
                    posicio.momentFinal.substring(0, 4).toInt(),
                    posicio.momentFinal.substring(4, 6).toInt() - 1,
                    posicio.momentFinal.substring(6, 8).toInt(),
                    posicio.momentFinal.substring(8, 10).toInt(),
                    posicio.momentFinal.substring(10, 12).toInt(),
                    posicio.momentFinal.substring(12, 14).toInt()
                )
                dh = momentIni
                segonsDelPeriode = (momentFi.timeInMillis - momentIni.timeInMillis) / 1000
                factor1 = (grauObjectiu - posicio.grauInicial) * segonsDelPeriode /
                        (posicio.grauFinal - posicio.grauInicial)
            } else {
                posicio = null
                if (aspecte.eActual.nom == "Sun") {
                    factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000011408
                }
                if (aspecte.eActual.nom == "Moon") {
                    factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000152513
                }
                if (aspecte.eActual.nom == "Lilith") {
                    factor1 = (grauObjectiu - aspecte.eActual.grau) / 0.000001268
                }
            }

        dh.add(Calendar.SECOND, factor1.toInt())

        hora = dh.get(Calendar.HOUR_OF_DAY) +
                (dh.get(Calendar.MINUTE) / 60.0) +
                (dh.get(Calendar.SECOND) / 3600.0)
        nd = SweDate(
            dh.get(Calendar.YEAR),
            dh.get(Calendar.MONTH) + 1,
            dh.get(Calendar.DAY_OF_MONTH),
            hora
        )
        sw.swe_calc_ut(
            nd.julDay,
            planet,
            flags,
            xp,
            null)

        if (abs(grauObjectiu - xp[0]) > marge) {
            sortir = false
            while (!sortir) {
                increment = ((grauObjectiu - xp[0]) / (xp[3] / 86400)).toInt()

                if (increment != 0) {
                    dh.add(Calendar.SECOND, increment)

                    hora = dh.get(Calendar.HOUR_OF_DAY) +
                            (dh.get(Calendar.MINUTE) / 60.0) +
                            (dh.get(Calendar.SECOND) / 3600.0)
                    nd = SweDate(
                        dh.get(Calendar.YEAR),
                        dh.get(Calendar.MONTH) + 1,
                        dh.get(Calendar.DAY_OF_MONTH),
                        hora
                    )
                    sw.swe_calc_ut(
                        nd.julDay,
                        planet,
                        flags,
                        xp,
                        null
                    )
                }

                if (abs(grauObjectiu - xp[0]) < marge &&
                    (
                            aspecte.eActual.nom == "Sun"
                                    || aspecte.eActual.nom == "Moon"
                                    || aspecte.eActual.nom == "Lilith"
                                    || !(nd!!.year < posicio?.momentInicial!!.substring(0,4).toInt())
                            )
                    || increment == 0) {
                    dhN = String.format(
                        "%04d%02d%02d%02d%02d%02d",
                        nd!!.year,
                        nd.month,
                        nd.day,
                        dh.get(Calendar.HOUR_OF_DAY),
                        dh.get(Calendar.MINUTE),
                        dh.get(Calendar.SECOND))

                    marcaDeTemps.add(dhN)
                    //marcaDeTemps.add(calcularCasa(xp[0], persona))
                    sortir = true
                }
            }
        }

        return marcaDeTemps
    }

    fun parts(momentInicial: String) {
        // Trànsits de Fortuna / Infortuni durant 1 dia
        var nd: SweDate?
        var aspecte: Aspecte
        var hora: Double
        var d: Double
        val xp = DoubleArray(6)
        val cims = DoubleArray(13)
        val punts = DoubleArray(10)

        val momentIni = GregorianCalendar(
            momentInicial.substring(0, 4).toInt(),
            momentInicial.substring(4, 6).toInt() - 1,
            momentInicial.substring(6, 8).toInt(),
            momentInicial.substring(8, 10).toInt(),
            momentInicial.substring(10, 12).toInt(),
            momentInicial.substring(12, 14).toInt()
        )
        dh = momentIni

        for (i in 1..86400) {
            dh.add(Calendar.SECOND, 1)
            hora = dh.get(Calendar.HOUR_OF_DAY) +
                    (dh.get(Calendar.MINUTE) / 60.0) +
                    (dh.get(Calendar.SECOND) / 3600.0)
            nd = SweDate(
                dh.get(Calendar.YEAR),
                dh.get(Calendar.MONTH) + 1,
                dh.get(Calendar.DAY_OF_MONTH),
                hora
            )

            // Cases
            sw.swe_houses(
                nd.julDay,
                0,
                vLatitud,
                vLongitud,
                'P'.toInt(),
                cims,
                punts)

            transit.entitats.clear()
            transit.setEntitat("AC", cims[1], " ", "Angle", "")
            transit.setEntitat("C2", cims[2], " ", "Cim", "")
            transit.setEntitat("C3", cims[3], " ", "Cim", "")
            transit.setEntitat("IC", cims[4], " ", "Angle", "")
            transit.setEntitat("C5", cims[5], " ", "Cim", "")
            transit.setEntitat("C6", cims[6], " ", "Cim", "")
            transit.setEntitat("DC", cims[7], " ", "Angle", "")
            transit.setEntitat("C8", cims[8], " ", "Cim", "")
            transit.setEntitat("C9", cims[9], " ", "Cim", "")
            transit.setEntitat("MC", cims[10], " ", "Angle", "")
            transit.setEntitat("C11", cims[11], " ", "Cim", "")
            transit.setEntitat("C12", cims[12], " ", "Cim", "")

            // Sol
            sw.swe_calc_ut(
                nd.julDay,
                SweConst.SE_SUN,
                flags,
                xp,
                null)

            transit.setEntitat("Sol", xp[0], "", "", "")

            // Dia o nit?
            val casaSol = calcularCasa(xp[0], transit)

            // Lluna
            sw.swe_calc_ut(
                nd.julDay,
                SweConst.SE_MOON,
                flags,
                xp,
                null)
            transit.setEntitat("Lluna", xp[0], "", "", "")

            // Mart
            sw.swe_calc_ut(
                nd.julDay,
                SweConst.SE_MARS,
                flags,
                xp,
                null)
            transit.setEntitat("Mart", xp[0], "", "", "")

            // Saturn
            sw.swe_calc_ut(
                nd.julDay,
                SweConst.SE_SATURN,
                flags,
                xp,
                null)
            transit.setEntitat("Saturn", xp[0], "", "", "")

            if (casaSol == "C7" ||
                casaSol == "C8" ||
                casaSol == "C9" ||
                casaSol == "C10" ||
                casaSol == "C11" ||
                casaSol == "C12"
            ) {
                // diurn:
                // Fortuna = Ascendent + Lluna - Sol
                // Infortuni = Ascendent + Mart - Saturn
                grauFortuna = transit.entitats[transit.entitats.indexOf(
                    transit.entitats.first { it.nom == "AC" })].grau +
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Lluna" })].grau -
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Sol" })].grau
                grauInfortuni = transit.entitats[transit.entitats.indexOf(
                    transit.entitats.first { it.nom == "AC" })].grau +
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Mart" })].grau -
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Saturn" })].grau
            } else {
                // nocturn:
                // Fortuna = Ascendent + Sol - Lluna
                // Infortuni = Ascendent + Saturn - Mart
                grauFortuna = transit.entitats[transit.entitats.indexOf(
                    transit.entitats.first { it.nom == "AC" })].grau +
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Sol" })].grau -
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Lluna" })].grau
                grauInfortuni = transit.entitats[transit.entitats.indexOf(
                    transit.entitats.first { it.nom == "AC" })].grau +
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Saturn" })].grau -
                        transit.entitats[transit.entitats.indexOf(
                            transit.entitats.first { it.nom == "Mart" })].grau
                //println("i: " + i)
                //println("Grau AC: " + transit.entitats[transit.entitats.indexOf(
                //transit.entitats.first { it.nom == "AC" })].grau)
                //println("Grau Saturn: " + transit.entitats[transit.entitats.indexOf(
                //    transit.entitats.first { it.nom == "Saturn" })].grau)
                //println("Grau Mart: " + transit.entitats[transit.entitats.indexOf(
                //    transit.entitats.first { it.nom == "Mart" })].grau)
            }

            // grava Fortuna
            transit.setEntitat("Fortuna",
                grauFortuna,
                " ",
                "Part",
                calcularCasa(grauFortuna, persona))

            // grava Infortuni
            transit.setEntitat("Infortuni",
                grauInfortuni,
                " ",
                "Part",
                calcularCasa(grauInfortuni, persona))

            val entitats = persona.entitats.subList(
                persona.entitats.indexOf(persona.entitats.first {it.nom == "Sun"}),
                persona.entitats.indexOf(persona.entitats.first {it.nom == "Pluto"})+1)

            val parts = transit.entitats.subList(
                transit.entitats.indexOf(transit.entitats.first {it.nom == "Fortuna"}),
                transit.entitats.indexOf(transit.entitats.first {it.nom == "Infortuni"})+1)

            for (ePersona in entitats.iterator()) {
                for (ePart in parts.iterator()) {
                    d = distancia(ePersona.grau, ePart.grau)
                    for ((key, value) in orbes) {
                        if (abs(key - d) <= value) {

                            // l'aspecte ja existeix?
                            val a = asp!!.filter { it.ePersona.nom == ePersona.nom
                                    && it.eActual.nom == ePart.nom
                                    && it.momentFinal == ""
                                    && it.tipusAspecte == key}

                            if (a.isEmpty()) {
                                aspecte = Aspecte(
                                    ePersona,
                                    key,
                                    ePart,
                                    abs(key - d),
                                    "",
                                    String.format(
                                        "%04d%02d%02d%02d%02d%02d",
                                        dh.get(Calendar.YEAR),
                                        (dh.get(Calendar.MONTH) + 1),
                                        dh.get(Calendar.DAY_OF_MONTH),
                                        dh.get(Calendar.HOUR_OF_DAY),
                                        dh.get(Calendar.MINUTE),
                                        dh.get(Calendar.SECOND)
                                    ),
                                    "",
                                    "",
                                    0.0
                                )
                                asp.add(aspecte)
                            } else {
                                if (abs(key - d) < a[0].distancia) {
                                    a[0].distancia = abs(key - d)
                                    a[0].momentExacte = String.format(
                                        "%04d%02d%02d%02d%02d%02d",
                                        dh.get(Calendar.YEAR),
                                        (dh.get(Calendar.MONTH) + 1),
                                        dh.get(Calendar.DAY_OF_MONTH),
                                        dh.get(Calendar.HOUR_OF_DAY),
                                        dh.get(Calendar.MINUTE),
                                        dh.get(Calendar.SECOND)
                                    )
                                    a[0].grauExacteActual = ePart.grau
                                }
                            }
                        }
                        else {
                            // l'aspecte amb el part finalitza
                            // l'aspecte existeix?
                            val a = asp!!.filter { it.ePersona.nom == ePersona.nom
                                    && it.eActual.nom == ePart.nom
                                    && it.momentFinal == ""
                                    && it.tipusAspecte == key}

                            if (a.isNotEmpty()) {

                                a[0].momentFinal = String.format(
                                    "%04d%02d%02d%02d%02d%02d",
                                    dh.get(Calendar.YEAR),
                                    (dh.get(Calendar.MONTH) + 1),
                                    dh.get(Calendar.DAY_OF_MONTH),
                                    dh.get(Calendar.HOUR_OF_DAY),
                                    dh.get(Calendar.MINUTE),
                                    dh.get(Calendar.SECOND)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calcularCasa(grau: Double, e: Persona): String {
        var casa = ""
        loop@ for (i in e.entitats.indexOf(e.entitats.first {it.nom == "AC"})
            ..e.entitats.indexOf(e.entitats.first {it.nom == "C12"})-1) {
            if (e.entitats[i].grau > e.entitats[i+1].grau &&
                (grau >= e.entitats[i].grau ||
                        grau < e.entitats[i+1].grau)) {
                casa = e.entitats[i].nom
                break@loop
            }
            if (e.entitats[i].grau < e.entitats[i+1].grau &&
                (grau >= e.entitats[i].grau &&
                        grau < e.entitats[i+1].grau)) {
                casa = e.entitats[i].nom
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

    private fun distancia(grauPersona: Double, grauActual: Double): Double {
        var d: Double
        if (grauActual > grauPersona) {
            d = grauActual - grauPersona
        }
        else  {
            d = grauPersona - grauActual
        }
        return d
    }
/*
    private fun aGMS(d: Double): String {
        var d = d
        d += 0.5 / 3600.0 / 10000.0    // round to 1/1000 of a second
        val deg = d.toInt()
        d = (d - deg) * 60
        val min = d.toInt()
        d = (d - min) * 60
        val sec = d

        return String.format("%3d° %02d' %07.4f\"", deg, min, sec)
    }
     */
}

/*
                loop@ for (i in 1..62208000) {
                    dh.add(Calendar.SECOND, increment)
                    hora = dh.get(Calendar.HOUR_OF_DAY) +
                            (dh.get(Calendar.MINUTE) / 60.0) +
                            (dh.get(Calendar.SECOND) / 3600.0)
                    nd = SweDate(dh.get(Calendar.YEAR),
                        dh.get(Calendar.MONTH),
                        dh.get(Calendar.DAY_OF_MONTH),
                        hora)
                    sw.swe_calc_ut(
                        nd.julDay,
                        planet,
                        flags,
                        // array of 6 doubles for longitude, latitude, distance,
                        // speed in long., speed in lat., and speed in dist.
                        xp,
                        null
                    )
                    // El Node Sud no es calcula per Swisseph. Cal calcular pel Node Nord
                    // i sumar 180º
                    if (aspecte.eActual.nom == "Node Sud") {
                         xp[0] = (xp[0] + 180.0) % 360
                    }
                    if (Math.abs(aspecte.ePersona.grau - xp[0] - aspecte.tipusAspecteDec)
                        < marge) {
                        marcaDeTemps.add(
                            String.format(
                                "%02d-%02d-%4d %d:%02d:%02d",
                                dh.get(Calendar.DAY_OF_MONTH),
                                dh.get(Calendar.MONTH),
                                dh.get(Calendar.YEAR),
                                dh.get(Calendar.HOUR_OF_DAY),
                                dh.get(Calendar.MINUTE),
                                dh.get(Calendar.SECOND)
                            ))
                        marcaDeTemps.add(dh.timeInMillis.toString())
                        marcaDeTemps.add(calcularCasa(xp[0]))
                        break@loop
                    }
                }
                */
/*
// Vertex
if (aspecte.eActual.nom.equals("Vertex")) {
    val dhActual = dh
    val cims = DoubleArray(13)
    val punts = DoubleArray(10)
    // a l'hora de calcular l'aspecte exacte, anirem iterant cada segon
    // durant 30 dies
    loop@ for (i in 1..2592000) {
        dh.add(Calendar.SECOND, increment)
        hora = dh.get(Calendar.HOUR_OF_DAY) +
                (dh.get(Calendar.MINUTE) / 60.0) +
                (dh.get(Calendar.SECOND) / 3600.0)
        nd = SweDate(dh.get(Calendar.YEAR),
            dh.get(Calendar.MONTH),
            dh.get(Calendar.DAY_OF_MONTH),
            hora)
        sw.swe_houses(
            nd.julDay,
            0,
            transit.latitud,
            transit.longitud,
            'P'.toInt(),
            cims,
            punts
        )
        if (Math.abs(aspecte.ePersona.grau - punts[3] - aspecte.tipusAspecteDec)
            < marge) {
            marcaDeTemps.add(
                String.format(
                    "%02d-%02d-%4d %d:%02d:%02d",
                    dh.get(Calendar.DAY_OF_MONTH),
                    dh.get(Calendar.MONTH),
                    dh.get(Calendar.YEAR),
                    dh.get(Calendar.HOUR_OF_DAY),
                    dh.get(Calendar.MINUTE),
                    dh.get(Calendar.SECOND)
                ))
            marcaDeTemps.add(dh.timeInMillis.toString())
            marcaDeTemps.add(calcularCasa(punts[3]))
            break@loop
        }
    }
}

private fun gravarVertex(
        t: Persona,
        sw: SwissEph,
        sd: SweDate,
        latitud: Double,
        longitud: Double
    ) {

        // cims de les cases
        val cims = DoubleArray(13)
        val punts = DoubleArray(10)

        sw.swe_houses(
            sd.julDay,
            0,
            latitud,
            longitud,
            'P'.toInt(),
            cims,
            punts
        )

        t.setEntitat("Vertex",
            punts[3],
            " ",
            "Vertex",
            "")
    }

    private fun gravarEstrellesFixes(t: Persona, sw: SwissEph, sd: SweDate) {

        var i: Int
        val xfs = DoubleArray(6)
        val serr = StringBuffer()
        val estrella = StringBuffer()
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

        i = 0
        while (i < estrelles.size) {
            estrella.delete(0, estrella.length)
            estrella.append(estrelles[i])

            sw.swe_fixstar_ut(
                estrella,
                sd.julDay,
                1,
                xfs,
                serr
            )
            /*
            System.err.println("estrella: " + estrella)
            System.err.println("xfs[0]: " + xfs[0])
            System.err.println("xfs[1]: " + xfs[1])
            System.err.println("xfs[2]: " + xfs[2])
            System.err.println("serr: " + serr)
            */

            t.setEntitat(estrelles[i], xfs[0], " ", "Estrella")

            i++
        }
    }
*/