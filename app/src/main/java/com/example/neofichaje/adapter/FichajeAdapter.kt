package com.example.neofichaje.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.neofichaje.R
import com.example.neofichaje.model.Fichaje

class FichajeAdapter(private val lista: List<Fichaje>) :
    RecyclerView.Adapter<FichajeAdapter.FichajeViewHolder>() {

    class FichajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre = itemView.findViewById<TextView>(R.id.tvNombreEmpleado)
        val entrada = itemView.findViewById<TextView>(R.id.tvEntrada)
        val salida = itemView.findViewById<TextView>(R.id.tvSalida)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FichajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fichaje, parent, false)
        return FichajeViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FichajeViewHolder, position: Int) {
        val item = lista[position]
        holder.nombre.text = "${item.nombre} ${item.apellidos}"
        holder.entrada.text = "Entrada: ${item.horaEntrada}"
        holder.salida.text = "Salida: ${item.horaSalida}"

    }

    override fun getItemCount(): Int = lista.size
}


