package com.gdsc.medimedi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.medimedi.databinding.ListItemResultBinding
import com.gdsc.medimedi.model.SearchResult

class ResultAdapter: RecyclerView.Adapter<ResultAdapter.ViewHolder>(){
    var dataSet = mutableListOf<SearchResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(val binding: ListItemResultBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: SearchResult){
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.des
        }
    }
}