package com.aldarius.novapersona

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.io.IOException

class NovaPersonaFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    // 41:24:51 N
    // 2:08:33 E
    private var btGravar: Button? = null
    private var etNom: EditText? = null
    private var etDia: EditText? = null
    private var etHora: EditText? = null
    private var etLatitud: EditText? = null
    private var etLongitud: EditText? = null
    private var etAltitud: EditText? = null
    private var spNS: Spinner? = null
    private var spEW: Spinner? = null
    private var spZona: Spinner? = null
    private var rutaAssets = ""
    private var rutaPersones = ""

    companion object {
        //fun newInstance() = NovaPersonaFragment()
        const val TAG = "NovaPersonaFragment"
    }

    private lateinit var viewModel: NovaPersonaViewModel
    private var retView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retView = inflater.inflate(R.layout.nova_persona_fragment, container, false)
        rutaAssets = arguments!!.getString("rutaAssets")!!
        rutaPersones = arguments!!.getString("rutaPersones")!!
        val ns = arrayOf("N", "S")
        val ew = arrayOf("E", "W")
        val zones = arrayOf("+01:00", "+00:00", "+02:00", "+03:00", "+03:30", "+04:00", "+04:30", "+05:00", "+05:30", "+05:45", "+06:00", "+06:30", "+07:00", "+08:00", "+08:30", "+08:45", "+09:00", "+09:30", "+10:00", "+10:30", "+11:00", "+11:30", "+12:00", "+12:45", "+13:00", "+14:00", "-01:00", "-02:00", "-02:30", "-03:00", "-03:30", "-04:00", "-04:30", "-05:00", "-06:00", "-07:00", "-08:00", "-09:00", "-09:30", "-10:00", "-11:00", "-12:00")

        etNom = retView!!.findViewById(R.id.etNom)
        etDia = retView!!.findViewById(R.id.etDia)
        etHora = retView!!.findViewById(R.id.etHora)
        etLatitud = retView!!.findViewById(R.id.etLatitud)
        spNS = retView!!.findViewById(R.id.spNS)
        etLongitud = retView!!.findViewById(R.id.etLongitud)
        spEW = retView!!.findViewById(R.id.spEW)
        etAltitud = retView!!.findViewById(R.id.etAltitud)
        spZona = retView!!.findViewById(R.id.spZona)
        btGravar = retView!!.findViewById(R.id.btGravar)

        val adaptador1 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, ns)
        spNS!!.adapter = adaptador1

        val adaptador2 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, ew)
        spEW!!.adapter = adaptador2

        val adaptador3 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, zones)
        spZona!!.adapter = adaptador3

        etDia!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mostraDialegDia()
            }
        }

        etHora!!.setOnFocusChangeListener{ _, hasFocus ->
            if (hasFocus) {
                mostraDialegHora()
            }
        }

        btGravar!!.setOnClickListener {
            gravarPersona()
        }

        return retView
    }

    /*
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NovaPersonaViewModel::class.java)
    }
    */

    private fun gravarPersona () {
        val dades: String = viewModel.gravarNatal(
            etNom!!.text.toString(),
            etDia!!.text.toString(),
            etHora!!.text.toString(),
            etLatitud!!.text.toString(),
            spNS!!.selectedItem.toString(),
            etLongitud!!.text.toString(),
            spEW!!.selectedItem.toString(),
            etAltitud!!.text.toString(),
            spZona!!.selectedItem.toString(),
            rutaAssets,
            rutaPersones
        )
        // dades per informar (totes obligatories):
        // etNom
        // dia de naixement (calendari)
        // hora de naixement
        // latitud (N/S)
        // longitud (E/W)
        // altitud (metres)
        // zona horària
        try {
            viewModel.gravarPersona(
                rutaPersones,
                etNom!!.text.toString() + ".txt",
                dades
            )
            Toast.makeText(
                this.context,
                R.string.dadesGravadesOut,
                Toast.LENGTH_SHORT
            ).show()
            Toast.makeText(
                this.context,
                rutaPersones + etNom!!.text.toString() + ".txt",
                Toast.LENGTH_LONG
            ).show()
            /*
            etNom!!.setText("")
            etDia!!.setText("")
            etHora!!.setText("")
            etLatitud!!.setText("")
            etLongitud!!.setText("")
            etAltitud!!.setText("")
            */
        }
        catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this.context,R.string.dadesNoGravades,Toast.LENGTH_SHORT).show()
            }
    }


    /*
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.getItemId()) {
            R.id.novaPersona -> {
                Toast.makeText(this, R.string.mateixaFinestra, Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.transits -> {
                var persones = ArrayList<String>()
                persones = Utilitats.recuperarPersones(ruta)
                if (persones.size() > 0) {
                    val i = Intent(this, Transits::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this, R.string.altaPersona, Toast.LENGTH_LONG).show()
                }
                return true
            }

            R.id.action_settings ->
                // User chose the "Settings" item, show the app settings UI...
                return true

            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }
    */

    private fun mostraDialegDia() {
        //val fragmentDia = FragmentDia()
        //fragmentDia.show(fragmentManager, "selectorDia")
        val ara = Calendar.getInstance()
        if (etDia!!.text.isEmpty()) {
            val any = ara.get(Calendar.YEAR)
            val mes = ara.get(Calendar.MONTH)
            val dia = ara.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(activity!!.applicationContext, { _, year, monthOfYear, dayOfMonth ->
                val dataDeNaixement = String.format("%02d", dayOfMonth) + "/" +
                        String.format("%02d", monthOfYear + 1) + "/" +
                        String.format("%02d", year)
                etDia!!.setText(dataDeNaixement)
            }, any, mes, dia).show()
        }
        else
        {
            DatePickerDialog(activity!!.applicationContext, { _, year, monthOfYear, dayOfMonth ->
                val dataDeNaixement = String.format("%02d", dayOfMonth) + "/" +
                        String.format("%02d", monthOfYear + 1) + "/" +
                        String.format("%02d", year)
                etDia!!.setText(dataDeNaixement)
            }, etDia!!.text.substring(0..1).toInt(),
                etDia!!.text.substring(3..4).toInt(),
                etDia!!.text.substring(6..9).toInt()).show()
        }
    }

     private fun mostraDialegHora() {
         val ara = Calendar.getInstance()
         if (etHora!!.text.isEmpty()) {
             // Not resolved com.kovachcode:timePickerWithSeconds:1.0.1, will implement it
             /*
             val mTimePicker = MyTimePickerDialog(
                 this.context, object : MyTimePickerDialog.OnTimeSetListener {
                     override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int, seconds: Int) {
                         val hora = String.format("%02d", hourOfDay) +
                                 ":" + String.format("%02d", minute) +
                                 ":" + String.format("%02d", seconds)
                         etHora!!.setText(hora)
                     }
                 }, ara.get(Calendar.HOUR_OF_DAY),
                 ara.get(Calendar.MINUTE),
                 ara.get(Calendar.SECOND),
                 true
             )
             mTimePicker.show()
             */
         }
         else
         {
             // Not resolved com.kovachcode:timePickerWithSeconds:1.0.1, will implement it
             /*
             val mTimePicker = MyTimePickerDialog(
                 this.context, object : MyTimePickerDialog.OnTimeSetListener {
                     override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int, seconds: Int) {
                         val hora = String.format("%02d", hourOfDay) +
                                 ":" + String.format("%02d", minute) +
                                 ":" + String.format("%02d", seconds)
                         etHora!!.setText(hora)
                     }
                 }, etHora!!.text.substring(0..1).toInt(),
                 etHora!!.text.substring(3..4).toInt(),
                 etHora!!.text.substring(6..7).toInt(),
                 true
             )
             mTimePicker.show()
             */
         }
     }

    private fun guardaDia(any: Int, mes: Int, dia: Int) {
        val diaS = dia.toString()
        val mesS = (mes + 1).toString()
        val anyS = any.toString()
        val etDia: TextView = retView!!.findViewById(R.id.etDia)
        etDia.text = "$diaS/$mesS/$anyS"
    }

    /*
    fun guardaHora(hora: Int, minut: Int, segon: Int) {
        val horaS = Integer.toString(hora)
        val minutS = Integer.toString(minut)
        val segonS = Integer.toString(segon)
        val etHora: TextView = retView!!.findViewById(R.id.etHora)
        etHora.setText("$horaS:$minutS:$segonS")
    }
    */

    private fun traduir(nomOriginal: String): String {
        val nomTraduit: String = when (nomOriginal) {
            "Sun" -> resources.getString(R.string.sol)
            "Moon" -> resources.getString(R.string.lluna)
            "Mercury" -> resources.getString(R.string.mercuri)
            "Mars" -> resources.getString(R.string.mart)
            "Jupiter" -> resources.getString(R.string.jupiter)
            "Saturn" -> resources.getString(R.string.saturn)
            "Uranus" -> resources.getString(R.string.ura)
            "Neptune" -> resources.getString(R.string.neptu)
            "Pluto" -> resources.getString(R.string.pluto)
            else -> nomOriginal
        }
        return nomTraduit
    }

    override fun onDateSet(view: DatePicker, any: Int, mes: Int, dia: Int) {
        //val fragment = parentFragment as NovaPersonaFragment
        //val fragment = fragmentManager?.findFragmentByTag(NovaPersonaFragment.TAG) as? NovaPersonaFragment
        guardaDia(any, mes, dia)
    }
}
