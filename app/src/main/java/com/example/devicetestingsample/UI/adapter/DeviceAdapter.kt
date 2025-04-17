package com.example.devicetestingsample.UI.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.devicetestingsample.databinding.LayoutItemDeviceBinding
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class DeviceAdapter: ListAdapter<IoTDevice, DeviceAdapter.DeviceViewHolder>(
    object : DiffUtil.ItemCallback<IoTDevice>() {
        override fun areItemsTheSame(oldItem: IoTDevice, newItem: IoTDevice): Boolean {
            return oldItem.uuid == newItem.uuid && oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: IoTDevice, newItem: IoTDevice): Boolean {
            return oldItem.uuid == newItem.uuid && oldItem.label == newItem.label
        }
    }
) {
    inner class DeviceViewHolder(private val binding: LayoutItemDeviceBinding):
            RecyclerView.ViewHolder(binding.root) {
                fun bindData(device: IoTDevice) {
                    binding.apply {
                        txtLabel.text = device.label
                    }
                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val inflater = LayoutItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }
}