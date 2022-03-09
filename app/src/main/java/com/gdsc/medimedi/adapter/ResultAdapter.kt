package com.gdsc.medimedi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.medimedi.databinding.ListItemResultBinding
import com.gdsc.medimedi.model.Result

class ResultAdapter: RecyclerView.Adapter<ResultAdapter.ViewHolder>(){
    var dataSet = mutableListOf<Result>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(val binding: ListItemResultBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: Result){
            binding.tvCate.text = item.category
            binding.tvDesc.text = item.description
        }
    }
}