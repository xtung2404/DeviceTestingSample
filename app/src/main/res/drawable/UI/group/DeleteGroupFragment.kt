package com.example.rogosample.UI.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentDeleteGroupBinding
import com.example.rogosample.databinding.FragmentDeleteLocationBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class DeleteGroupFragment : BaseFragment<FragmentDeleteGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_delete_group

    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.delete_group)
            spinnerGroup.adapter = groupSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnDeleteGroup.setOnClickListener {
                /*
                * Delete a group, pass the UUID of the group
                * */
                SmartSdk.groupHandler().delete(
                    (spinnerGroup.selectedItem as IoTGroup).uuid,
                    object : RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            showNoti(R.string.delete_success)
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            p1?.let{
                                showNoti(it)
                            }
                        }

                    }
                )
            }
        }
    }
}