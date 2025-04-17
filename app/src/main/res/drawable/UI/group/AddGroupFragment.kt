package com.example.rogosample.UI.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.GroupTypeAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddGroupBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.define.IoTGroupType
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class AddGroupFragment : BaseFragment<FragmentAddGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_group

    private val groupTypeAdapter by lazy {
        GroupTypeAdapter(requireContext(), IoTGroupType.entries.toMutableList())
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_group)
            spinnerGroupType.adapter = groupTypeAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnAddGroup.setOnClickListener {
                val label = edtGroupName.text.toString()
                val groupType = (spinnerGroupType.selectedItem as IoTGroupType).name
                if(label.isNullOrEmpty() || groupType.isNullOrEmpty()) {
                    notify(resources.getString(R.string.enter_group_name))
                } else {
                    /*
                    * To add a new group
                    * Para 1: Label of the new group
                    * Para 2: Name of type of the group
                    * */
                    SmartSdk.groupHandler().createGroup(
                        label,
                        (spinnerGroupType.selectedItem as IoTGroupType).name,
                        object : RequestCallback<IoTGroup> {
                            override fun onSuccess(p0: IoTGroup?) {
                                showNoti(R.string.add_success)
                            }

                            override fun onFailure(p0: Int, p1: String?) {
                                p1?.let {
                                    showNoti(it)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}