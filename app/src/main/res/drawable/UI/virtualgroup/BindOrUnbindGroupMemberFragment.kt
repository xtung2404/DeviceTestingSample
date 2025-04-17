package com.example.rogosample.UI.virtualgroup

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
import com.example.rogosample.databinding.FragmentBindOrUnbindGroupMemberBinding
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.callback.SuccessStatus
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup
import rogo.iot.module.rogocore.sdk.entity.IoTGroupMember

class BindOrUnbindGroupMemberFragment : BaseFragment<FragmentBindOrUnbindGroupMemberBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_bind_or_unbind_group_member

    var toBindMember: Boolean = false

    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().groupCtls.toList())
    }

    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            arguments?.let {
                toBindMember = it.getBoolean("toBindMember", false)
            }
            if (toBindMember) {
                btnBindUnbind.text = resources.getString(R.string.bind_member)
                deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.toList())
                spinnerDevice.adapter = deviceSpinnerAdapter
            } else {
                btnBindUnbind.text = resources.getString(R.string.unbind_member)
            }
            spinnerGroup.adapter = groupSpinnerAdapter
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    parent?.let {
                        if (!toBindMember) {
                            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(),
                                SmartSdk.groupHandler().getGroupCtlMembers((it.selectedItem as IoTGroup).uuid).map { groupMember ->
                                SmartSdk.deviceHandler().get(groupMember.deviceId)
                            }.toList())
                            spinnerDevice.adapter = deviceSpinnerAdapter
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            btnBindUnbind.setOnClickListener {
                when(toBindMember) {
                    true -> {
                        SmartSdk.groupHandler().bindGroupCtlMember(
                            (spinnerGroup.selectedItem as IoTGroup).uuid,
                            (spinnerDevice.selectedItem as IoTDevice).uuid,
                            (spinnerDevice.selectedItem as IoTDevice).elementIds,
                            object : RequestCallback<IoTGroupMember> {
                                override fun onSuccess(p0: IoTGroupMember?) {
                                    ILogR.D("BindMember", "onSuccess")
                                }

                                override fun onFailure(p0: Int, p1: String?) {
                                    ILogR.D("BindMember", "onFailure", p0, p1)
                                }

                            }
                        )
                    }
                    else -> {
                        SmartSdk.groupHandler().unboundGroupCtlMember(
                            (spinnerGroup.selectedItem as IoTGroup).uuid,
                            (spinnerDevice.selectedItem as IoTDevice).uuid,
                            object : SuccessStatus {
                                override fun onStatus(p0: Boolean) {
                                    if (p0) {
                                        ILogR.D("UnbindMember", "onSuccess")
                                    } else {
                                        ILogR.D("UnbindMember", "onFailure")
                                    }
                                }

                            }
                        )
                    }
                }
            }
        }
    }
}