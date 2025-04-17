package com.example.devicetestingsample.UI.authentication

import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.databinding.FragmentSignInBinding
import rogo.iot.module.rogocore.basesdk.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTLocation
import rogo.iot.module.rogocore.sdk.entity.auth.SignInDefaultEmailPhoneMethod

class SignInFragment : BaseFragment<FragmentSignInBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_sign_in

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnSignIn.setOnClickListener {
                var username = edtEmail.text.toString()
                var password = edtPassword.text.toString()
                username = "tungrogo24@gmail.com"
                password = "123456"
                if (
                    username.isNotEmpty() &&
                    password.length >= 6
                ) {
                    SmartSdk.signIn(
                        SignInDefaultEmailPhoneMethod(
                            username,
                            password
                        ),
                        object : AuthRequestCallback {
                            override fun onSuccess() {
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
                            }

                            override fun onFailure(p0: Int, p1: String?) {

                            }
                        }
                    )
                }
            }

            btnSignUp.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }
    }
}