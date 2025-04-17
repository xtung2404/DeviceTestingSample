package com.example.rogosample.UI.device.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.SwitchDeviceAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentControlSwitchGroupBinding
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class ControlSwitchGroupFragment : BaseFragment<FragmentControlSwitchGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_control_switch_group

    private var switchList: MutableList<IoTDevice> ?= null

    private val switchDeviceAdapter by lazy {
        SwitchDeviceAdapter()
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.control_device)
            rvSwitch.adapter = switchDeviceAdapter
            switchList = SmartSdk.deviceHandler().all.filter {
                it.devType == IoTDeviceType.SWITCH
            }.toMutableList()
            switchDeviceAdapter.submitList(switchList)
        }
    }

    override fun initAction() {
        super.initAction()

    }


}