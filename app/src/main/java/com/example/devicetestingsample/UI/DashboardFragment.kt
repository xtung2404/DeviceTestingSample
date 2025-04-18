package com.example.devicetestingsample.UI

import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.adapter.DeviceAdapter
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.databinding.FragmentDashboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.rogocore.basesdk.ILogR
import rogo.iot.module.rogocore.basesdk.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk

class DashboardFragment : BaseFragment<FragmentDashboardBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_dashboard

    private val TAG = "DashboardFragment"
    private val deviceAdapter: DeviceAdapter by lazy {
        DeviceAdapter()
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            rvDevice.adapter = deviceAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            val deviceList = SmartSdk.deviceHandler().all.toMutableList()
            if(deviceList.isNotEmpty()) {
                lnEmpty.visibility = View.GONE
                rvDevice.visibility = View.VISIBLE
                deviceAdapter.submitList(deviceList)
            } else {
                lnEmpty.visibility = View.VISIBLE
                rvDevice.visibility = View.GONE
            }

            btnConnect.setOnClickListener {
                findNavController().navigate(R.id.multiConnectDeviceFragment)
            }

            btnDelete.setOnClickListener {
                for (device in deviceList.withIndex()) {
                    SmartSdk.deviceHandler().delete(
                        device.value.uuid,
                        object: RequestCallback<Boolean> {
                            override fun onSuccess(p0: Boolean?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    ILogR.D(TAG, "ON_DELETE_DEVICE", "ON_SUCCESS")
                                    deviceList.remove(device.value)
                                    deviceAdapter.notifyDataSetChanged()
                                    if(deviceList.isNotEmpty()) {
                                        lnEmpty.visibility = View.GONE
                                        rvDevice.visibility = View.VISIBLE
                                        deviceAdapter.submitList(deviceList)
                                    } else {
                                        lnEmpty.visibility = View.VISIBLE
                                        rvDevice.visibility = View.GONE
                                    }
                                }
                            }

                            override fun onFailure(p0: Int, p1: String?) {
                                ILogR.D(TAG, "ON_DELETE_DEVICE", "ON_FAILURE", p0, p1)
                            }
                        })
                }
            }

            btnSignOut.setOnClickListener {
                SmartSdk.signOut(
                    object : AuthRequestCallback {
                        override fun onSuccess() {
                            findNavController().navigate(R.id.signInFragment)
                        }

                        override fun onFailure(p0: Int, p1: String?) {

                        }
                    }
                )
            }
        }
    }
}