package com.example.rogosample.UI.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.FunctionAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddDeviceBinding
import com.example.rogosample.`object`.Function
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.define.ir.IoTIrPrtc

class AddDeviceFragment : BaseFragment<FragmentAddDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_device

    private val deviceFunctionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.ADDBOX -> {
                    findNavController().navigate(R.id.configBoxFragment)
                }
                Function.ADDBLE -> {
                    findNavController().navigate(R.id.configMeshFragment)
                }
                Function.ADDZIGBEE -> {
                    findNavController().navigate(R.id.configZigbeeFragment)
                }
                Function.ADDWILEDEVICE -> {
                    findNavController().navigate(R.id.configWifiDeviceDirectFragment)
                }
                Function.ADDIRREMOTE -> {
                    findNavController().navigate(R.id.learnIRFragment)
                }
                Function.ADDRFDEVICE -> {
                    findNavController().navigate(R.id.configRfDeviceFragment)
                }
                else -> {
                    findNavController().navigate(R.id.learnIrAcFragment)
                }
            }
        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_device)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            rvFunction.adapter = deviceFunctionAdapter
            deviceFunctionAdapter.submitList(Function.getAddDeviceFuncs())
        }
    }
}