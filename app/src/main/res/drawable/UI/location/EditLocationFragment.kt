package com.example.rogosample.UI.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.example.rogosample.R
import com.example.rogosample.adapter.LocationSpinnerAdapter
import com.example.rogosample.adapter.LocationTypeAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.base.LocationType
import com.example.rogosample.databinding.FragmentEditLocationBinding
import rogo.iot.module.platform.callback.RequestCallback
//import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.define.IoTLocationType
import rogo.iot.module.rogocore.sdk.entity.IoTLocation

class EditLocationFragment : BaseFragment<FragmentEditLocationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_location

    private var locationList = mutableListOf<IoTLocation>()

    private val locationSpinnerAdapter by lazy {
        LocationSpinnerAdapter(requireContext(), locationList)
    }

    private val locationTypeAdapter by lazy {
        LocationTypeAdapter(requireContext(), IoTLocationType.entries.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            locationList = SmartSdk.locationHandler().all.toMutableList()
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_location)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerLocation.adapter = locationSpinnerAdapter
            spinnerLocationType.adapter = locationTypeAdapter
            btnEditLocation.setOnClickListener {
                val uuid = (spinnerLocation.selectedItem as IoTLocation).uuid
                val label = edtLocationName.text.toString()
                val desc = resources.getString(LocationType.getLocationDesc(spinnerLocationType.selectedItem as IoTLocationType))
                if(uuid.isNullOrEmpty()) {
                    showNoti("Choose location to update")
                    return@setOnClickListener
                }
                if(label.isNullOrEmpty()) {
                    notify("Enter the new location name")
                } else {
                    /*
                    * To edit a location
                    * Para 1: UUID of the location
                    * Para 2: New label of the location
                    * Para 3: New type of the location
                    * */
                    SmartSdk.locationHandler().updateLocation(
                        uuid,
                        label,
                        desc,
                        object : RequestCallback<IoTLocation> {
                            override fun onSuccess(p0: IoTLocation?) {
                                showNoti("Update success")
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
}