package com.example.rogosample.UI.device

import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentConfigMeshBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.CheckDeviceAvailableCallback
import rogo.iot.module.rogocore.sdk.callback.CheckListDeviceAvailableCallback
import rogo.iot.module.rogocore.sdk.callback.ConnectMeshDeviceCallback
import rogo.iot.module.rogocore.sdk.callback.DiscoveryIoTBleCallback
import rogo.iot.module.rogocore.sdk.callback.LoadMeshNwkCallback
import rogo.iot.module.rogocore.sdk.callback.SetupDeviceMeshCallback
import rogo.iot.module.rogocore.sdk.entity.IoTBleScanned
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup
import rogo.iot.module.rogocore.sdk.entity.IoTProductModel

class ConfigMeshFragment : BaseFragment<FragmentConfigMeshBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_config_mesh
    private val deviceList = arrayListOf<IoTDevice>()
    private val deviceMap = hashMapOf<String, IoTBleScanned>()
    private var ioTBleScanned: IoTBleScanned? = null
    private val TAG = "ConfigMeshFragment"

    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter
    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        deviceList.clear()
        binding.apply{
            btnAddDevice.visibility = View.INVISIBLE
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_ble)
            spinnerGroup.adapter = groupSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        /*
        * Check available hub
        * */
        CoroutineScope(Dispatchers.Main).launch {
            SmartSdk.configMeshDeviceHandler().checkMeshGatewayAvailable(6, object : CheckListDeviceAvailableCallback {
                override fun onDeviceAvailable(centralList: MutableCollection<IoTDevice>?) {
                    centralList?.let {
                        deviceList.addAll(it)
                    }
                }
            })
            delay(5000)
            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
            binding.spinnerHub.adapter = deviceSpinnerAdapter
            ILogR.D(TAG, "HUB_DEVICE", deviceList.size)
        }

        binding.apply {
            btnStartScan.setOnClickListener {
                dialogLoading.show()
                startScan()
            }

            btnAddDevice.setOnClickListener {
                setUpDevice(ioTBleScanned!!)
            }
        }
    }

    private fun startScan() {
        deviceMap.clear()
        /*
        * Scan nearby BLE devices of which RSSI are greater than -70. Check if Bluetooth is available.
        * */
        SmartSdk.configMeshDeviceHandler().discoveryMeshDevice(object : DiscoveryIoTBleCallback {
            override fun onMeshDeviceFound(ioTBleScanned: IoTBleScanned?) {
                ioTBleScanned?.let {
                    if (it.ioTProductModel != null && it.rssi > -70) {
                        deviceMap[it.mac] = it
                    }
                    ILogR.D("deviceAvailable", it.mac, Gson().toJson(it.ioTProductModel))
                }
                dialogLoading.dismiss()
            }

            override fun onRequirePermision() {
                dialogLoading.dismiss()

            }

            override fun onRequireEnableBluetooth() {
                dialogLoading.dismiss()
                showNoti(R.string.turn_on_the_bluetooth)
            }

            override fun onBluetoothError() {
                dialogLoading.dismiss()
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            if (deviceMap.isNotEmpty()) {
                pickBestDevice()
                dialogLoading.dismiss()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            stopDiscovery()
            if (deviceMap.isNotEmpty()) {
                pickBestDevice()
                dialogLoading.dismiss()
            } else {
                dialogLoading.dismiss()
                if (ioTBleScanned == null) {
                    showNoti(R.string.no_available_device)
                }
            }
        }
    }

    /*
    * Stop scan devices
    * */
    private fun stopDiscovery() {
        SmartSdk.configMeshDeviceHandler().stopDiscovery()
    }

    /*
    * Get the closest device
    * */
    private fun pickBestDevice() {
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
            startConfig(deviceMap[bestDevice]!!)
            deviceMap.remove(bestDevice)
        }
    }

    /*
    * config the device scanned
    * */
    private fun startConfig(ioTBleScanned: IoTBleScanned) {
        this.ioTBleScanned = ioTBleScanned
        binding.edtDeviceName.setText(ioTBleScanned.ioTProductModel.name)
        binding.lnDevice.visibility = View.VISIBLE
        setUpDevice(ioTBleScanned)
    }

    /*
    * Set up the device scanned
    * */
    private fun setUpDevice(ioTBleScanned: IoTBleScanned) {
        dialogLoading.show()
        SmartSdk.configMeshDeviceHandler().connectAndIdentifyDevice(
            (binding.spinnerHub.selectedItem as IoTDevice).uuid,
            ioTBleScanned,
            object : ConnectMeshDeviceCallback {
                override fun onConnectionFailure(p0: Int, p1: String?) {

                }

                override fun onConnecting() {

                }

                override fun onConnected() {

                }

                override fun onDeviceIdentifiedAndReadyForSetup() {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.btnAddDevice.visibility = View.VISIBLE
                    }
                    SmartSdk.configMeshDeviceHandler().setupDeviceIntoMeshNetwork(object  : SetupDeviceMeshCallback{
                        override fun onSetupMeshProgressStatus(p0: String?, p1: Int, p2: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.txtProgress.text = p1.toString()
                            }
                        }

                        override fun onSetupMeshConfigurationCompleted() {
                            SmartSdk.configMeshDeviceHandler().syncDeviceInfoToCloud(
                                binding.edtDeviceName.text.toString(),
                                (binding.spinnerGroup.selectedItem as IoTGroup).uuid,
                                ioTBleScanned.ioTProductModel.devSubType,
                                object : RequestCallback<IoTDevice> {
                                    override fun onSuccess(p0: IoTDevice?) {
                                        dialogLoading.dismiss()
                                        showNoti(R.string.add_success)
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {
                                        dialogLoading.dismiss()
                                        p1?.let {
                                            showNoti(it)
                                        }
                                    }

                                }
                            )
                        }

                        override fun onSetupFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            p1?.let {
                                showNoti(it)
                            }
                        }
                })

            }

        })
    }
}