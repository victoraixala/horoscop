package com.aldarius.common

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.*


class AdaptadorExpandible(
    context: Context?,
    data: Collection<FilaAspecte>
) :
    ExpandableListAdapter {
    var context: Context? = null
    var llistaAspectesOriginal: ArrayList<FilaAspecte>
    var llistaAspectes: ArrayList<FilaAspecte>
    override fun registerDataSetObserver(dataSetObserver: DataSetObserver) {}
    override fun unregisterDataSetObserver(dataSetObserver: DataSetObserver) {}
    override fun onGroupExpanded(i: Int) {}
    override fun onGroupCollapsed(i: Int) {}
    override fun isEmpty(): Boolean {
        return if (llistaAspectes.size == 0) true else false
    }

    override fun getGroupCount(): Int {
        return llistaAspectes.size
    }

    override fun getChildrenCount(i: Int): Int {
        return llistaAspectes[i].getDetall()!!.size
    }

    override fun getGroup(i: Int): FilaAspecte {
        return llistaAspectes[i]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Detall {
        return llistaAspectes[groupPosition].getDetall()!![childPosition]
    }

    override fun getGroupId(i: Int): Long {
        return i.toLong()
    }

    override fun getChildId(i: Int, i1: Int): Long {
        return i1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        position: Int,
        b: Boolean,
        contentView: View,
        parent: ViewGroup
    ): View {
        var contentView = contentView
        val FilaAspecte: FilaAspecte = llistaAspectes[position]
        if (contentView == null) {
            contentView =
                LayoutInflater.from(context).inflate(R.layout.fila_aspecte, parent, false)
        }
        val ivPlanetaActual =
            contentView.findViewById<View>(R.id.ivPlanetaActual) as ImageView
        val tvPlanetaActual =
            contentView.findViewById<View>(R.id.tvPlanetaActual) as TextView
        val ivAspecte =
            contentView.findViewById<View>(R.id.ivAspecte) as ImageView
        val ivPlanetaNatal =
            contentView.findViewById<View>(R.id.ivPlanetaNatal) as ImageView
        val tvPlanetaNatal =
            contentView.findViewById<View>(R.id.tvPlanetaNatal) as TextView
        ivPlanetaActual.setImageResource(FilaAspecte.imatgePlanetaActual)
        tvPlanetaActual.setText(FilaAspecte.nomPlanetaActual)
        ivAspecte.setImageResource(FilaAspecte.imatgeAspecte)
        ivPlanetaNatal.setImageResource(FilaAspecte.imatgePlanetaNatal)
        tvPlanetaNatal.setText(FilaAspecte.nomPlanetaNatal)
        return contentView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        b: Boolean,
        contentView: View,
        parent: ViewGroup
    ): View {
        var contentView = contentView
        val detall: Detall = llistaAspectes[groupPosition].getDetall()!![childPosition]
        if (contentView == null) {
            contentView = LayoutInflater.from(context).inflate(R.layout.fila_0_1, parent, false)
        }
        val tvCountryName =
            contentView.findViewById<View>(R.id.tvCountryName) as TextView
        val imageView =
            contentView.findViewById<View>(R.id.ivCountryFlag) as ImageView
        imageView.setImageResource(country.getFlag())
        tvCountryName.setText(country.getName())
        return contentView
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return true
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun getCombinedChildId(l: Long, l1: Long): Long {
        return l1
    }

    override fun getCombinedGroupId(l: Long): Long {
        return l
    }

    init {
        this.context = context
        llistaAspectesOriginal = ArrayList()
        llistaAspectesOriginal.addAll(data)
        llistaAspectes = ArrayList()
        llistaAspectes.addAll(data)
    }
}