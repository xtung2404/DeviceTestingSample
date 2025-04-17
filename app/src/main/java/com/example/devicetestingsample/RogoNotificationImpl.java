package com.example.devicetestingsample;

import android.util.Log;


import com.google.firebase.messaging.FirebaseMessaging;

import rogo.iot.module.rogocore.sdk.handler.NotificationHandler;

public class RogoNotificationImpl implements NotificationHandler {
    private final String TAG = "RogoNotificationImpl";

    @Override
    public void subcribeTopic(String topic) {
        Log.d(TAG, "subcribeTopic: " + topic);
        if (topic != null) {
//            FirebaseMessaging.getInstance().subscribeToTopic(topic)
//                    .addOnCompleteListener(task -> {
//
//
//                    });
        }
    }

    @Override
    public void unsubcribeTopic(String topic) {

    }
}
