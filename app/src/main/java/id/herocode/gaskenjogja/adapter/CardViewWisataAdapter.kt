package id.herocode.gaskenjogja.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import id.herocode.gaskenjogja.R
import id.herocode.gaskenjogja.adapter.CardViewWisataAdapter.WisataHolder
import id.herocode.gaskenjogja.model.ModelWisata
import kotlin.collections.ArrayList

class CardViewWisataAdapter(private val context: Context, wisata: List<ModelWisata>?) : RecyclerView.Adapter<WisataHolder>() {
    private val wisatas: List<ModelWisata>
    private val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    private lateinit var view: View
    private lateinit var clickItem: ClickItem

    interface ClickItem {
        fun itemClicked(data: ModelWisata)
    }

    fun setClickItem(clickItem: ClickItem) {
        this.clickItem = clickItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): WisataHolder {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.list_wisata, parent, false)
        return WisataHolder(view)
    }


    override fun onBindViewHolder(holder: WisataHolder, position: Int) {

        val wisata = wisatas[position]
        val imgView = holder.imgWisata
        val image = wisata.imG_WISATA
        val imgBitMap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val expanded = wisata.isExpanded

        holder.tvNamaWisata.text = wisata.namA_WISATA
        holder.tvAlamat.text = wisata.alamaT_WISATA
        holder.tvHtmWisata.text = "Rp. ${wisata.hargA_WISATA},-"

        if  (expanded) {
            expandItem(holder, true)
        } else {
            expandItem(holder, false)
        }

        Glide.with(context)
            .load(imgBitMap)
            .transition(withCrossFade(factory))
            .fallback(R.color.colorPrimaryDark)
            .centerCrop()
            .into(imgView)

        holder.baseLayout.setOnClickListener {
            for (i in wisatas.indices) {
                if (i == position) {
                    wisata.isExpanded = expanded != true
                } else {
                    wisatas[i].isExpanded = false
                }
                notifyItemChanged(i)
            }
        }

        holder.btnClickDetail.setOnClickListener{
            clickItem.itemClicked(wisatas[holder.adapterPosition])
        }

    }


    override fun getItemCount(): Int {
        return wisatas.size
    }

    private fun expandItem(holder: WisataHolder, expand: Boolean) {
        if  (expand) {
            holder.layoutDetail.visibility = View.VISIBLE
            holder.layoutClickDetail.visibility = View.VISIBLE
        } else {
            holder.layoutDetail.visibility = View.GONE
            holder.layoutClickDetail.visibility = View.GONE
        }
    }

    inner class WisataHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvNamaWisata: TextView = itemView.findViewById(R.id.tv_namawisata)
        val tvAlamat: TextView = itemView.findViewById(R.id.tv_alamat)
        val tvHtmWisata: TextView = itemView.findViewById(R.id.tv_htm_wisata)
        val imgWisata: ImageView = itemView.findViewById(R.id.img_wisata)
        val layoutDetail: LinearLayout = itemView.findViewById(R.id.layout_detail_item)
        val baseLayout: LinearLayout = itemView.findViewById(R.id.base_layout_item)
        val layoutClickDetail: LinearLayout = itemView.findViewById(R.id.layout_click_detail)
        val btnClickDetail: Button = itemView.findViewById(R.id.btn_detail_wisata)
    }

    init {
        wisatas = ArrayList(wisata!!)
    }
}