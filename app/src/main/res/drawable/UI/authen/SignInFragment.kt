package com.example.rogosample.UI.authen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentSignInBinding
import rogo.iot.module.cloudapi.auth.callback.AuthRequestCallback
import rogo.iot.module.platform.ILogR
import rogo.iot.module.rogocore.sdk.SmartSdk

class SignInFragment : BaseFragment<FragmentSignInBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_sign_in

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.sign_in)
            edtUsername.setText("")
            edtEmail.setText("phamtung240402@gmail.com")
            edtPassword.setText("123456")
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            initializeBluetoothOrRequestPermission()

            /*
            * sign in with email, password, username
            * */
            btnSignIn.setOnClickListener {
                dialogLoading.show()
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                val username = edtUsername.text.toString()
                if(username.isEmpty()) {
                    SmartSdk.signIn(null, email, null, password, object : AuthRequestCallback {
                        override fun onSuccess() {
                            dialogLoading.dismiss()
                            lnOptions.visibility = View.VISIBLE
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            ILogR.D("SignInFragment", p0, p1)
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    })
                } else {
                    SmartSdk.signIn(username, null, null, password, object : AuthRequestCallback {
                        override fun onSuccess() {
                            dialogLoading.dismiss()
                            lnOptions.visibility = View.VISIBLE
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            ILogR.D("SignInFragment", p0, p1)
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    })
                }
            }
            btnToLocation.setOnClickListener {
                findNavController().navigate(R.id.locationFunctionFragment)
            }
            btnToSignOut.setOnClickListener {
                findNavController().navigate(R.id.signOutFragment)
            }
        }
    }
        private fun initializeBluetoothOrRequestPermission() {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                requireActivity(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            initializeBluetooth()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 2)
        }
    }

    private fun initializeBluetooth() {
        val bluetoothManager: BluetoothManager = ContextCompat.getSystemService(
            requireActivity(),
            BluetoothManager::class.java
        ) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(requireActivity(), turnOn, 0, null)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            2 -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    initializeBluetooth()
                } else {
                    Log.d("tung", "still fail")
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}