package com.example.rogosample.UI.device

import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.adapter.WifiSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentConfigIRBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.callback.SuccessRequestCallback
import rogo.iot.module.platform.callback.SuccessStatus
import rogo.iot.module.platform.define.IoTWifiConnectionState
import rogo.iot.module.platform.entity.IoTDirectDeviceInfo
import rogo.iot.module.platform.entity.IoTNetworkConnectivity
import rogo.iot.module.platform.entity.IoTWifiInfo
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.DiscoverySmartDeviceCallback
import rogo.iot.module.rogocore.sdk.callback.SetupDeviceWileCallback
import rogo.iot.module.rogocore.sdk.callback.SetupWileDirectDeviceCallback
import rogo.iot.module.rogocore.sdk.callback.SuccessStatusCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class ConfigWifiDeviceDirectFragment : BaseFragment<FragmentConfigIRBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_config_i_r

    private val TAG = "ConfigWifiDeviceDirectFragment"
    private val deviceMap = hashMapOf<String, IoTDirectDeviceInfo>()
    private val wifiList = arrayListOf<IoTWifiInfo?>()
    private var ioTDirectDeviceInfo: IoTDirectDeviceInfo?= null
    private lateinit var wifiSpinnerAdapter: WifiSpinnerAdapter
    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_wile_device)
            spinnerGroup.adapter = groupSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
//            ILogR.D(TAG, "DEVICE_INFO", Gson().toJson(SmartSdk.getProductModel("00000018340400D6")))
            btnStartScan.setOnClickListener {
                dialogLoading.show()
                startScan()
            }

            btnConnect.setOnClickListener {
                lnDevice.visibility = View.GONE
                binding.lnSetup.visibility = View.VISIBLE
            }
            btnAddDevice.setOnClickListener {
                setUp()
            }
            /*
            * set up Internet for IR
            * */
            btnSetWifi.setOnClickListener {
                SmartSdk.configWileDirectDeviceHandler().requestConnectWifiNetwork(
                    0,
                    edtSsid.text.toString(),
                    edtPassword.text.toString(),
                    true,
                    object : SuccessRequestCallback {
                        override fun onSuccess() {
                            ILogR.D(TAG, "SETWIFI", "onSuccess")
                            SmartSdk.configWileDirectDeviceHandler().setupAndSyncDeviceToCloud(
                                binding.edtDeviceName.text.toString(),
                                (binding.spinnerGroup.selectedItem as IoTGroup).uuid,
                                SmartSdk.getProductModel(ioTDirectDeviceInfo!!.productId).devSubType,
                                object : RequestCallback<IoTDevice> {
                                    override fun onSuccess(p0: IoTDevice?) {
                                        dialogLoading.dismiss()
                                        ILogR.D(TAG, "setupAndSyncDeviceToCloud", "onSuccess")
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {
                                        dialogLoading.dismiss()
                                        ILogR.D(TAG, "setupAndSyncDeviceToCloud", "onFailure", p0, p1)
                                    }

                                }
                            )
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            ILogR.D(TAG, "SETWIFI", "onFailure", p0, p1)
                        }

                    }
                )
            }
        }
    }


    private fun startScan() {
        dialogLoading.show()
        stopDiscovery()
        deviceMap.clear()
        /*
        * Scan for available Wile devices
        * */

        SmartSdk.discoverySmartDeviceHandler().discovery(object : DiscoverySmartDeviceCallback {
            override fun onSmartDeviceFound(ioTDirectDeviceInfo: IoTDirectDeviceInfo?) {
                ioTDirectDeviceInfo?.let {
                    ILogR.D("onDeviceFound", Gson().toJson(it), it.typeConnect)
//                    if (SmartSdk.getProductModel(it.productId) != null && it.rssi > -90) {
//                        if (it.typeConnect != IoTDirectDeviceInfo.TypeConnect.MESH) {
//                            deviceMap[it.mac] = it
//                        }
//                    }
                    if (SmartSdk.getProductModel(it.productId) != null && it.productId == "00000018340400D6") {
                        ILogR.D(TAG, "ONDEVICEWILEFOUND", Gson().toJson(it), it.typeConnect)
                        if (it.typeConnect == IoTDirectDeviceInfo.TypeConnect.WILEDIRECTLAN) {
                            deviceMap[it.mac] = it
                        }
                    }
                }
            }

        })
        CoroutineScope(Dispatchers.Main).launch {
            delay(20000)
            stopDiscovery()
            if (deviceMap.isNotEmpty()) {
                pickBestDevice()
                dialogLoading.dismiss()
            } else {
                dialogLoading.dismiss()
                showNoti(R.string.no_available_device)
            }
        }
    }

    /*
    * Stop scan
    * */
    private fun stopDiscovery() {
        SmartSdk.discoverySmartDeviceHandler().stopDiscovery()
    }

    /*
    * Get the closest device
    * */
    private fun pickBestDevice() {
        stopDiscovery()
        var bestDevice = ""
        var bestRssi = -9999
        for (entry in deviceMap.entries) {
            if (entry.value.rssi > bestRssi) {
                bestDevice = entry.key
                bestRssi = entry.value.rssi
                if (bestRssi > -45) {
                    break
                }
            }
        }
        if (deviceMap.isNotEmpty()) {
            showDeviceInfo(deviceMap[bestDevice]!!)
            deviceMap.remove(bestDevice)
        }
    }

    /*
    * Config the device scanned
    * */
    private fun showDeviceInfo(ioTDirectDeviceInfo: IoTDirectDeviceInfo) {
        this.ioTDirectDeviceInfo = ioTDirectDeviceInfo
        stopDiscovery()
        val label = SmartSdk.getProductModel(ioTDirectDeviceInfo.productId).name
        binding.lnDevice.visibility = View.VISIBLE
        binding.txtDeviceName.text = label
        binding.edtDeviceName.setText(label)
        binding.btnAddDevice.visibility = View.VISIBLE
    }

    private fun setUp() {
        SmartSdk.configWileDirectDeviceHandler().connectAndIdentifyDevice(ioTDirectDeviceInfo, object : SetupWileDirectDeviceCallback {
            override fun onDeviceIdentifiedAndReadySetup(
                p0: String?,
                p1: MutableCollection<IoTNetworkConnectivity>?
            ) {
                ILogR.D(TAG, "onDeviceIdentifiedAndReadySetup", p0, Gson().toJson(p1))
                wifiList.clear()
                SmartSdk.configWileDirectDeviceHandler().scanWifi(10, object : RequestCallback<Collection<IoTWifiInfo>> {
                    override fun onSuccess(p0: Collection<IoTWifiInfo>?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            p0?.let {
                                wifiList.addAll(it)
                            }
                            wifiSpinnerAdapter = WifiSpinnerAdapter(requireContext(), wifiList)
                            binding.spinnerWifi.adapter = wifiSpinnerAdapter
                            dialogLoading.dismiss()
                            binding.lnWifi.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(p0: Int, p1: String?) {
                        ILogR.D(TAG, "onScanWifiFailure", p0, p1)
                    }

                })
                dialogLoading.show()
            }

            override fun onProgress(p0: Int, p1: String?) {
                ILogR.D(TAG, "onProgress", p0, p1)
                CoroutineScope(Dispatchers.Main).launch {
                    binding.txtProgress.text = p0.toString()
                }
            }

            override fun onSetupFailure(p0: Int, p1: String?) {
                ILogR.D(TAG, "onSetupFailure", "onFailure", p0, p1)
            }

        })

    }
}