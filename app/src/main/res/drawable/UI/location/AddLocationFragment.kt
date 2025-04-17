package com.example.rogosample.UI.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.LocationTypeAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.base.LocationType
import com.example.rogosample.databinding.FragmentAddLocationBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.define.IoTLocationType
import rogo.iot.module.rogocore.sdk.entity.IoTLocation

class AddLocationFragment : BaseFragment<FragmentAddLocationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_location

    private val locationTypeAdapter by lazy {
        LocationTypeAdapter(requireContext(), IoTLocationType.entries.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
                toolbar.txtTitle.text = resources.getString(R.string.add_location)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerLocationType.adapter = locationTypeAdapter
            /*
            * Add new location
            * Para 1: Location name
            * Para 2: Name of the type of the location
            * */
            btnAddLocation.setOnClickListener {
                val label = edtLocationName.text.toString()
                SmartSdk.locationHandler().createLocation(
                    label,
                    resources.getString(LocationType.getLocationDesc(spinnerLocationType.selectedItem as IoTLocationType)),
                    object : RequestCallback<IoTLocation> {
                        override fun onSuccess(p0: IoTLocation?) {
                            showNoti("Add success")
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