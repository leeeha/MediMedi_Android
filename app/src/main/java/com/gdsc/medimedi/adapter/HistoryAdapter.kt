package com.gdsc.medimedi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.medimedi.databinding.ListItemHistoryBinding
import com.gdsc.medimedi.model.History

class HistoryAdapter(private val onItemClicked: (position: Int) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {
    var dataSet = mutableListOf<History>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataSet[position])

    }

    override fun getItemCount(): Int = dataSet.size

    class MyViewHolder(
        private val binding: ListItemHistoryBinding,
        private val onItemClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val pos = adapterPosition
            onItemClicked(pos)
        }

        fun bind(item: History) {
            binding.tvName.text = item.name
            binding.tvDate.text = item.date
        }
    }
}