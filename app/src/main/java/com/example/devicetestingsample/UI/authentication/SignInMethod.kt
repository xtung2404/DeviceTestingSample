package com.example.devicetestingsample.UI.authentication

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import rogo.iot.module.rogocore.basesdk.auth.method.IAuthForgotMethod
import rogo.iot.module.rogocore.basesdk.auth.method.IAuthSignInMethod
import rogo.iot.module.rogocore.basesdk.auth.method.IAuthSignUpMethod


sealed class SignInMethod : IAuthSignInMethod {
    class SignInWithCredential(var credential: AuthCredential) :
        SignInMethod()

    class SignInWithPhone(var activity: Activity, var phoneNumber: String, var seconds: Int) :
        SignInMethod()

    class VerifySmsMethod(var code: String) : SignInMethod()

    class SignInWithEmail(var email: String, var password: String) :
        SignInMethod()

    class SignInWithCode(var code: String) : SignInMethod()

    class SignInWithToken(var token: String) : SignInMethod()
}

sealed class SignUpMethod : IAuthSignUpMethod {
    class SignUpWithEmail(var email: String, var password: String) :
        SignUpMethod()

}

sealed class ForgotPasswordMethod : IAuthForgotMethod {
    class ForgotEmailPassword(var email: String) : ForgotPasswordMethod()

}