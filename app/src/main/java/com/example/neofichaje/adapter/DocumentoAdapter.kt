package com.example.neofichaje.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.neofichaje.R
import com.example.neofichaje.databinding.ItemDocumentoBinding
import com.example.neofichaje.model.Documento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DocumentoAdapter(
    private val context: Context,
    private val listaDocumentos: MutableList<Documento>,
    private val tipoDoc: String
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

        //Abre el PDF al pulsar
        holder.binding.root.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(documento.url), "application/pdf")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        //Muestra PopupMenu al pulsar 3 puntitos
        holder.binding.btnOpciones.setOnClickListener {
            val popup = PopupMenu(context, holder.binding.btnOpciones)
            popup.menuInflater.inflate(R.menu.menu_opciones_documento, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_eliminar -> {
                        eliminarDocumento(documento.id, position)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = listaDocumentos.size

    private fun eliminarDocumento(documentId: String, position: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
        val documento = listaDocumentos[position]

        //  eliminar de Storage
        val storageRef = storage.reference
            .child("documentos/$uid/$tipoDoc/${documento.nombreArchivo}")
        storageRef.delete()
            .addOnSuccessListener {
                // eliminar de usuarios Firestore
                db.collection("usuarios")
                    .document(uid)
                    .collection(tipoDoc)
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        //  buscar empresaId y eliminar también de empresa
                        db.collection("usuarios")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val empresaId = userDoc.getString("empresa_id")
                                if (!empresaId.isNullOrEmpty()) {
                                    val rutaEmpresa = when (tipoDoc) {
                                        "nominas" -> "gestionNominas"
                                        "contrato" -> "gestionContratos"
                                        else -> "otrosDocumentos"
                                    }

                                    db.collection("empresas")
                                        .document(empresaId)
                                        .collection(rutaEmpresa)
                                        .whereEqualTo("nombreArchivo", documento.nombreArchivo)
                                        .get()
                                        .addOnSuccessListener { docs ->
                                            for (doc in docs) {
                                                db.collection("empresas")
                                                    .document(empresaId)
                                                    .collection(rutaEmpresa)
                                                    .document(doc.id)
                                                    .delete()
                                            }
                                        }
                                }
                            }

                        // actualiza el RecyclerView
                        Toast.makeText(context, "Documento eliminado correctamente", Toast.LENGTH_SHORT).show()
                        listaDocumentos.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error eliminando Firestore: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error eliminando archivo PDF: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
