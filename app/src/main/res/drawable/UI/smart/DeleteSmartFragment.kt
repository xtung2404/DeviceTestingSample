package com.example.rogosample.UI.smart

import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentDeleteSmartBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart

class DeleteSmartFragment : BaseFragment<FragmentDeleteSmartBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_delete_smart

    var type = IoTSmartType.TYPE_SCENE
    private lateinit var smartSpinnerAdapter: SmartSpinnerAdapter

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            arguments?.let {
                type = it.getInt("smartType")
            }
            when(type) {
                IoTSmartType.TYPE_SCENE -> {
                    toolbar.txtTitle.text = resources.getString(R.string.delete_smart_scenario)
                    smartSpinnerAdapter = SmartSpinnerAdapter(requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(type).toList())
                    spinnerSmart.adapter = smartSpinnerAdapter
                }
                IoTSmartType.TYPE_SCHEDULE -> {
                    toolbar.txtTitle.text = resources.getString(R.string.delete_smart_schedule)
                    smartSpinnerAdapter = SmartSpinnerAdapter(requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(type).toList())
                    spinnerSmart.adapter = smartSpinnerAdapter
                }
                else -> {
                    toolbar.txtTitle.text = resources.getString(R.string.delete_smart_automation)
                    smartSpinnerAdapter = SmartSpinnerAdapter(requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(type).toList())
                    spinnerSmart.adapter = smartSpinnerAdapter
                }
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Pass Smart's UUID to delete the Smart
            * */
            btnDeleteSmart.setOnClickListener {
                SmartSdk.smartFeatureHandler().delete(
                    (spinnerSmart.selectedItem as IoTSmart).uuid,
                    object : RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            showNoti(R.string.delete_success)
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