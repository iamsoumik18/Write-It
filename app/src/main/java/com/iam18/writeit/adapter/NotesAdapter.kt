package com.iam18.writeit.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iam18.writeit.databinding.ItemNotesBinding
import com.iam18.writeit.entities.Notes


class NotesAdapter :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>(){
    private var listener:OnItemClickListener? = null
    private var arrList = ArrayList<Notes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(ItemNotesBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.binding.tvTitle.text = arrList[position].title
        holder.binding.tvSubTitle.text = arrList[position].subTitle
        holder.binding.tvDateTime.text = arrList[position].dateTime

        if (arrList[position].color != "null"){
            holder.binding.cardView.setBackgroundColor(Color.parseColor(arrList[position].color))
        }else{
            holder.binding.cardView.setBackgroundColor(Color.TRANSPARENT)
        }

        if (arrList[position].imgPath != null){
            Glide.with(holder.itemView.context).load(arrList[position].imgPath).override(1280,720).into(holder.binding.imgNote)
            holder.binding.imgNote.visibility = View.VISIBLE
        }else{
            holder.binding.imgNote.visibility = View.GONE
        }

        holder.binding.cardView.setOnClickListener {
            listener!!.onClicked(arrList[position].id!!)
        }

    }

    fun setData(arrNotesList: List<Notes>){
        arrList = arrNotesList as ArrayList<Notes>
    }

    fun setOnClickListener(listener1: OnItemClickListener){
        listener = listener1
    }

    class NotesViewHolder(val binding: ItemNotesBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener{
        fun onClicked(noteId: Int)
    }
}