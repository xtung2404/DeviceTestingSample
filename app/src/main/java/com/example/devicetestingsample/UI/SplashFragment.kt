package com.example.devicetestingsample.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.databinding.FragmentSplashBinding
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.SmartSdkConnectCallback
import rogo.iot.module.rogocore.sdk.entity.IoTLocation

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_splash

    override fun initAction() {
        super.initAction()
        SmartSdk.connectService(
            context,
            object : SmartSdkConnectCallback {
                override fun onConnected(isAuthenticated: Boolean) {
                    if (isAuthenticated) {
                        val locList = SmartSdk.locationHandler().all
                        if (locList.isNotEmpty()) {
                            SmartSdk.setAppLocation(locList.first().uuid)
                            findNavController().navigate(R.id.dashboardFragment)
                        } else {
                            SmartSdk.locationHandler().createLocation(
                                "default",
                                "",
                                object : RequestCallback<IoTLocation> {
                                    override fun onSuccess(p0: IoTLocation?) {
                                        p0?.let {
                                            SmartSdk.setAppLocation(it.uuid)
                                            findNavController().navigate(R.id.dashboardFragment)
                                        }
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {

                                    }
                                }
                            )
                        }
                    } else {
                        findNavController().navigate(R.id.signInFragment)
                    }
                }

                override fun onDisconnected() {

                }
            }
        )
    }
}