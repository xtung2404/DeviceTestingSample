package com.example.devicetestingsample

import android.app.Application
import rogo.iot.module.rogocore.basesdk.ILogR
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.handler.AuthHandler

class RApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        SmartSdk.isForceProduction = true
        SmartSdk().initV2(
            this,
            "e4b75a6b23fc4f30bd5fab35436c6a90",
            "964e2c974f001a0468bf2734ce88e96652afff328886",
            RogoNotificationImpl(), false, true
        )
//        SmartSdk().initV2(
//            this,
//            "f78f5dd2fc594475a27bef7c2caf9ab4",
//            "41d96be770b2902f801b1689c5edae29c16a068e8f87",
//            RogoNotificationImpl(), false, true
//        )
//        SmartSdk().initV2(
//            this,
//            "907a15d2bdfa4dd5aba1914e97dc7146",
//            "5b077bf50d654369e7e5bc83c3c2f310d3a020136aab",
//            RogoNotificationImpl(), false, true
//        )
        ILogR.isEnablePrint()
    }
}