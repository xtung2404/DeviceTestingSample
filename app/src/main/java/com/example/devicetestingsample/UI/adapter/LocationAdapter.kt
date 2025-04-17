package com.example.devicetestingsample.UI.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.devicetestingsample.databinding.LayoutItemLocationBinding
import rogo.iot.module.rogocore.sdk.entity.IoTLocation

class LocationAdapter(
    private val onItemClick: (IoTLocation) -> Unit
): ListAdapter<IoTLocation, LocationAdapter.LocationViewHolder>(object : DiffUtil.ItemCallback<IoTLocation>() {
    override fun areItemsTheSame(oldItem: IoTLocation, newItem: IoTLocation): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: IoTLocation, newItem: IoTLocation): Boolean {
        return oldItem == newItem
    }

}) {
    inner class LocationViewHolder(private val binding: LayoutItemLocationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindData(location: IoTLocation) {
            binding.apply {
                txtName.text = location.label
                txtType.text = location.desc
                itemView.setOnClickListener {
                    onItemClick.invoke(location)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val inflater = LayoutItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

}