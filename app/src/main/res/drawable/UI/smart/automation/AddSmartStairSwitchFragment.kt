package com.example.rogosample.UI.smart.automation

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddStairSwitchBinding
import com.example.rogosample.`object`.ELement
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartStairSwitch

class AddSmartStairSwitchFragment : BaseFragment<FragmentAddStairSwitchBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_stair_switch

    private val elmFirstList = arrayListOf<ELement>()
    private val elmSecondList = arrayListOf<ELement>()
    private var ioTSmartStairSwitch: IoTSmartStairSwitch? = null

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
    * Get the element of device owner
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
   * Get the element of device extension
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
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_stair_switch)
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
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
            btnAddSmart.setOnClickListener {
                /*
                * Create a Smart Automation
                * */
                SmartSdk.smartFeatureHandler().createSmartAutomation(
                    edtSmartName.text.toString(),
                    IoTAutomationType.TYPE_STAIR_SWITCH,
                    intArrayOf(),
                    null,
                    object : RequestCallback<IoTSmart> {
                        override fun onSuccess(p0: IoTSmart?) {
                            /*
                            * Bind Smart Stair Switch to Smart Automation
                            * */
                            ioTSmartStairSwitch?.smartId = p0?.uuid
                            ioTSmartStairSwitch?.devIdOwner =
                                (spinnerSwitchFirst.selectedItem as IoTDevice).uuid
                            ioTSmartStairSwitch?.devIdExt =
                                (spinnerSwitchSecond.selectedItem as IoTDevice).uuid
                            SmartSdk.smartFeatureHandler().bindSmartFeatureStairSwitch(
                                ioTSmartStairSwitch, object : RequestCallback<IoTSmartStairSwitch> {
                                    override fun onSuccess(p0: IoTSmartStairSwitch?) {
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

                        override fun onFailure(p0: Int, p1: String?) {

                        }

                    }
                )
            }
        }
    }
}