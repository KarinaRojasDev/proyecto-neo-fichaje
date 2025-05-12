package com.example.neofichaje.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.neofichaje.databinding.ItemDocumentoBinding
import com.example.neofichaje.model.Documento

class DocumentoAdapter(
    private val context: Context,
    private val listaDocumentos: List<Documento>
) : RecyclerView.Adapter<DocumentoAdapter.DocumentoViewHolder>() {

    inner class DocumentoViewHolder(val binding: ItemDocumentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentoViewHolder {
        val binding = ItemDocumentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentoViewHolder, position: Int) {
        val documento = listaDocumentos[position]

        holder.binding.nombreDocumento.text =
            if (!documento.tituloDocumento.isNullOrEmpty()) documento.tituloDocumento else documento.nombreArchivo

        holder.binding.root.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(documento.url), "application/pdf")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaDocumentos.size
}