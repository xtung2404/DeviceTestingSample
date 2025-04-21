package com.example.devicetestingsample.UI.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.devicetestingsample.databinding.LayoutItemTestingDeviceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.FirmwareVersionCheckingCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class TestingDevicesAdapter(): ListAdapter<IoTDevice, TestingDevicesAdapter.TestingDevicesViewHolder>(
    object: DiffUtil.ItemCallback<IoTDevice>() {
        override fun areItemsTheSame(oldItem: IoTDevice, newItem: IoTDevice): Boolean {
            return oldItem.mac == newItem.mac
        }

        override fun areContentsTheSame(oldItem: IoTDevice, newItem: IoTDevice): Boolean {
            return oldItem.mac == newItem.mac
        }
    }
) {
    private val deviceProgressMap = mutableMapOf<String, Int>()

    inner class TestingDevicesViewHolder(private val binding: LayoutItemTestingDeviceBinding):
            RecyclerView.ViewHolder(binding.root) {
                fun bindData(device: IoTDevice) {
                    binding.apply {
                        txtLabel.text = device.label
                        txtMac.text = device.mac
                        txtVersion.text = "Current version: "
                        txtLastestVersion.text = "Lastest version: "
                        btnRefresh.visibility = View.GONE
                        getFwVersion(device)
                        btnRefresh.setOnClickListener {
                            txtVersion.text = "Current version: "
                            txtLastestVersion.text = "Lastest version: "
                            getFwVersion(device)
                        }
                    }
                }
                fun getFwVersion(device: IoTDevice) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.apply {
                            SmartSdk.deviceHandler().checkNewFirmVersion(
                                device.uuid,
                                object: FirmwareVersionCheckingCallback {
                                    override fun onSuccess(currentVer: String?, newVer: String?) {
                                        txtVersion.text = "Current version: ${currentVer}"
                                        txtLastestVersion.text = "Lastest version: ${newVer}"
                                    }

                                    override fun onFailure(
                                        currentVer: String?,
                                        code: Int,
                                        msg: String?
                                    ) {
                                        txtVersion.text = "Current version: ${currentVer}"
                                        txtLastestVersion.text = "Lastest version: ${msg}"
                                        btnRefresh.visibility = View.VISIBLE
                                    }
                                }
                            )
                        }
                    }
                }

            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestingDevicesViewHolder {
        val inflater = LayoutItemTestingDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TestingDevicesViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: TestingDevicesViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    override fun onBindViewHolder(
        holder: TestingDevicesViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when(payloads[0]) { }

        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}