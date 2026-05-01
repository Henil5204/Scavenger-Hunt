package ca.gbc.comp3074.scavengerhunt

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhunt.data.Hunt

/**
 * RecyclerView adapter for the hunt list.
 *
 * Going with ListAdapter (not bare RecyclerView.Adapter) so DiffUtil handles
 * the per-row animation when the list changes — way nicer than a full-list flicker.
 */
class HuntAdapter(
    private val onClick: (Hunt) -> Unit,
    private val onLongPress: (Hunt) -> Unit
) : ListAdapter<Hunt, HuntAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_hunt, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position), onClick, onLongPress)

    /** ViewHolder caches findViewById once per row — that's the whole point. */
    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name     = itemView.findViewById<TextView>(R.id.huntName)
        private val tag      = itemView.findViewById<TextView>(R.id.huntTag)
        private val location = itemView.findViewById<TextView>(R.id.huntLocation)
        private val rating   = itemView.findViewById<RatingBar>(R.id.huntRating)
        private val done     = itemView.findViewById<ImageView>(R.id.huntDone)

        fun bind(hunt: Hunt, onClick: (Hunt) -> Unit, onLong: (Hunt) -> Unit) {
            name.text = hunt.name
            tag.text  = hunt.tag.ifBlank { "untagged" }

            // join "address, city" — but skip empty parts so we don't end up with stray commas
            location.text = listOf(hunt.address, hunt.city)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifEmpty { "—" }

            rating.rating = hunt.rating

            // visual flag for done: green check + strikethrough title + dim
            if (hunt.completed) {
                done.visibility = View.VISIBLE
                name.paintFlags = name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                itemView.alpha = 0.55f
            } else {
                // important: REMOVE the flag too, otherwise reused VHs keep it
                done.visibility = View.GONE
                name.paintFlags = name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemView.alpha = 1f
            }

            itemView.setOnClickListener { onClick(hunt) }
            itemView.setOnLongClickListener { onLong(hunt); true }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Hunt>() {
            override fun areItemsTheSame(a: Hunt, b: Hunt) = a.id == b.id
            override fun areContentsTheSame(a: Hunt, b: Hunt) = a == b
        }
    }
}
