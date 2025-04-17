package com.example.rogosample.UI.authen

import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentSignUpBinding
import rogo.iot.module.cloudapi.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk

class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_sign_up

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.sign_up)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * To sign up, user can sign up with or without phone number
            * */
            btnSignUp.setOnClickListener {
                dialogLoading.show()
                val username = edtUsername.text.toString()
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                val phoneNumber = edtPhonenumber.text.toString()
                if(phoneNumber.isNullOrEmpty()) {
                    SmartSdk.signUp(username, email, null, password, object : AuthRequestCallback {
                        override fun onSuccess() {
                            dialogLoading.dismiss()
                            lnVerification.visibility = View.VISIBLE
                            showNoti(resources.getString(R.string.sign_up_success))
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            p1?.let{
                                showNoti(it)
                            }
                        }
                    })
                } else {
                    SmartSdk.signUp(username, email, phoneNumber, password, object : AuthRequestCallback{
                        override fun onSuccess() {
                            dialogLoading.dismiss()
                            lnVerification.visibility = View.VISIBLE
                            showNoti(resources.getString(R.string.sign_up_success))
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            p1?.let{
                                showNoti(it)
                            }
                        }

                    })
                }
            }

            /*
            * Verify the code sent to Gmail
            * */
            btnVerify.setOnClickListener {
                dialogLoading.show()
                val code = edtCode.text.toString()
                SmartSdk.updatePasswordOrVerifyAccount(code, null, object : AuthRequestCallback {
                    override fun onSuccess() {
                        dialogLoading.dismiss()
                        showNoti(resources.getString(R.string.sign_up_success))
                        findNavController().navigate(R.id.signInFragment)
                    }

                    override fun onFailure(p0: Int, p1: String?) {
                        dialogLoading.dismiss()
                        p1?.let {
                            showNoti(it)
                        }
                    }
                })
            }
        }
    }
}
