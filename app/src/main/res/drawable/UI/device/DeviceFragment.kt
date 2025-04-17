
package com.example.rogosample.UI.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentDeviceBinding
import com.google.gson.Gson
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi
import rogo.iot.module.platform.ILogR
import rogo.iot.module.rogocore.sdk.SmartSdk

class DeviceFragment : BaseFragment<FragmentDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_device
    private val TAG = "DeviceFragment"
    private val deviceAdapter by lazy {
        DeviceAdapter(
            onControlSelected = {
                findNavController().navigate(R.id.controlDeviceFragment, bundleOf("uuid" to it))
        },
            onStateSelected = {
                findNavController().navigate(R.id.stateDeviceFragment, bundleOf("uuid" to it))
            }
        )
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.get_list_device)
            rvDevice.adapter = deviceAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnGetList.setOnClickListener {
                /*
                * Get all devices
                * */
                val list = SmartSdk.deviceHandler().all.toMutableList()
                list.forEach {
                    ILogR.D(TAG, "device", Gson().toJson(it))
                }
                if(list.isEmpty()) {
                    lnDevices.visibility = View.GONE
                    lnEmpty.visibility = View.VISIBLE
                    rvDevice.visibility = View.GONE
                } else {
                    lnDevices.visibility = View.VISIBLE
                    lnEmpty.visibility = View.GONE
                    rvDevice.visibility = View.VISIBLE
                    deviceAdapter.submitList(list)
                }
            }
        }
    }
}