package com.example.rogosample.UI.authen

import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentForgotPasswordBinding
import rogo.iot.module.cloudapi.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk

class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_forgot_password

    private var code: String?= null
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.forgot_password)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Pass the user's email to get verify code from Gmail
            * */
            btnContinue.setOnClickListener {
                val email = edtEmail.text.toString()
                if(email.isNullOrEmpty()) {
                    showNoti(R.string.enter_your_email)
                } else {
                    SmartSdk.forgot(email, object : AuthRequestCallback {
                        override fun onSuccess() {
                            lnVerification.visibility = View.VISIBLE
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    })
                }
            }
            btnVerify.setOnClickListener {
                code = edtCode.text.toString()
                if(code.isNullOrEmpty()) {
                    showNoti(R.string.enter_verification_code)
                } else {
                    lnNewPassword.visibility = View.VISIBLE
                }
            }
            /*
            * To set a new password, verify the code and then update the new password
            * */
            btnUpdate.setOnClickListener {
                val password = edtPassword.text.toString()
                if(password.isNullOrEmpty()) {
                    showNoti(R.string.enter_new_password)
                } else {
                    dialogLoading.show()
                    SmartSdk.updatePasswordOrVerifyAccount(code, password, object : AuthRequestCallback {
                        override fun onSuccess() {
                            dialogLoading.dismiss()
                            showNoti(R.string.update_success)
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
}