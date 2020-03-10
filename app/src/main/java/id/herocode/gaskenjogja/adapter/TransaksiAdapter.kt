package id.herocode.gaskenjogja.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        holder.transaksiNamaWisata.text = transaksi.namaWisata
        holder.transaksiStatus.text =
            if (transaksi.statusWisata.equals("BELUM TERPAKAI")) "Belum Terpakai"
            else "Terpakai"
        if (holder.transaksiStatus.text == "Terpakai") {
            holder.idTransaksi.setBackgroundColor(Color.rgb(229,57,53))
            holder.idTransaksi.setTextColor(Color.WHITE)
        } else {
            holder.idTransaksi.setBackgroundColor(Color.rgb(118,225,3))
        }
        holder.transaksiTanggal.text = transaksi.tanggalWisata
    }

    override fun getItemCount(): Int {
        return transaksi.size
    }

    inner class TransaksiHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val idTransaksi: TextView = itemView.findViewById(R.id.id_transaksi)
        val transaksiNamaWisata: TextView = itemView.findViewById(R.id.transaksi_nama_wisata)
        val transaksiStatus: TextView = itemView.findViewById(R.id.transaksi_status)
        val transaksiTanggal: TextView = itemView.findViewById(R.id.tanggal_transaksi)
    }

}