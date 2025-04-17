package com.example.rogosample.UI.smart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.FunctionAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentSmartSceneBinding
import com.example.rogosample.`object`.Function
import rogo.iot.module.platform.define.IoTSmartType

class SmartSceneFragment: BaseFragment<FragmentSmartSceneBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_smart_scene

    var type = IoTSmartType.TYPE_SCENE
    private val smartFunctionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.GETLISTSMARTSCENARIO -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.getListScenarioFragment, bundle)
                }
                Function.ADDSMARTSCENARIO -> {
                    findNavController().navigate(R.id.addSmartScenarioFragment)
                }
                Function.DELETESMARTSCENARIO -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.deleteSmartFragment, bundle)
                }
                else -> {
                    findNavController().navigate(R.id.editSmartScenarioFragment)
                }
            }
        })
    }
    private val smartScheduleFunctionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.GETLISTSMARTSCHEDULE -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.getListScenarioFragment, bundle)
                }
                Function.ADDSMARTSCHEDULE -> {
                    findNavController().navigate(R.id.addSmartScheduleFragment)
                }
                Function.DELETESMARTSCHEDULE -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.deleteSmartFragment, bundle)
                }
                else -> {
                    findNavController().navigate(R.id.editSmartScheduleFragment)
                }
            }
        })
    }
    private val smartAutomationFunctionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.GETLISTSMARTAUTOMATION -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.getListScenarioFragment, bundle)
                }
                Function.ADDSMARTAUTOMATION -> {
                    val bundle = bundleOf("type" to 0)
                    findNavController().navigate(R.id.addSmartAutomationFragment, bundle)
                }
                Function.DELETESMARTAUTOMATION -> {
                    val bundle = bundleOf("smartType" to type)
                    findNavController().navigate(R.id.deleteSmartFragment, bundle)
                }
                else -> {
                    val bundle = bundleOf("type" to 1)
                    findNavController().navigate(R.id.addSmartAutomationFragment, bundle)
                }
            }
        })
    }


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
                    toolbar.txtTitle.text = resources.getString(R.string.smart_scenario)
                    rvFunction.adapter = smartFunctionAdapter
                    smartFunctionAdapter.submitList(Function.getSmartSceneFuncs())
                }
                IoTSmartType.TYPE_SCHEDULE -> {
                    toolbar.txtTitle.text = resources.getString(R.string.smart_schedule)
                    rvFunction.adapter = smartScheduleFunctionAdapter
                    smartScheduleFunctionAdapter.submitList(Function.getSmartScheduleFuncs())
                }
                else -> {
                    toolbar.txtTitle.text = resources.getString(R.string.smart_automation)
                    rvFunction.adapter = smartAutomationFunctionAdapter
                    smartAutomationFunctionAdapter.submitList(Function.getSmartAutomationFuncs())
                }
            }

        }

    }

    override fun initAction() {
        super.initAction()
        binding.apply {

        }
    }
}