package com.example.rogosample.UI.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.FunctionAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentLocationFunctionBinding
import com.example.rogosample.`object`.Function

class LocationFunctionFragment : BaseFragment<FragmentLocationFunctionBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_location_function

    private val locationFunctionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.GETLISTLOCATION -> {
                    findNavController().navigate(R.id.locationFragment)
                }
                Function.ADDLOCATION -> {
                    findNavController().navigate(R.id.addLocationFragment)
                }
                Function.EDITLOCATION -> {
                    findNavController().navigate(R.id.editLocationFragment)
                }
                else -> {
                    findNavController().navigate(R.id.deleteLocationFragment)
                }
            }
        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            if(findNavController().previousBackStackEntry?.destination?.id == R.id.splashFragment) {
                findNavController().navigate(R.id.functionFragment)
            }
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
    override fun initAction() {
        super.initAction()
        binding.apply {
            rvFunction.adapter = locationFunctionAdapter
            locationFunctionAdapter.submitList(Function.getLocationFuncs())
        }

    }
}