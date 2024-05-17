package com.example.safezoneadmin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load

import com.example.safezoneadmin.databinding.PersonItemBinding


class PersonAdapter( var datalist:ArrayList<PersonModel>,var context: Context) : RecyclerView.Adapter<PersonAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var binding=PersonItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.imageView5.load(datalist.get(position).ImageUrl)
        holder.itemView.setOnClickListener {
            var intent=Intent(context,AdminImageView::class.java)
            intent.putExtra("documentId", datalist.get(position).id)
            intent.putExtra("image",datalist.get(position).ImageUrl)
            intent.putExtra("disp",datalist.get(position).disp)
            intent.putExtra("latitute",datalist.get(position).lati)
            intent.putExtra("longitute",datalist.get(position).longi)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }



    inner class MyViewHolder(var binding:PersonItemBinding): RecyclerView.ViewHolder(binding.root)
}