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
import com.example.rogosample.adapter.ElementSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentDeleteDeviceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart

class DeleteDeviceFragment : BaseFragment<FragmentDeleteDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_delete_device

    private val deviceSpinnerAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.toMutableList())
    }
    private val elementMap = hashMapOf<Int, String>()
    private lateinit var elementSpinnerAdapter: ElementSpinnerAdapter

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_device)
            spinnerDevice.adapter = deviceSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    parent?.let {
                        val ioTDevice = (it.getItemAtPosition(position) as IoTDevice)
                        if (ioTDevice.devType == IoTDeviceType.GATEWAY) {
                            elementMap.clear()
                            ioTDevice.elementInfos.forEach { entry ->
                                if (entry.value.label.isNullOrEmpty()) {
                                    elementMap[entry.key] =
                                        "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                } else {
                                    elementMap[entry.key] = entry.value.label
                                }
                            }
                            elementSpinnerAdapter =
                                ElementSpinnerAdapter(requireContext(), elementMap.entries.toList())
                            spinnerElement.adapter = elementSpinnerAdapter
                            lnElement.visibility = View.VISIBLE
                            btnDeleteElmDevice.visibility = View.VISIBLE
                        } else {
                            btnDeleteElmDevice.visibility = View.GONE
                            lnElement.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
            btnDeleteElmDevice.setOnClickListener {
                dialogLoading.show()
                SmartSdk.deviceHandler().deleteElementDevice(
                    (spinnerDevice.selectedItem as IoTDevice).uuid,
                    (spinnerElement.selectedItem as Map.Entry<Int, String>).key,
                    object : RequestCallback<IoTDevice> {
                        override fun onSuccess(p0: IoTDevice?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.delete_success)
                            }
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.delete_element_failure)
                            }
                        }

                    }
                )
            }
            /*
            * Delete a device, pass the device's UUID
            * */
            btnDeleteDevice.setOnClickListener {
                SmartSdk.deviceHandler().delete(
                    (spinnerDevice.selectedItem as IoTDevice).uuid,
                    object : RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            p0?.let {
                                showNoti(R.string.delete_success)
                            }
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