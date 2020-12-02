package com.aldarius.horoscop

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.aldarius.novapersona.NovaPersonaFragment
import com.aldarius.transits.TransitsFragment
import java.io.File
import java.util.ArrayList
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class Inicial : AppCompatActivity() {
    private lateinit var positiveButtonClick: (DialogInterface, Int) -> Unit
    private lateinit var fragment: Fragment
    // els TXT amb les persones donades d'alta estaran a la memòria SD (si n'hi ha)
    internal var rutaPersones: String = ""
    // els fitxers SE1 (i TXT amb dades de cossos estelars) estaran a la memòria interna del telèfon
    internal var rutaAssets: String = ""

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int, b: Bundle){
        fragment.arguments = b
        supportFragmentManager.inTransaction{add(frameId, fragment)}
    }

    fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int, b: Bundle) {
        fragment.arguments = b
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitsONovaPersona()
        setContentView(R.layout.inicial)
        //pagerAdapter = SimpleFragmentPagerAdapter(supportFragmentManager)
        //viewPager = findViewById(R.id.viewpager)

        //viewPager.adapter = pagerAdapter
        //setSupportActionBar(findViewById(R.id.barra))
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CopiaAssets(".*\\.se1", applicationContext).copy()
        CopiaAssets(".*\\.txt", applicationContext).copy()
    }


    override fun onDestroy() {
        super.onDestroy()
        transitsONovaPersona()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("NovaPersonaFragment") != null) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
            transitsONovaPersona()
        }
    }

    fun transitsONovaPersona() {
        rutaPersones = applicationContext.filesDir.toString()
        rutaAssets = applicationContext.filesDir.toString() + "/ephe"
        val b = Bundle()
        b.putString("rutaPersones", rutaPersones)
        b.putString("rutaAssets", rutaAssets)
        if (recuperarPersones(rutaPersones).size == 0) {
            /*
            positiveButtonClick = { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            basicAlert()
            */
            fragment = NovaPersonaFragment()
        }
        else {
            fragment = TransitsFragment()
        }
        replaceFragment(fragment, R.id.fragmentContainer, b)
        //setSupportActionBar(findViewById(R.id.barra))
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    fun basicAlert(){

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setMessage(R.string.altaPersona)
            setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
            show()
        }
    }
    /*

    fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    public boolean onOptionsItemSelected (MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.novaPersona:
                i = new Intent(this, NovaPersona.class);
                startActivity(i);
                return true;

            case R.id.transits:
                ArrayList<String> persones = new ArrayList<String>();
                persones = Utilitats.recuperarPersones(ruta);
                if (persones.size() > 0) {
                    i = new Intent(this, Transits.class);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(this, R.string.altaPersona, Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    internal fun recuperarPersones(ruta: String): ArrayList<String> {
        val persones = ArrayList<String>()
        val r = File(ruta)
        val fitxers = r.listFiles()
        if (fitxers != null) {
            for (i in fitxers.indices) {
                if (fitxers[i].toString().endsWith(".ser")) {
                    persones.add(
                        fitxers[i].toString().substring(
                            r.toString().length + 1,
                            fitxers[i].toString().length - 4
                        )
                    )
                }
            }
        }
        return persones
    }
}
