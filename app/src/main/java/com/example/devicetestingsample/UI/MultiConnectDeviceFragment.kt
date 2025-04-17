package com.example.devicetestingsample.UI

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.adapter.ConnectedDevicesAdapter
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.UI.localdb.PrefsManager
import com.example.devicetestingsample.databinding.FragmentMultiConnectDeviceBinding
import com.google.android.material.button.MaterialButton.OnCheckedChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.rogocore.basesdk.ILogR
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.SetupDeviceWileCallback
import rogo.iot.module.rogocore.sdk.callback.SmartSdkOTADeviceStateCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTWileScanned
import rogo.iot.module.rogocore.sdk.entity.SetupDeviceInfo


class MultiConnectDeviceFragment : BaseFragment<FragmentMultiConnectDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_multi_connect_device
    private val TAG = "MultiConnectDeviceFragment"
    var isScanning = false
    val connectedDevices = arrayListOf<IoTDevice>()
    private var sharedPrefs: PrefsManager?= null
    private val connectedDevicesAdapter: ConnectedDevicesAdapter by lazy {
        ConnectedDevicesAdapter(
            onDeleteDevice = {
                SmartSdk.deviceHandler().delete(
                    it.uuid,
                    object : RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                connectedDevices.remove(it)
                                connectedDevicesAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onFailure(p0: Int, p1: String?) {

                        }

                    })
            },
            onUpdateFirmware = { device, isForceUpdating ->
                CoroutineScope(Dispatchers.Main).launch {
                    binding.apply {
                        if(!isForceUpdating) {
                            if (!switchUpdateFirmware.isChecked) return@launch
                        }
                        SmartSdk.registerOTADeviceStateCallback(otaDeviceStateCallback)
                        SmartSdk.deviceHandler().requestOta(
                            device.uuid,
                            object : RequestCallback<Boolean> {
                                override fun onSuccess(p0: Boolean?) {
                                    ILogR.D(TAG, "ON_UPDATE_OTA_SUCCESS")
                                }

                                override fun onFailure(p0: Int, p1: String?) {

                                }

                            }
                        )
                    }
                }
            },
            onTesting = {

            }
        )
    }

    private val otaDeviceStateCallback: SmartSdkOTADeviceStateCallback = object : SmartSdkOTADeviceStateCallback() {
        override fun onOTAProgress(deviceId: String?, progress: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                deviceId?.let {
                    connectedDevicesAdapter.updateProgress(it, progress)
                }
            }
        }

        override fun onSuccess(deviceId: String?) {
            ILogR.D(TAG, "ON_UPDATE_OTA_SUCCESS_CALLBACK")
            CoroutineScope(Dispatchers.Main).launch {
                deviceId?.let {
                    connectedDevicesAdapter.updateVersion(it)
                }
            }
        }

        override fun onFailure(deviceId: String?, errCode: Int, msg: String?) {

        }

    }

    override fun initVariable() {
        super.initVariable()
        isScanning = false
        connectedDevices.clear()
        binding.apply {
            rvConnectedDevices.adapter = connectedDevicesAdapter
            connectedDevicesAdapter.submitList(connectedDevices)
            lnSetting.visibility = View.GONE
            txtLabel.text = ""
            txtProductid.text = ""
            txtPercent.text = ""
            sharedPrefs = PrefsManager(requireContext())
            sharedPrefs?.let {
                edtSsid.setText(it.getString(KEY_WIFI_SSID))
                edtPassword.setText(it.getString(KEY_WIFI_PASSWORD))
                switchConnect.isChecked = it.getBoolean(KEY_CONNECT)
                switchUpdateFirmware.isChecked = it.getBoolean(KEY_UPDATE_FIRMWARE)
                switchDeleteEnable.isChecked = it.getBoolean(KEY_DELTETE_NOT_OTA)
                switchDeleteTest.isChecked = it.getBoolean(KEY_DELETE_NOT_TESTED)
                edtProductid.setText(it.getString(KEY_PRODUCTID))
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnBack.setOnClickListener {
                stopScan()
                findNavController().navigate(R.id.dashboardFragment)
            }

            btnSettings.setOnClickListener {
                if (lnSetting.visibility == View.GONE) {
                    lnSetting.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                lnSetting.visibility = View.GONE
            }

            edtSsid.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { ssid ->
                        sharedPrefs?.setString(KEY_WIFI_SSID, ssid.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { wifiPass ->
                        sharedPrefs?.setString(KEY_WIFI_PASSWORD, wifiPass.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtProductid.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        s?.let {
                            sharedPrefs?.setString(KEY_PRODUCTID, s.toString())
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                }
            )

            btnDelete.setOnClickListener {
                connectedDevices.forEach {
                    SmartSdk.deviceHandler().delete(
                        it.uuid,
                        object : RequestCallback<Boolean> {
                            override fun onSuccess(p0: Boolean?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    ILogR.D(TAG, "ON_DELETE_DEVICE", "ON_SUCCESS")
                                    connectedDevices.remove(it)
                                    connectedDevicesAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onFailure(p0: Int, p1: String?) {

                            }

                        }
                    )
                }
            }

            btnStartScan.setOnClickListener {
                if (isScanning) {
                    stopScan()
                    resetUI()
                    btnStartScan.text = context?.resources?.getString(R.string.start_scanning_for_devices)
                    isScanning = false
                } else {
                    btnStartScan.text = context?.resources?.getString(R.string.stop_scanning)
                    isScanning = true
                    connectedDevices.clear()
                    startScan()
                }
            }
            switchConnect.setOnCheckedChangeListener(
                object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        sharedPrefs?.setBoolean(KEY_CONNECT, isChecked)
                    }

                }
            )

            switchUpdateFirmware.setOnCheckedChangeListener(
                object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        sharedPrefs?.setBoolean(KEY_UPDATE_FIRMWARE, isChecked)
                    }

                }
            )
        }
    }

    fun startScan() {
        SmartSdk.configWileHandler().discoveryWileDevice {
            it?.let { ioTWileScanned ->
                if (ioTWileScanned.ioTProductModel.modelId.contentEquals(binding.edtProductid.text)) {
                    startConfig(it)
                    stopScan()
                }
            }
        }
    }

    fun startConfig(
        ioTWileScanned: IoTWileScanned
    ) {
        binding.apply {
            val scannedModel = SmartSdk.getProductModel(ioTWileScanned.ioTProductModel.modelId)
            scannedModel?.let {
                txtLabel.text = it.name
                txtProductid.text = it.modelId
            }
        }
        val setUpDeviceInfo = SetupDeviceInfo(
            ioTWileScanned.device.address,
            ioTWileScanned.device.name,
            null,
            ioTWileScanned.ioTProductModel.modelId
        )
        SmartSdk.configWileHandler().startSetupWileDevice(
            setUpDeviceInfo,
            object : SetupDeviceWileCallback {
                override fun onProgress(id: String?, progress: Int, msg: String?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.txtPercent.text = "${progress} %"
                        if (progress == 20) {
                            SmartSdk.configWileHandler().setWifiPwd(
                                binding.edtSsid.text.toString(),
                                binding.edtPassword.text.toString(),
                                true
                            )
                        }
                    }
                }

                override fun onSuccess(ioTDevice: IoTDevice?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.apply {
                            txtLabel.text = ""
                            txtProductid.text = ""
                            txtPercent.text = ""
                        }
                        ioTDevice?.let {
                            connectedDevices.add(0, it)
                            connectedDevicesAdapter.notifyItemInserted(0)
                            binding.txtNumberConnectedDevices.text = context?.resources?.getString(R.string.number_of_connected_devices) + ": ${connectedDevices.size}"
                            binding.txtNumberUpdatedDevices.text = context?.resources?.getString(R.string.number_of_connected_devices) + ": ${connectedDevices.size}"
                            resetUI()
                            if (!binding.switchConnect.isChecked) {
                                stopScan()
                            } else {
                                startScan()
                            }
                        }
                    }
                }

                override fun onSetupFailure(errorCode: Int, msg: String) {

                }

                override fun onWifiScanned(ssid: String, auth: Int, rssi: Int) {

                }

                override fun onWifiStopScanned() {

                }

                override fun onWifiSetted() {

                }

                override fun onWifiSsidInfo(status: Int, ssid: String?) {

                }

            }
        )
    }

    fun resetUI() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                txtLabel.text = ""
                txtProductid.text = ""
                txtPercent.text = ""
            }
        }
    }

    fun stopScan() {
        SmartSdk.configWileHandler().stopDiscovery()
    }

    companion object {
        val KEY_WIFI_SSID = "WIFI_SSID"
        val KEY_WIFI_PASSWORD = "WIFI_PASSWORD"
        val KEY_PRODUCTID = "ProductId"
        val KEY_CONNECT = "CONNECT_DEVICE"
        val KEY_UPDATE_FIRMWARE = "UPDATE_FIRMWARE"
        val KEY_DELETE_NOT_TESTED = "DELETE_NOT_TESTED"
        val KEY_DELTETE_NOT_OTA = "DELETE_NOT_OTA"
    }
}