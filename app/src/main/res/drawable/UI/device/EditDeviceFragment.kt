package com.example.rogosample.UI.device

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditDeviceBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice

class EditDeviceFragment : BaseFragment<FragmentEditDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_device

    private val deviceSpinnerAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.toMutableList())
    }
    private val elementAdapter by lazy {
        ElementAdapter(labelChange = {pair ->
            elementChangeLabels[pair.first] = pair.second
        })
    }
    private val elementLabels = hashMapOf<Int, String>()
    private val elementChangeLabels = hashMapOf<Int, String>()

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_device)
            spinnerDevice.adapter = deviceSpinnerAdapter
            rvElement.adapter = elementAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Update a device, pass
            * Para 1: UUID of the device
            * Para 2: new label of the device
            * */
            btnEditDevice.setOnClickListener {
                val label = edtDeviceName.text.toString()
                SmartSdk.deviceHandler().updateDeviceLabel(
                    (spinnerDevice.selectedItem as IoTDevice).uuid,
                    label,
                    elementChangeLabels,
                    object : RequestCallback<IoTDevice> {
                        override fun onSuccess(p0: IoTDevice?) {
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
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    elementLabels.clear()
                    elementChangeLabels.clear()
                    edtDeviceName.setText(deviceSpinnerAdapter.getItem(p2)?.label)
                    if(deviceSpinnerAdapter.getItem(p2)?.elementIds?.isNotEmpty() == true) {
                        lnElement.visibility = View.VISIBLE
                        deviceSpinnerAdapter.getItem(p2)?.elementInfos?.forEach {
                            if(it.value.label.isNullOrEmpty()) {
                                elementLabels[it.key] = "Nút ${deviceSpinnerAdapter.getItem(p2)?.getIndexElement(it.key)}"
                            } else {
                                elementLabels[it.key] = it.value.label
                            }
                            }
                        elementAdapter.submitList(elementLabels.entries.toList())
                    } else {
                        lnElement.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    lnElement.visibility = View.GONE
                }
            }
        }
    }
}