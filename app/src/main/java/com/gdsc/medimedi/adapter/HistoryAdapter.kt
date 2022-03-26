package com.gdsc.medimedi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.medimedi.databinding.ListItemHistoryBinding
import com.gdsc.medimedi.model.History

class HistoryAdapter: RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    var dataSet = mutableListOf<History>()

    interface OnItemClickListener{
        fun onItemClick(v: View, data: History, pos: Int)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(val binding: ListItemHistoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: History){
            binding.tvId.text = item.id.toString() // Int -> String
            binding.tvName.text = item.name
            binding.tvDate.text = item.date.toString() // Date -> String

            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                itemView.setOnClickListener{
                    listener?.onItemClick(itemView, item, pos)
                }
            }
        }
    }
}