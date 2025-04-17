package com.example.rogosample.UI.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.adapter.GroupTypeAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditGroupBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.define.IoTGroupType
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class EditGroupFragment : BaseFragment<FragmentEditGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_group

    private val groupAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toMutableList())
    }

    private val groupTypeAdapter by lazy {
        GroupTypeAdapter(requireContext(), IoTGroupType.entries.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_group)
            spinnerGroup.adapter = groupAdapter
            spinnerGroupType.adapter = groupTypeAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {

            spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    parent?.let {
                        edtGroupName.setText((it.selectedItem as IoTGroup).label)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            btnEditLocation.setOnClickListener {
                /*
                * To edit a group
                * Para 1: UUID of the group to delete
                * Para 2: New label of the group
                * Para 3: Name of type of the group
                * */
                val uuid = (spinnerGroup.selectedItem as IoTGroup).uuid
                val label = edtGroupName.text.toString()
                val type = (spinnerGroupType.selectedItem as IoTGroupType).name
                SmartSdk.groupHandler().updateGroup(
                    uuid,
                    label,
                    type,
                    object : RequestCallback<IoTGroup> {
                        override fun onSuccess(p0: IoTGroup?) {
                            showNoti(R.string.update_success)
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