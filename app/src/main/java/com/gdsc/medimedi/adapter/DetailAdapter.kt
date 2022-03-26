package com.gdsc.medimedi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.medimedi.databinding.ListItemDetailBinding
import com.gdsc.medimedi.model.Detail

class DetailAdapter: RecyclerView.Adapter<DetailAdapter.ViewHolder>() {
    var dataSet = mutableListOf<Detail>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(val binding: ListItemDetailBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Detail){
            binding.tvCate.text = item.cate
            binding.tvDesc.text = item.desc
        }
    }
}