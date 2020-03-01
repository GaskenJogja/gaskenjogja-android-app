package id.herocode.gaskenjogja.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.model.Transaksi

class TransaksiAdapter(
    private val context: Context,
    private val transaksi: List<Transaksi>
) : RecyclerView.Adapter<TransaksiAdapter.TransaksiHolder>() {

    private val listTransaksi: ArrayList<Transaksi> = ArrayList(transaksi)
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiHolder {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.list_transaksi, parent, false)
        return TransaksiHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiHolder, position: Int) {
        val transaksi = listTransaksi[position]
        holder.idTransaksi.text = (position+1).toString()
        holder.transaksi_nama_wisata.text = transaksi.namaWisata
        holder.transaksi_status.text =
            if (transaksi.statusWisata.equals("BELUM MASUK")) "Belum Terpakai"
            else "Terpakai"
        if (holder.transaksi_status.text.equals("Terpakai")) {
            holder.idTransaksi.setBackgroundColor(Color.rgb(229,57,53))
            holder.idTransaksi.setTextColor(Color.WHITE)
        } else {
            holder.idTransaksi.setBackgroundColor(Color.rgb(118,225,3))
        }
        holder.transaksi_tanggal.text = transaksi.tanggalWisata
    }

    override fun getItemCount(): Int {
        return transaksi.size
    }

    inner class TransaksiHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val idTransaksi: TextView = itemView.findViewById(R.id.id_transaksi)
        val transaksi_nama_wisata: TextView = itemView.findViewById(R.id.transaksi_nama_wisata)
        val transaksi_status: TextView = itemView.findViewById(R.id.transaksi_status)
        val transaksi_tanggal: TextView = itemView.findViewById(R.id.tanggal_transaksi)
    }

}