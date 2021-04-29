package com.iam18.writeit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iam18.writeit.R
import com.iam18.writeit.entities.Notes
import kotlinx.android.synthetic.main.item_notes.view.*


class NotesAdapter():
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>(){
    var listener:OnItemClickListener? = null
    var arrList = ArrayList<Notes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_notes,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.tvTitle.text = arrList[position].title
        holder.itemView.tvDesc.text = arrList[position].noteText
        holder.itemView.tvDateTime.text = arrList[position].dateTime

        if (arrList[position].imgPath != null){
            Glide.with(holder.itemView.context).load(arrList[position].imgPath).override(1280,720).into(holder.itemView.imgNote)
            holder.itemView.imgNote.visibility = View.VISIBLE
        }else{
            holder.itemView.imgNote.visibility = View.GONE
        }

        holder.itemView.cardView.setOnClickListener {
            listener!!.onClicked(arrList[position].id!!)
        }

    }

    fun setData(arrNotesList: List<Notes>){
        arrList = arrNotesList as ArrayList<Notes>
    }

    fun setOnClickListener(listener1: OnItemClickListener){
        listener = listener1
    }

    class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }

    interface OnItemClickListener{
        fun onClicked(noteId: Int)
    }
}