package com.example.rogosample.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentSplashBinding
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.SmartSdkConnectCallback

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_splash

    override fun initAction() {
        super.initAction()
        SmartSdk.connectService(object : SmartSdkConnectCallback {
            override fun onConnected(isAuthenticated: Boolean) {
                if(isAuthenticated) {
                    if (SmartSdk.getAppLocation() != null) {
                        findNavController().navigate(R.id.generalFunction)
                    } else {
                        findNavController().navigate(R.id.locationFunctionFragment)
                    }
                } else {
                    findNavController().navigate(R.id.functionFragment)
                }
            }

            override fun onDisconnected() {
                SmartSdk.closeConnectionService()
            }
        })
    }
}