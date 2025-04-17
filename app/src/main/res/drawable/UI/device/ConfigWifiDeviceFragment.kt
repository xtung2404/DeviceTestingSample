package com.example.rogosample.UI.device

import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.adapter.WifiSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentConfigIRBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.callback.SuccessStatus
import rogo.iot.module.platform.define.IoTWifiConnectionState
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.SetupDeviceWileCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup
import rogo.iot.module.rogocore.sdk.entity.IoTWileScanned
import rogo.iot.module.rogocore.sdk.entity.SetupDeviceInfo
@Deprecated("Change to ConfigWifiDeviceDirect")
class ConfigIRFragment : BaseFragment<FragmentConfigIRBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_config_i_r

//    private val deviceMap = hashMapOf<String, IoTWileScanned>()
//    private val wifiMap = hashMapOf<String, Int>()
//    private val wifiList = arrayListOf<String?>()
//    private var ioTWileScanned: IoTWileScanned? = null
//    private lateinit var wifiSpinnerAdapter: WifiSpinnerAdapter
//    private val groupSpinnerAdapter by lazy {
//        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toList())
//    }
//
//    override fun initVariable() {
//        super.initVariable()
//        binding.apply {
//            toolbar.btnBack.setOnClickListener {
//                findNavController().popBackStack()
//            }
//            toolbar.txtTitle.text = resources.getString(R.string.add_wile_device)
//            spinnerGroup.adapter = groupSpinnerAdapter
//        }
//    }
//
//    override fun initAction() {
//        super.initAction()
//        binding.apply {
//            btnStartScan.setOnClickListener {
//                dialogLoading.show()
//                startScan()
//            }
//
//            btnConnect.setOnClickListener {
//                lnDevice.visibility = View.GONE
//                binding.lnSetup.visibility = View.VISIBLE
//            }
//            btnAddDevice.setOnClickListener {
//                setUp()
//            }
//            /*
//            * set up Internet for IR
//            * */
//            btnSetWifi.setOnClickListener {
//                SmartSdk.configWileDeviceHandler().setWifiPwd(
//                    0,
//                    (spinnerWifi.selectedItem as String),
//                    edtPassword.text.toString(),
//                    true
//                )
//            }
//        }
//    }
//
//
//    private fun startScan() {
//        stopDiscovery()
//        deviceMap.clear()
//        /*
//        * Scan for available Wile devices
//        * */
//        SmartSdk.configWileDeviceHandler().discoveryWileDevice { ioTWileScanned ->
//            ioTWileScanned?.let {
//                if (it.ioTProductModel != null && it.rssi > -90) {
//                    deviceMap[it.mac] = it
//                }
//            }
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(15000)
//            if (deviceMap.isNotEmpty()) {
//                pickBestDevice()
//                stopDiscovery()
//                dialogLoading.dismiss()
//            } else {
//                dialogLoading.dismiss()
//                showNoti(R.string.no_available_device)
//            }
//        }
//    }
//
//    /*
//    * Stop scan
//    * */
//    private fun stopDiscovery() {
//        SmartSdk.configWileDeviceHandler().stopDiscovery()
//    }
//
//    /*
//    * Get the closest device
//    * */
//    private fun pickBestDevice() {
//        stopDiscovery()
//        var bestDevice = ""
//        var bestRssi = -9999
//        for (entry in deviceMap.entries) {
//            if (entry.value.rssi > bestRssi) {
//                bestDevice = entry.key
//                bestRssi = entry.value.rssi
//                if (bestRssi > -45) {
//                    break
//                }
//            }
//        }
//        if (deviceMap.isNotEmpty()) {
//            startConfig(deviceMap[bestDevice]!!)
//            deviceMap.remove(bestDevice)
//        }
//    }
//
//    /*
//    * Config the device scanned
//    * */
//    private fun startConfig(ioTWileScanned: IoTWileScanned) {
//        this.ioTWileScanned = ioTWileScanned
//        stopDiscovery()
//        binding.lnDevice.visibility = View.VISIBLE
//        binding.txtDeviceName.text = ioTWileScanned.ioTProductModel.name
//        binding.edtDeviceName.setText(ioTWileScanned.ioTProductModel.name)
//        SmartSdk.configWileDeviceHandler().identifyDevice(ioTWileScanned, object : SuccessStatus {
//            override fun onStatus(p0: Boolean) {
//                if (p0) {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        binding.btnAddDevice.visibility = View.VISIBLE
//                    }
//                }
//            }
//
//        })
//    }
//
//    /*
//    * Set up the device scanned
//    * */
//    private fun setUp() {
//        wifiMap.clear()
//        wifiList.clear()
//        ioTWileScanned?.let {
//            SmartSdk.configWileDeviceHandler()
//                .setupWileDevice(it, object : SetupDeviceWileCallback {
//                    override fun onProgress(p0: String?, p1: Int, p2: String?) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            binding.txtProgress.text = p1.toString()
//                            ILogR.D("ConfigWileDevice", p1, p2)
//                            if (p1 == 20) {
//                                SmartSdk.configWileDeviceHandler().scanWifi()
//                                dialogLoading.show()
//                            }
//                            if (p1 == 90) {
//                                SmartSdk.configWileDeviceHandler().completeAddDevice(
//                                    binding.edtDeviceName.text.toString(),
//                                    (binding.spinnerGroup.selectedItem as IoTGroup).uuid,
//                                    it.ioTProductModel.devSubType,
//                                    object : RequestCallback<IoTDevice> {
//                                        override fun onSuccess(p0: IoTDevice?) {
//                                            ILogR.D("ConfigWifiDevice", "onSuccess")
//                                        }
//
//                                        override fun onFailure(p0: Int, p1: String?) {
//                                            ILogR.D("ConfigWifiDevice", "onFailure", p0, p1)
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    override fun onSuccess() {
//                        ILogR.D("ConfigWifiDeviceFragment", "OnWileDeviceConnected")
//                    }
//
//                    override fun onSetupFailure(p0: Int, p1: String?) {
//                        ILogR.D("ConfigWifiDeviceFragment", "OnSetupFaiulure", p0, p1)
//                    }
//
//                    override fun onWifiScanned(p0: String?, p1: Int, p2: Int) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            wifiList.add(p0)
//                        }
//                    }
//
//                    override fun onWifiStopScanned() {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            wifiSpinnerAdapter = WifiSpinnerAdapter(requireContext(), wifiList)
//                            binding.spinnerWifi.adapter = wifiSpinnerAdapter
//                            dialogLoading.dismiss()
//                            binding.lnWifi.visibility = View.VISIBLE
//                        }
//                    }
//
//                    override fun onWifiSetted() {
//                        ILogR.D("ConfigWifiDevice", "onWifiSetted")
//                    }
//
//                    override fun onWifiSsidInfo(p0: Int, p1: String?) {
//                        if (
//                            p0 == IoTWifiConnectionState.SOMETHING_WENT_WRONG ||
//                            p0 == IoTWifiConnectionState.PASSWORD_WRONG ||
//                            p0 == IoTWifiConnectionState.SSID_NOTFOUND
//                        ) {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                showNoti(R.string.wifi_problem)
//                            }
//                        }
//                    }
//                })
//        }
//    }
}