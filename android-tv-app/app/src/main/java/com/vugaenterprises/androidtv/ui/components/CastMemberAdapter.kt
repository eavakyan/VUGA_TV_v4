package com.vugaenterprises.androidtv.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.CastItem

class CastMemberAdapter(
    private val castMembers: List<CastItem>,
    private val onCastMemberClick: (CastItem) -> Unit
) : RecyclerView.Adapter<CastMemberAdapter.CastMemberViewHolder>() {

    class CastMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val castMemberImage: ImageView = itemView.findViewById(R.id.castMemberImage)
        val castMemberName: TextView = itemView.findViewById(R.id.castMemberName)
        val castMemberCharacter: TextView = itemView.findViewById(R.id.castMemberCharacter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cast_member_item, parent, false)
        return CastMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastMemberViewHolder, position: Int) {
        val castMember = castMembers[position]
        
        // Load actor image with circular crop
        holder.castMemberImage.load(castMember.actor.image) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
            transformations(CircleCropTransformation())
        }
        
        // Set actor name
        holder.castMemberName.text = castMember.actor.name
        
        // Set character name
        if (castMember.characterName.isNotEmpty()) {
            holder.castMemberCharacter.text = castMember.characterName
            holder.castMemberCharacter.visibility = View.VISIBLE
        } else {
            holder.castMemberCharacter.visibility = View.GONE
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onCastMemberClick(castMember)
        }
        
        // Set focus change listener for scaling effect
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.itemView.scaleX = 1.1f
                holder.itemView.scaleY = 1.1f
            } else {
                holder.itemView.scaleX = 1.0f
                holder.itemView.scaleY = 1.0f
            }
        }
    }

    override fun getItemCount(): Int = castMembers.size
} 