package com.aldarius.transits

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProviders
import com.aldarius.common.Aspecte
import com.aldarius.common.Persona
//import kotlinx.android.synthetic.main.transits_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class TransitsFragment : Fragment() {

    private lateinit var viewModel: TransitsViewModel
    private var retView: View? = null
    private var rutaAssets: String? = null
    private var rutaPersones: String? = null
    private var persona: Persona? = null
    private var persones: ArrayList<String>? = null
    private var posicionsActuals: Persona? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var altitud: Double? = null
    private var strLatitud: String = ""
    private var strLongitud: String = ""
    private var strAltitud: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retView = inflater.inflate(R.layout.transits_fragment, container, false)
        rutaAssets = arguments!!.getString("rutaAssets")
        rutaPersones = arguments!!.getString("rutaPersones")
        return retView
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        //super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProviders.of(this).get(TransitsViewModel::class.java)
        viewModel.fitxerPosicions(rutaAssets!!)
        persones = viewModel.recuperarPersones(rutaPersones!!)

        //val adaptador1 = ArrayAdapter(this.context, android.R.layout.simple_spinner_item, persones)
        //spPersones!!.adapter = adaptador1

        //persona = viewModel.carregarPersona(
            //rutaPersones.toString(),
            //spPersones!!.selectedItem.toString())

        // dia i hora
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val moment = df.format(c.time)

        // localització del dispositiu
        var carregat = false

        val locationResult = object : MyLocation.LocationResult() {

            override fun gotLocation(location: Location?) {

                latitud = location!!.latitude
                longitud = location.longitude
                altitud = location.altitude

                val coordenades = convert(location.latitude, location.longitude)
                strLatitud = coordenades[0]
                strLongitud = coordenades[1]
                strAltitud = location.altitude.toString() + " m."

                if (latitud != null && longitud != null && altitud != null && !carregat) {
                    // zona horària de les dades natals de la persona
                    // TODO poder canviar la zona horària a una altra diferent del naixement
                    // dia/hora, latitud, longitud, altitud i zona horària
                    carregat = true
                    posicionsActuals = viewModel.calcular(
                        moment,
                        latitud!!,
                        longitud!!,
                        altitud!!,
                        persona!!.zona
                    )

                    val aspectes = viewModel.buscarAspectes(persona!!, posicionsActuals!!)
                    aspectes.sortedWith(compareBy { it.momentExacte })
                    mostrarAspectes(aspectes)
                }
            }
        }
        val myLocation = MyLocation()
        myLocation.getLocation(context!!, locationResult)
    }
    /*
0 = {Aspecte@5059} "Aspecte(ePersona=Entitat(nom=AC, grau=77.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=180.0, eActual=Entitat(nom=Jupiter, grau=259.2545115334745, retrograd=D, tipus=Planeta, casa=C7), distancia=1.8156361399363163, apliSepa=S, momentExacte=20190925081606, grauExacteActual=257.4388762574469)"
1 = {Aspecte@5060} "Aspecte(ePersona=Entitat(nom=AC, grau=77.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=270.0, eActual=Entitat(nom=Neptune, grau=346.5754378550603, retrograd=D, tipus=Planeta, casa=C10), distancia=-0.8634375384779105, apliSepa=S, momentExacte=20190411061824, grauExacteActual=347.4388750638575)"
2 = {Aspecte@5061} "Aspecte(ePersona=Entitat(nom=AC, grau=77.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=270.0, eActual=Entitat(nom=Lilith, grau=347.57934015714, retrograd=D, tipus=Lilith, casa=C10), distancia=0.1404647636018126, apliSepa=S, momentExacte=20191007025914, grauExacteActual=347.4388749311481)"
3 = {Aspecte@5062} "Aspecte(ePersona=Entitat(nom=AC, grau=77.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=180.0, eActual=Entitat(nom=Ceres, grau=256.32583910007634, retrograd=D, tipus=Asteroide, casa=C6), distancia=-1.113036293461846, apliSepa=A, momentExacte=20200109195643, grauExacteActual=257.4388755121586)"
4 = {Aspecte@5063} "Aspecte(ePersona=Entitat(nom=DC, grau=257.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=0.0, eActual=Entitat(nom=Jupiter, grau=259.2545115334745, retrograd=D, tipus=Planeta, casa=C7), distancia=1.8156361399363163, apliSepa=S, momentExacte=20190925081606, grauExacteActual=257.4388762574469)"
5 = {Aspecte@5064} "Aspecte(ePersona=Entitat(nom=DC, grau=257.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=90.0, eActual=Entitat(nom=Neptune, grau=346.5754378550603, retrograd=D, tipus=Planeta, casa=C10), distancia=-0.8634375384779105, apliSepa=S, momentExacte=20190411061824, grauExacteActual=347.4388750638575)"
6 = {Aspecte@5065} "Aspecte(ePersona=Entitat(nom=DC, grau=257.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=90.0, eActual=Entitat(nom=Lilith, grau=347.57934015714, retrograd=D, tipus=Lilith, casa=C10), distancia=0.1404647636018126, apliSepa=S, momentExacte=20191007025914, grauExacteActual=347.4388749311481)"
7 = {Aspecte@5066} "Aspecte(ePersona=Entitat(nom=DC, grau=257.4388753935382, retrograd= , tipus=Angle, casa=), tipusAspecte=0.0, eActual=Entitat(nom=Ceres, grau=256.32583910007634, retrograd=D, tipus=Asteroide, casa=C6), distancia=1.113036293461846, apliSepa=A, momentExacte=20200109195643, grauExacteActual=257.4388755121586)"
8 = {Aspecte@5067} "Aspecte(ePersona=Entitat(nom=Sun, grau=327.4756604188794, retrograd=D, tipus=Planeta, casa=C10), tipusAspecte=270.0, eActual=Entitat(nom=Vesta, grau=56.90052303138283, retrograd=D, tipus=Asteroide, casa=C12), distancia=0.575137387496568, apliSepa=S, momentExacte=20190518170006, grauExacteActual=57.475666063210234)"
9 = {Aspecte@5068} "Aspecte(ePersona=Entitat(nom=Moon, grau=243.8971015835688, retrograd=D, tipus=Planeta, casa=C6), tipusAspecte=240.0, eActual=Entitat(nom=Chiron, grau=3.1467772950874346, retrograd=D, tipus=Planeta, casa=C11), distancia=0.7503242884813517, apliSepa=S, momentExacte=20190428012904, grauExacteActual=3.8971014289921206)"
10 = {Aspecte@5069} "Aspecte(ePersona=Entitat(nom=Mercury, grau=303.4862940535341, retrograd=D, tipus=Planeta, casa=C9), tipusAspecte=120.0, eActual=Entitat(nom=Mars, grau=182.70489122294634, retrograd=D, tipus=Planeta, casa=C5), distancia=0.7814028305877514, apliSepa=A, momentExacte=20191009141227, grauExacteActual=183.4862873688774)"
11 = {Aspecte@5070} "Aspecte(ePersona=Entitat(nom=Mercury, grau=303.4862940535341, retrograd=D, tipus=Planeta, casa=C9), tipusAspecte=270.0, eActual=Entitat(nom=Uranus, grau=35.40550927509671, retrograd=D, tipus=Planeta, casa=C11), distancia=-1.919215221562638, apliSepa=A, momentExacte=20200224145502, grauExacteActual=33.486293717620626)"
12 = {Aspecte@5071} "Aspecte(ePersona=Entitat(nom=Mercury, grau=303.4862940535341, retrograd=D, tipus=Planeta, casa=C9), tipusAspecte=300.0, eActual=Entitat(nom=Chiron, grau=3.1467772950874346, retrograd=D, tipus=Planeta, casa=C11), distancia=0.3395167584466776, apliSepa=A, momentExacte=20200221111153, grauExacteActual=3.4862946388285057)"
13 = {Aspecte@5072} "Aspecte(ePersona=Entitat(nom=Mars, grau=199.0661689881389, retrograd=D, tipus=Planeta, casa=C5), tipusAspecte=60.0, eActual=Entitat(nom=Jupiter, grau=259.2545115334745, retrograd=D, tipus=Planeta, casa=C7), distancia=0.18834254533558692, apliSepa=S, momentExacte=20191007031752, grauExacteActual=259.06617038835174)"
14 = {Aspecte@5073} "Aspecte(ePersona=Entitat(nom=Mars, grau=199.0661689881389, retrograd=D, tipus=Planeta, casa=C5), tipusAspecte=90.0, eActual=Entitat(nom=Pluto, grau=290.6401025792148, retrograd=D, tipus=Planeta, casa=C8), distancia=1.5739335910758712, apliSepa=S, momentExacte=20180109104148, grauExacteActual=289.0661686081151)"
15 = {Aspecte@5074} "Aspecte(ePersona=Entitat(nom=Saturn, grau=202.01457981453427, retrograd=R, tipus=Planeta, casa=C5), tipusAspecte=90.0, eActual=Entitat(nom=Pluto, grau=290.6401025792148, retrograd=D, tipus=Planeta, casa=C8), distancia=-1.374477235319489, apliSepa=S, momentExacte=20190213105621, grauExacteActual=292.01457988327377)"
16 = {Aspecte@5075} "Aspecte(ePersona=Entitat(nom=Pluto, grau=206.8354093721954, retrograd=R, tipus=Planeta, casa=C5), tipusAspecte=0.0, eActual=Entitat(nom=Venus, grau=209.58777752746397, retrograd=D, tipus=Planeta, casa=C5), distancia=2.7523681552685844, apliSepa=S, momentExacte=20191006040054, grauExacteActual=206.8354191697848)"
17 = {Aspecte@5076} "Aspecte(ePersona=Entitat(nom=Node Nord, grau=110.7231427884808, retrograd=R, tipus=Node, casa=C2), tipusAspecte=180.0, eActual=Entitat(nom=Pluto, grau=290.6401025792148, retrograd=D, tipus=Planeta, casa=C8), distancia=-0.08304020926601652, apliSepa=A, momentExacte=20191022073441, grauExacteActual=290.72314277555853)"
18 = {Aspecte@5077} "Aspecte(ePersona=Entitat(nom=Lilith, grau=256.2135390194825, retrograd=D, tipus=Lilith, casa=C6), tipusAspecte=90.0, eActual=Entitat(nom=Neptune, grau=346.5754378550603, retrograd=D, tipus=Planeta, casa=C10), distancia=0.3618988355777901, apliSepa=S, momentExacte=20180517210953, grauExacteActual=346.2135388844264)"
19 = {Aspecte@5078} "Aspecte(ePersona=Entitat(nom=Lilith, grau=256.2135390194825, retrograd=D, tipus=Lilith, casa=C6), tipusAspecte=90.0, eActual=Entitat(nom=Lilith, grau=347.57934015714, retrograd=D, tipus=Lilith, casa=C10), distancia=1.3658011376575132, apliSepa=S, momentExacte=20190926035612, grauExacteActual=346.21353794260403)"
20 = {Aspecte@5079} "Aspecte(ePersona=Entitat(nom=Lilith, grau=256.2135390194825, retrograd=D, tipus=Lilith, casa=C6), tipusAspecte=0.0, eActual=Entitat(nom=Ceres, grau=256.32583910007634, retrograd=D, tipus=Asteroide, casa=C6), distancia=0.11230008059385455, apliSepa=A, momentExacte=20200108145643, grauExacteActual=256.21354168423136)"
21 = {Aspecte@5080} "Aspecte(ePersona=Entitat(nom=Chiron, grau=48.12047414571869, retrograd=D, tipus=Planeta, casa=C12), tipusAspecte=300.0, eActual=Entitat(nom=Lilith, grau=347.57934015714, retrograd=D, tipus=Lilith, casa=C10), distancia=-0.5411339885786788, apliSepa=A, momentExacte=20191013051737, grauExacteActual=348.1204739039377)"
22 = {Aspecte@5081} "Aspecte(ePersona=Entitat(nom=Chiron, grau=48.12047414571869, retrograd=D, tipus=Planeta, casa=C12), tipusAspecte=180.0, eActual=Entitat(nom=Pallas, grau=226.87052181042085, retrograd=D, tipus=Asteroide, casa=C6), distancia=-1.2499523352978485, apliSepa=A, momentExacte=20191110195925, grauExacteActual=228.12046780603487)"
23 = {Aspecte@5082} "Aspecte(ePersona=Entitat(nom=Node Sud, grau=290.7231427884808, retrograd=R, tipus=Node, casa=C8), tipusAspecte=0.0, eActual=Entitat(nom=Pluto, grau=290.6401025792148, retrograd=D, tipus=Planeta, casa=C8), distancia=0.08304020926601652, apliSepa=A, momentExacte=20191022073437, grauExacteActual=290.72314235951296)"
24 = {Aspecte@5083} "Aspecte(ePersona=Entitat(nom=Pallas, grau=201.41231541304586, retrograd=R, tipus=Asteroide, casa=C5), tipusAspecte=90.0, eActual=Entitat(nom=Pluto, grau=290.6401025792148, retrograd=D, tipus=Planeta, casa=C8), distancia=-0.7722128338310768, apliSepa=S, momentExacte=20190125083200, grauExacteActual=291.41231567273314)"
25 = {Aspecte@5084} "Aspecte(ePersona=Entitat(nom=Vesta, grau=281.99699413997934, retrograd=D, tipus=Asteroide, casa=C8), tipusAspecte=0.0, eActual=Entitat(nom=Saturn, grau=284.23842139915456, retrograd=D, tipus=Planeta, casa=C8), distancia=2.241427259175225, apliSepa=S, momentExacte=20190106055902, grauExacteActual=281.9969953867947)"
26 = {Aspecte@5085} "Aspecte(ePersona=Entitat(nom=Vertex, grau=219.63297287349505, retrograd= , tipus=Vertex, casa=C6), tipusAspecte=0.0, eActual=Entitat(nom=Mercury, grau=216.9523353454479, retrograd=D, tipus=Planeta, casa=C6), distancia=2.680637528047157, apliSepa=A, momentExacte=20191010100525, grauExacteActual=219.6329701306839)"
27 = {Aspecte@5086} "Aspecte(ePersona=Entitat(nom=Venus, grau=293.9798317987018, retrograd=D, tipus=Planeta, casa=C8), tipusAspecte=60.0, eActual=Entitat(nom=Infortuni, grau=234.2417304271424, retrograd= , tipus=Part, casa=C6), distancia=-0.26189862844057643, apliSepa=A, momentExacte=20191116154401, grauExacteActual=233.97982520370167)"
28 = {Aspecte@5087} "Aspecte(ePersona=Entitat(nom=Ceres, grau=232.6164845648644, retrograd=D, tipus=Asteroide, casa=C6), tipusAspecte=0.0, eActual=Entitat(nom=Infortuni, grau=234.2417304271424, retrograd= , tipus=Part, casa=C6), distancia=1.6252458622779784, apliSepa=A, momentExacte=20191115071801, grauExacteActual=232.61649464512726)"
29 = {Aspecte@5088} "Aspecte(ePersona=Entitat(nom=Vesta, grau=281.99699413997934, retrograd=D, tipus=Asteroide, casa=C8), tipusAspecte=270.0, eActual=Entitat(nom=Fortuna, grau=13.636709987700158, retrograd= , tipus=Part, casa=C11), distancia=-1.6397158477208222, apliSepa=S, momentExacte=20190402004954, grauExacteActual=11.996999369022127)"
     */

    private fun convert(latitud: Double, longitud: Double): List<String> {
        val coordenades = ArrayList<String>()

        val latitudeDegrees = Location.convert(
            abs(latitud),
            Location.FORMAT_SECONDS)

        val latitudeSplit = latitudeDegrees.split(":").toTypedArray()

        coordenades.add(latitudeSplit[0] + "°" + latitudeSplit[1] + "'" + latitudeSplit[2] + "\"")

        if (latitud < 0) {
            coordenades[0] = coordenades[0] + " S"
        } else {
            coordenades[0] = coordenades[0] + " N"
        }

        val longitudeDegrees = Location.convert(
            abs(longitud),
            Location.FORMAT_SECONDS
        )

        val longitudeSplit = longitudeDegrees.split(":").toTypedArray()
        coordenades.add(longitudeSplit[0] + "°" + longitudeSplit[1] + "'" + longitudeSplit[2]
                + "\"")

        if (latitud < 0) {
            coordenades[1] = coordenades[1] + " W"
        } else {
            coordenades[1] = coordenades[1] + " E"
        }

        return coordenades
    }

    fun mostrarAspectes(aspectes: ArrayList<Aspecte>) {

        //tvLatitud.text = strLatitud
        //tvLongitud.text = strLongitud
        //tvAltitud.text = strAltitud

        /*
        // zona: +xx:00
        persona!!.zona
        var zona = java.lang.Double.parseDouble(zona.substring(0, 3))
        when (zona.substring(4, 6)) {
            "30" -> zona += 0.5
            "45" -> zona += 0.75
        }
        */

        //omplirLlista(aspectes)
    }

    /*
    fun omplirLlista(aspectes: ArrayList<Aspecte>): HashMap<String?, List<String?>?>? {
        var expandableListDetail: HashMap<String?, List<String?>?>? = null
        /*
        http://www.apnatutorials.com/android/expandable-listview-customization-and-usage.php?categoryId=2&subCategoryId=57&myPath=android/expandable-listview-customization-and-usage.php
        */
        for (a in aspectes) {
            val cAspecte: Unit
            cAspecte

            val dAspecte: ArrayList<String>? = null
            dAspecte!!.add(a.momentInicial)
            dAspecte.add(a.momentExacte)
            dAspecte.add(a.momentFinal)
            dAspecte.add(a.apliSepa)
            //dAspecte.add(a.eActual.nom)
            dAspecte.add(a.eActual.casa)
            dAspecte.add(a.eActual.retrograd)
            //dAspecte.add(a.eActual.grau.toString())
            //dAspecte.add(a.ePersona.nom)
            dAspecte.add(a.ePersona.casa)
            dAspecte.add(a.ePersona.retrograd)
            //dAspecte.add(a.ePersona.grau.toString())
            dAspecte.add(a.distancia.toString())
            expandableListDetail!!.put(cAspecte, dAspecte)
        }
            return expandableListDetail
    }
     */
}
