package com.aldarius.horoscop.transits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aldarius.horoscop.common.Aspecte

class AspecteAdapter(private val aspectes: ArrayList<Aspecte>) :
    RecyclerView.Adapter<AspecteAdapter.AspecteViewHolder>() {

    class AspecteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgActual: ImageView = itemView.findViewById(R.id.imgActual)
        val imgTipusAspecte: ImageView = itemView.findViewById(R.id.imgTipusAspecte)
        val imgPersona: ImageView = itemView.findViewById(R.id.imgPersona)
        val tvMomentExacte: TextView = itemView.findViewById(R.id.tvMomentExacte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AspecteViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_aspecte, parent, false)
        return AspecteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AspecteViewHolder, position: Int) {
        val aspecte = aspectes[position]

        val imgActual = when (aspecte.eActual.nom) {
            "Sol" -> R.drawable.sol
            "Lluna" -> R.drawable.lluna
            "Mercuri" -> R.drawable.mercuri
            "Venus" -> R.drawable.venus
            "Mart" -> R.drawable.mart
            "Jupiter" -> R.drawable.jupiter
            "Saturn" -> R.drawable.saturn
            "Ura" -> R.drawable.ura
            "Neptu" -> R.drawable.neptu
            "Pluto" -> R.drawable.pluto
            else -> {0}
        }
        val imgTipusAspecte = when (aspecte.tipusAspecte) {
            0.0 -> R.drawable.conjuncio
            60.0 -> R.drawable.sextil
            90.0 -> R.drawable.quadratura
            120.0 -> R.drawable.trigon
            180.0 -> R.drawable.oposicio
            240.0 -> R.drawable.trigon
            270.0 -> R.drawable.quadratura
            300.0 -> R.drawable.sextil
            else -> {0}
        }
        val imgPersona = when (aspecte.ePersona.nom) {
            "Sol" -> R.drawable.sol
            "Lluna" -> R.drawable.lluna
            "Mercuri" -> R.drawable.mercuri
            "Venus" -> R.drawable.venus
            "Mart" -> R.drawable.mart
            "Jupiter" -> R.drawable.jupiter
            "Saturn" -> R.drawable.saturn
            "Ura" -> R.drawable.ura
            "Neptu" -> R.drawable.neptu
            "Pluto" -> R.drawable.pluto
            else -> {0}
        }

        holder.imgActual.setImageResource(imgActual)
        holder.imgTipusAspecte.setImageResource(imgTipusAspecte)
        holder.imgPersona.setImageResource(imgPersona)

        holder.tvMomentExacte.text = aspecte.momentExacte
    }

    override fun getItemCount(): Int {
        return aspectes.size
    }
}