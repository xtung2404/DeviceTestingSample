package com.example.rogosample.UI.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentConfigZigbeeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTDeviceSubType
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.CheckDeviceAvailableCallback
import rogo.iot.module.rogocore.sdk.callback.PairZigbeeDeviceCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup
import rogo.iot.module.rogocore.sdk.entity.IoTPairedZigbeeDevice

class ConfigZigbeeFragment : BaseFragment<FragmentConfigZigbeeBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_config_zigbee
    private val deviceList = arrayListOf<IoTDevice>()
    private var scanningTime = 60
    private val deviceMap = hashMapOf<String, IoTPairedZigbeeDevice>()
    private var device: IoTPairedZigbeeDevice?= null
    private var gatewayId = ""
    private var deviceSubType = -1
    private val devType = IoTDeviceType.USB_DONGLE
    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter
    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toMutableList())
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            deviceList.clear()
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_zigbee_device)
            spinnerGroup.adapter = groupSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.btnStartScan.setOnClickListener {
            startScan()
        }
        binding.spinnerHub.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.btnStartScan.visibility = View.VISIBLE
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                binding.btnStartScan.visibility = View.GONE
            }

        }
        CoroutineScope(Dispatchers.Main).launch {
            SmartSdk.configZigbeeDeviceHandler().checkZigbeeGatewayAvailable (object: CheckDeviceAvailableCallback  {
                override fun onDeviceAvailable(p0: String?) {
                    deviceList.add(SmartSdk.deviceHandler().get(p0))
                }
            })
            delay(5000)
            SmartSdk.configZigbeeDeviceHandler().cancelCheckZigbeeGatewayAvailable()
            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
            binding.spinnerHub.adapter = deviceSpinnerAdapter
        }
        binding.btnAddDevice.setOnClickListener {
            setUp()
        }
        binding.btnCurtain.setOnClickListener {
            deviceSubType = IoTDeviceSubType.DC_CTL_CURTAIN_TYPE
        }
        binding.btnGate.setOnClickListener {
            deviceSubType = IoTDeviceSubType.DC_CTL_GATE_TYPE
        }
    }
    private fun startScan() {
        stopDiscovery()
        gatewayId = (binding.spinnerHub.selectedItem as IoTDevice).uuid
        deviceMap.clear()
        SmartSdk.configZigbeeDeviceHandler().startPairZigbeeDevice(gatewayId, scanningTime, devType, object : PairZigbeeDeviceCallback {
            override fun onPairingStatus(p0: Int) {

            }

            override fun onPairedDevice(p0: IoTPairedZigbeeDevice?) {
                device = p0
                binding.edtDeviceName.setText(p0!!.ioTProductModel.name)
                binding.lnDevice.visibility = View.VISIBLE
                stopDiscovery()
            }

            override fun onPairedUnknownDevice(p0: String?, p1: String?, p2: String?) {

            }

            override fun onNotDevicePaired() {

            }
        })
        CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            stopDiscovery()
            if(device == null) {
                showNoti(R.string.no_available_device)
            }
        }
    }
    private fun stopDiscovery() {
        SmartSdk.configZigbeeDeviceHandler().stopPairZigbeeDevice(gatewayId)
    }
    private fun setUp() {
        dialogLoading.show()
        device?.let {
            SmartSdk.configZigbeeDeviceHandler().syncDeviceToCloud(
                gatewayId, it,binding.edtDeviceName.text.toString(),
                (binding.spinnerGroup.selectedItem as IoTGroup).uuid,
                deviceSubType,
                object: RequestCallback<IoTDevice> {
                    override fun onFailure(errorCode: Int, message: String?) {
                        dialogLoading.dismiss()
                        message?.let {
                            showNoti(it)
                        }
                    }

                    override fun onSuccess(item: IoTDevice?) {
                        dialogLoading.dismiss()
                        showNoti(getString(R.string.connect_to, item!!.label))
                    }
                }
            )
        }
    }
}