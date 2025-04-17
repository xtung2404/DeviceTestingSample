package com.example.devicetestingsample.UI.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.databinding.FragmentSignUpBinding
import rogo.iot.module.rogocore.basesdk.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTLocation
import rogo.iot.module.rogocore.sdk.entity.auth.SignUpDefaultEmailPhoneMethod
import rogo.iot.module.rogocore.sdk.entity.auth.VerifyDefaultEmailPhoneMethod

class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_sign_up

    override fun initVariable() {
        super.initVariable()
        binding.apply {

        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * To sign up, user can sign up with or without phone number
            * */
            btnSignUp.setOnClickListener {
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                if (email.isNotEmpty() && password.length >= 6) {
                    SmartSdk.signUp(
                        SignUpDefaultEmailPhoneMethod(
                            email,
                            password
                        ),
                        object : AuthRequestCallback {
                            override fun onSuccess() {
                                lnVerification.visibility = View.VISIBLE
                            }

                            override fun onFailure(p0: Int, p1: String?) {

                            }
                        })
                }
            }

            /*
            * Verify the code sent to Gmail
            * */
            btnVerify.setOnClickListener {
                val code = edtCode.text.toString()
                SmartSdk.verifySignUp(
                    VerifyDefaultEmailPhoneMethod(
                        code
                    ),
                    object : AuthRequestCallback {
                        override fun onSuccess() {
                            SmartSdk.locationHandler().createLocation(
                                "default",
                                "",
                                object : RequestCallback<IoTLocation> {
                                    override fun onSuccess(p0: IoTLocation?) {
                                        p0?.let {
                                            SmartSdk.setAppLocation(it.uuid)
//                                            findNavController().navigate(R.id.)
                                        }
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {

                                    }

                                }
                            )
                        }

                        override fun onFailure(p0: Int, p1: String?) {

                        }

                    }
                )
            }
        }
    }
}