package ca.gbc.comp3074.scavengerhunt.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhunt.R
import ca.gbc.comp3074.scavengerhunt.data.Hunt

/**
 * Adapter for the hunt list.
 *
 * Using ListAdapter (not bare RecyclerView.Adapter) on purpose — it diffs the
 * list automatically so when one hunt changes we get a nice fade animation
 * instead of the whole list flashing. submitList() is all we have to call.
 */
class HuntAdapter(
    private val onClick: (Hunt) -> Unit
) : ListAdapter<Hunt, HuntAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Inflate one row layout (item_hunt.xml) and wrap it in a ViewHolder.
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hunt, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * One ViewHolder per visible row. Caching findViewById results here is a
     * big perf win — RecyclerView will reuse the same VH for hundreds of rows.
     */
    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.huntName)
        private val tag: TextView = itemView.findViewById(R.id.huntTag)
        private val location: TextView = itemView.findViewById(R.id.huntLocation)
        private val rating: RatingBar = itemView.findViewById(R.id.huntRating)

        fun bind(hunt: Hunt, onClick: (Hunt) -> Unit) {
            name.text = hunt.name
            tag.text = hunt.tag
            // "Address, City" but skip the comma if address is empty (rare but possible).
            location.text = listOf(hunt.address, hunt.city)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            rating.rating = hunt.rating
            // Whole row is clickable, not just a button. Easier on the thumb.
            itemView.setOnClickListener { onClick(hunt) }
        }
    }

    companion object {
        /**
         * Tells DiffUtil how to compare two lists. id-equality means "same row"
         * (so animate a move/update). content-equality means "no visible change".
         */
        private val DIFF = object : DiffUtil.ItemCallback<Hunt>() {
            override fun areItemsTheSame(a: Hunt, b: Hunt) = a.id == b.id
            override fun areContentsTheSame(a: Hunt, b: Hunt) = a == b
        }
    }
}
