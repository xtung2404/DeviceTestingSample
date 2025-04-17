package com.example.rogosample.UI.smart.automation

import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.SmartTypeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartAutomationBinding
import rogo.iot.module.platform.define.IoTAutomationType

class AddSmartAutomationFragment : BaseFragment<FragmentAddSmartAutomationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_automation

    private var type = 0

    private val automationTypeSpinnerAdapter by lazy {
        SmartTypeSpinnerAdapter(
            requireContext(), listOf(
                IoTAutomationType.TYPE_STAIR_SWITCH,
                IoTAutomationType.TYPE_NOTIFICATION,
                IoTAutomationType.TYPE_SMART_REVERSE_ON_OFF,
                IoTAutomationType.TYPE_MIX_AND
            )
        )
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            arguments?.let {
                type = it.getInt("type")
            }
            when(type) {
                0 -> {
                    toolbar.txtTitle.text = resources.getString(R.string.add_smart_automation)
                }
                else -> {
                    toolbar.txtTitle.text = resources.getString(R.string.edit_smart_automation)
                }
            }
            spinnerAutomationType.adapter = automationTypeSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnContinue.setOnClickListener {
                when(type) {
                    0 -> {
                        when (spinnerAutomationType.selectedItem as Int) {
                            IoTAutomationType.TYPE_STAIR_SWITCH -> {
                                findNavController().navigate(R.id.addStairSwitchFragment)
                            }

                            IoTAutomationType.TYPE_NOTIFICATION -> {
                                findNavController().navigate(R.id.addSmartNotificationFragment)
                            }

                            IoTAutomationType.TYPE_SMART_REVERSE_ON_OFF -> {
                                findNavController().navigate(R.id.addSmartSelfReverseFragment)
                            }
                            else -> {
                                findNavController().navigate(R.id.addSmartAdvanceFragment)
                            }
                        }
                    } else -> {
                    when (spinnerAutomationType.selectedItem as Int) {
                        IoTAutomationType.TYPE_STAIR_SWITCH -> {
                            findNavController().navigate(R.id.editSmartStairSwitchFragment)
                        }

                        IoTAutomationType.TYPE_NOTIFICATION -> {
                            findNavController().navigate(R.id.editSmartNotificationFragment)
                        }

                        IoTAutomationType.TYPE_SMART_REVERSE_ON_OFF -> {

                        }
                        else -> {
                            findNavController().navigate(R.id.editSmartAdvanceFragment)
                        }
                    }
                    }
                }

            }
        }
    }
}