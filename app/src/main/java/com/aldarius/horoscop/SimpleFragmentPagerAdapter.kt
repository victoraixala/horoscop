package com.aldarius.horoscop

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.aldarius.horoscop.novapersona.NovaPersonaFragment
import com.aldarius.transits.TransitsFragment

class SimpleFragmentPagerAdapter(fm: FragmentManager, data: Bundle): FragmentPagerAdapter(fm) {

    private val b: Bundle = data

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position)
        {
            0 -> {
                fragment = NovaPersonaFragment()
            }
            1 -> {
                fragment = TransitsFragment()
            }
        }
        fragment!!.arguments = b
        return fragment!!
    }

    override fun getCount(): Int {
        return 2
    }
}