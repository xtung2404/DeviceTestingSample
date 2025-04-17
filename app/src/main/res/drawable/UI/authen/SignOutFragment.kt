package com.example.rogosample.UI.authen

import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentSignOutBinding
import rogo.iot.module.cloudapi.auth.callback.AuthRequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk

class SignOutFragment : BaseFragment<FragmentSignOutBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_sign_out

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.sign_in)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Sign out
            * */
            btnSignOut.setOnClickListener {
                dialogLoading.show()
                SmartSdk.signOut(object : AuthRequestCallback {
                    override fun onSuccess() {
                        dialogLoading.dismiss()
                        showNoti(R.string.signed_out)
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