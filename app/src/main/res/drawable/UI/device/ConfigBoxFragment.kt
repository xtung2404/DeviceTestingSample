package com.example.rogosample.UI.device

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentConfigBoxBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.RequestAddGatewayCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class ConfigBoxFragment : BaseFragment<FragmentConfigBoxBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_config_box

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                SmartSdk.configGatewayDeviceHandler().cancel()
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_box)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnAddDevice.setOnClickListener {
                /*
                * Get a 6 digit code to let user pass on FPT Box
                * */

                SmartSdk.configGatewayDeviceHandler().setupGatewayDevice("verifyId", object : RequestAddGatewayCallback {
                    override fun onVerifyCodeGenerated(code: String?) {
                        btnAddDevice.visibility = View.GONE
                        lnOtp.visibility = View.VISIBLE
                        code?.let {
                            val otpList = listOf<EditText>(edtOtp1, edtOtp2, edtOtp3, edtOtp4, edtOtp5, edtOtp6)
                            for((i,value) in it.withIndex()) {
                                otpList[i].setText(value.toString())
                            }
                        }
                    }

                    override fun onTimeCountDown(time: Int) {
                        txtTimeout.text = time.toString()
                        if(time == 0) {
                            btnAddDevice.visibility = View.VISIBLE
                        }
                    }

                    override fun onSuccess(ioTDevice: IoTDevice?) {
                        findNavController().navigate(R.id.addDeviceFragment)
                        showNoti(getString(R.string.connect_to, ioTDevice?.label))
                    }

                    override fun onFailure(errorCode: Int, errorMsg: String?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            showNoti(getString(R.string.cannot_connect, errorMsg))
                        }
                    }

                    override fun onCancelled() {

                    }

                })
            }
        }
    }
}