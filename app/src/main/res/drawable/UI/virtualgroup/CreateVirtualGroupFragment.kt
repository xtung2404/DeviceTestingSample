package com.example.rogosample.UI.virtualgroup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentCreateVirtualGroupBinding
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class CreateVirtualGroupFragment : BaseFragment<FragmentCreateVirtualGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_create_virtual_group

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnAddGroup.setOnClickListener {
                SmartSdk.groupHandler().createGroupCtl(
                    edtGroupName.text.toString(),
                    object : RequestCallback<IoTGroup> {
                        override fun onSuccess(p0: IoTGroup?) {
                            ILogR.D("CreateVirtualGroup", "onSuccess")
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            ILogR.D("CreateVirtualGroup", "onFailure", p0, p1)
                        }

                    }
                )
            }
        }
    }

}