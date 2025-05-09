package com.example.devicetestingsample.UI.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.devicetestingsample.R
import com.example.devicetestingsample.databinding.LayoutItemConnectedDeviceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.FirmwareVersionCheckingCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class ConnectedDevicesAdapter(
    private val onDeleteDevice: (IoTDevice) -> Unit,
    private val onUpdateFirmware: (IoTDevice, Boolean) -> Unit,
    private val onTesting: (IoTDevice) -> Unit
): ListAdapter<Pair<IoTDevice, Boolean>, ConnectedDevicesAdapter.ConnectedDevicesViewHolder>(
    object: DiffUtil.ItemCallback<Pair<IoTDevice, Boolean>>() {
        override fun areItemsTheSame(
            oldItem: Pair<IoTDevice, Boolean>,
            newItem: Pair<IoTDevice, Boolean>
        ): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: Pair<IoTDevice, Boolean>,
            newItem: Pair<IoTDevice, Boolean>
        ): Boolean {
            return false
        }

    }
) {
    private val deviceProgressMap = mutableMapOf<String, Int>()
    private val testProgressMap = mutableMapOf<String, Boolean>()

    fun updateProgress(uuid: String, progress: Int) {
        deviceProgressMap[uuid] = progress
        val index = currentList.indexOfFirst {
            it.first.uuid == uuid
        }
        if (index != -1) {
            notifyItemChanged(index, "progress")
        }
    }

    fun updateVersion(uuid: String) {
        val index = currentList.indexOfFirst {
            it.first.uuid == uuid
        }
        if (index != -1) {
            notifyItemChanged(index, "version")
        }
    }

    fun updateCheckState(uuid: String) {
        val index = currentList.indexOfFirst {
            it.first.uuid == uuid
        }
        if (index != -1) {
            notifyItemChanged(index, "state")
        }
    }

    fun checkTestingProgress(uuid: String): Boolean {
        val result = testProgressMap[uuid]
        if (result != null) return result
        return false
    }

//    fun notifyItemChanged(uuid: String) {
//        val index = currentList.indexOfFirst {
//            it.uuid == uuid
//        }
//        if (index != -1) {
//            notifyItemChanged(index)
//        }
//    }

    inner class ConnectedDevicesViewHolder(private val binding: LayoutItemConnectedDeviceBinding):
            RecyclerView.ViewHolder(binding.root) {
                fun bindData(device: Pair<IoTDevice, Boolean>) {
                    binding.apply {
                        val currentState = testProgressMap[device.first.uuid]
                        if (currentState == null) {
                            testProgressMap[device.first.uuid] = false
                        }
                        txtLabel.text = device.first.label
                        txtMac.text = device.first.mac
                        txtVersion.text = "Current version: "
                        txtLastestVersion.text = "Lastest version: "
                        btnUpdateFirm.isEnabled = false
                        btnDelete.isEnabled = false
                        btnTest.isEnabled = true
                        btnRefresh.visibility = View.GONE
                        swIsTested.isEnabled = false
                        swIsTested.isChecked = testProgressMap[device.first.uuid] == true
                        txtPercent.text = "${deviceProgressMap[device.first.uuid]} %"
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(4000)
                            getFwVersion(device.first, false)
                        }
                        btnUpdateFirm.setOnClickListener {
                            onUpdateFirmware.invoke(device.first, true)
                        }
                        btnDelete.setOnClickListener {
                            onDeleteDevice.invoke(device.first)
                        }
                        btnRefresh.setOnClickListener {
                            txtVersion.text = "Current version: "
                            txtLastestVersion.text = "Lastest version: "
                            getFwVersion(device.first, false)
                        }
                        btnTest.setOnClickListener {
                            onTesting.invoke(device.first)
                        }
                    }
                }

                fun setProgress(uuid: String, progress: Int) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val device = getItem(position)
                        if (device.first.uuid == uuid) {
                            binding.txtPercent.text = "$progress%"
                        }
                    }
                }

                fun setVersion(uuid: String) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val device = getItem(position)
                        if (device.first.uuid == uuid) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.txtPercent.text = binding.root.context.resources.getString(R.string.update_firmware_successfully)
                                delay(10000)
                                getFwVersion(device.first, false)
                            }
                        }
                    }
                }

                fun setState(uuid: String) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val device = getItem(position)
                        if (device.first.uuid == uuid) {
                            testProgressMap[uuid] = true
                            binding.swIsTested.isEnabled = true
                            binding.swIsTested.isChecked = true
                        }
                    }
                }

                fun getStateCheck(uuid: String): Boolean {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val device = getItem(position)
                        if (device.first.uuid == uuid) {
                            return binding.swIsTested.isChecked
                        }
                    }
                    return false
                }
                fun getFwVersion(device: IoTDevice, isForceUpdating: Boolean) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.apply {
                            SmartSdk.deviceHandler().checkNewFirmVersion(
                                device.uuid,
                                object: FirmwareVersionCheckingCallback {
                                    override fun onSuccess(currentVer: String?, newVer: String?) {
                                        txtVersion.text = "Current version: ${currentVer}"
                                        txtLastestVersion.text = "Lastest version: ${newVer}"
                                        if (!currentVer.contentEquals(newVer)) {
                                            btnUpdateFirm.isEnabled = true
                                            onUpdateFirmware.invoke(device, isForceUpdating)
                                            return
                                        }
                                        btnTest.isEnabled = true
                                        btnDelete.isEnabled = true
                                    }

                                    override fun onFailure(
                                        currentVer: String?,
                                        code: Int,
                                        msg: String?
                                    ) {
                                        txtVersion.text = "Current version: ${currentVer}"
                                        txtLastestVersion.text = "Lastest version: ${msg}"
                                        btnRefresh.visibility = View.VISIBLE
                                        btnDelete.isEnabled = true
                                    }

                                }
                            )
                        }
                    }
                }

            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectedDevicesViewHolder {
        val inflater = LayoutItemConnectedDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConnectedDevicesViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ConnectedDevicesViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ConnectedDevicesViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when(payloads[0]) {
                "progress" -> {
                    val device = getItem(position)
                    val progress = deviceProgressMap[device.first.uuid]?: 0
                    holder.setProgress(device.first.uuid, progress)
                }
                "version" -> {
                    val device = getItem(position)
                    holder.setVersion(device.first.uuid)
                }
                "state" -> {
                    val device = getItem(position)
                    holder.setState(device.first.uuid)
                }
            }

        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}