package com.candyhouse.server

import android.widget.Toast
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.regions.Regions
import com.candyhouse.app.MyApp
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.CHLoginProvider
import com.candyhouse.sesame.server.CHOauthToken
import com.candyhouse.utils.L


object AWSCognitoOAuthService : CHLoginProvider {
    var nameChange: NameChange? = null

    interface LoginResult {
        fun onSuccess(v: String?)
        fun onError(exception: Exception)
    }

    interface NameChange {
        fun onName(v: String?)
    }

    var userToken: String? = null

    val userPool =
            CognitoUserPool(
                    MyApp.ctx,
                    cognitoIdentityUserPoolId,
                    cognitoIdentityUserPoolAppClientId,
                    cognitoIdentityUserPoolAppClientSecret,
                    regin
            )

    fun getID(nameChange: NameChange) {
        this.nameChange = nameChange
        nameChange.onName(userPool.currentUser.userId)
    }

    fun logOut(): Boolean {

        userPool.currentUser.signOut()

        CHAccountManager.logout()
        return false
    }

    fun loginWithUsernamePassword(username: String, password: String, result: LoginResult) {

        userPool.getUser(username).getSessionInBackground(object : AuthenticationHandler {

            override fun getAuthenticationDetails(
                    authenticationContinuation: AuthenticationContinuation,
                    userId: String
            ) {
                val authenticationDetails =
                        AuthenticationDetails(username, password, null)
                authenticationContinuation.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {}

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {}

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
//                userToken = userSession!!.idToken.jwtToken
                userToken = userSession!!.idToken.jwtToken
                nameChange?.onName(userPool.currentUser.userId)
                result.onSuccess(userSession.username)
            }

            override fun onFailure(exception: Exception) { // Sign-in failed, check exception for the cause
                result.onError(exception)
            }
        })
    }


    override fun oauthToken(): CHOauthToken {

        val user = userPool.currentUser
        if (user.userId == null) {
            return CHOauthToken(identityProviderCognito, "nouser")
        }
        user.getSession(object : AuthenticationHandler {

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
            }

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            }

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                userSession!!.refreshToken
                userToken = userSession!!.idToken.jwtToken
                L.d("hcia", "取得 userToken:" + userToken!!.slice(0..4))
            }

            override fun onFailure(exception: Exception) {
//                exception is NullPointerException
//                when (exception) {
//                    is NullPointerException -> {
                Toast.makeText(MainActivity.activity, exception.message, Toast.LENGTH_LONG).show()
                L.d("hcia", "取得失敗exception:" + exception)
//                oauthToken()
//                CHAccountManager.setupLoginSession(AWSCognitoOAuthService)
//                    }
//                }
            }
        })

        L.d("hcia", "送出４userToken:" + userToken!!.slice(0..4))
        return CHOauthToken(identityProviderCognito, userToken ?: "")
    }
}