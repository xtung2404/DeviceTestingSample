package com.example.rogosample.UI.smart.automation.editAutomation

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
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditSmartStairSwitchBinding
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.ELement
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartNotification
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartStairSwitch

class EditSmartStairSwitchFragment : BaseFragment<FragmentEditSmartStairSwitchBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_smart_stair_switch

    private val elmFirstList = arrayListOf<ELement>()
    private val elmSecondList = arrayListOf<ELement>()
    private var ioTSmartStairSwitch: IoTSmartStairSwitch? = null

    private val smartSpinnerAdapter by lazy {
        SmartSpinnerAdapter(requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(
            IoTSmartType.TYPE_AUTOMATION)
            .filter {
                it.subType == IoTAutomationType.TYPE_STAIR_SWITCH
            }
            .toList())
    }

    private val deviceFirstSpinnerAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.filter {
            it.containtFeature(IoTAttribute.ACT_ONOFF) && it.devType == IoTDeviceType.SWITCH
        })
    }
    private val elementFirstCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            notifyChange(it)
        })
    }

    private val elementSecondCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            notifyChangeSecond(it)
        })
    }

    private lateinit var deviceSecondSpinnerAdapter: DeviceSpinnerAdapter

    /*
    * Get the elmOwn
    * */
    private fun notifyChange(change: Pair<Int, Boolean>) {
        if (change.second) {
            elmFirstList.forEach {elm ->
                elm.isChecked = elm.elmIndex == change.first
            }
            ioTSmartStairSwitch?.elmOwner = change.first
        }
        binding.rvElmFirst.post(object : Runnable {
            override fun run() {
                elementFirstCheckAdapter.notifyDataSetChanged()
            }
        })
    }

    /*
    * Get the elmExt
    * */
    private fun notifyChangeSecond(change: Pair<Int, Boolean>) {
        if (change.second) {
            elmSecondList.forEach {elm ->
                elm.isChecked = elm.elmIndex == change.first
            }
            ioTSmartStairSwitch?.elmExt = change.first
        }
        binding.rvElmSecond.post(object : Runnable {
            override fun run() {
                elementSecondCheckAdapter.notifyDataSetChanged()
            }
        })
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_smart_stair_switch)
            spinnerSmart.adapter = smartSpinnerAdapter
            spinnerSwitchFirst.adapter = deviceFirstSpinnerAdapter
            rvElmFirst.adapter = elementFirstCheckAdapter
            rvElmSecond.adapter = elementSecondCheckAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            ioTSmartStairSwitch = IoTSmartStairSwitch()
            /*
            * After selecting a Smart Automation, get the Smart Stair Switch of the Smart Automation
            * */
            spinnerSmart.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val ioTSmart = (it.getItemAtPosition(p2) as IoTSmart)
                        ioTSmartStairSwitch =
                            SmartSdk.smartFeatureHandler().getSmartFeatureStairSwitch(ioTSmart.uuid)
                        edtSmartName.text = ioTSmart.label
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

            /*
            * After selecting device -> Show elements of device.
            * Check whether user updated the elements' label or not. If not show the index of that element
            * Show the available list of the second device.
            * */
            spinnerSwitchFirst.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmFirstList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTSmartStairSwitch?.devIdOwner = ioTDevice.uuid
                        if (ioTDevice.elementIds.isNotEmpty()) {
                            ioTDevice.elementInfos.forEach { entry ->
                                if (entry.value.label.isNullOrEmpty()) {
                                    elmFirstList.add(
                                        ELement(
                                            entry.key,
                                            "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                        )
                                    )
                                } else {
                                    elmFirstList.add(
                                        ELement(entry.key, entry.value.label)
                                    )
                                }
                            }
                            elementFirstCheckAdapter.submitList(elmFirstList)
                        } else {
                            ILogR.D("getElement", "No element")
                        }
                        deviceSecondSpinnerAdapter = DeviceSpinnerAdapter(
                            requireContext(),
                            SmartSdk.deviceHandler().all.filter { device ->
                                device.containtFeature(IoTAttribute.ACT_ONOFF) && device.devType == IoTDeviceType.SWITCH && device != ioTDevice
                            })
                        spinnerSwitchSecond.adapter = deviceSecondSpinnerAdapter
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * After selecting device -> Show elements of device.
            * Check whether user updated the elements' label or not. If not show the index of that element
            * */
            spinnerSwitchSecond.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmSecondList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTSmartStairSwitch?.devIdExt = ioTDevice.uuid
                        if (ioTDevice.elementIds.isNotEmpty()) {
                            ioTDevice.elementInfos.forEach { entry ->
                                if (entry.value.label.isNullOrEmpty()) {
                                    elmSecondList.add(
                                        ELement(
                                            entry.key,
                                            "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                        )
                                    )
                                } else {
                                    elmSecondList.add(ELement(entry.key, entry.value.label))
                                }
                            }
                            elementSecondCheckAdapter.submitList(elmSecondList)
                        } else {
                            ILogR.D("getElement", "No element")
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            btnEditSmart.setOnClickListener {
                /*
                * Bind Smart Stair Switch to the selected Smart Automation
                * */
                dialogLoading.show()
                SmartSdk.smartFeatureHandler().bindSmartFeatureStairSwitch(
                    ioTSmartStairSwitch,
                    object : RequestCallback<IoTSmartStairSwitch> {
                        override fun onSuccess(p0: IoTSmartStairSwitch?) {
                            dialogLoading.dismiss()
                            showNoti(R.string.update_success)
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
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