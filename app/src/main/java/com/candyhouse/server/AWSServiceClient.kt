package com.candyhouse.server

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
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.CHLoginProvider
import com.candyhouse.sesame.server.CHOauthToken
import com.candyhouse.utils.L

/**
 *
 * */
val cognitoIdentityUserPoolId = "us-east-1_69JF5fktv"
val cognitoIdentityUserPoolAppClientId = "21v9tlqp4qtjbau7k1epb15n8f"
val cognitoIdentityUserPoolAppClientSecret = "1k1ni8bnjifjpsl2pg9n2061ln7ja1hdan2ptkdu7b5ups44ud8d"
val identityProviderCognito = "cognito-idp.us-east-1.amazonaws.com/us-east-1_69JF5fktv"
val regin = Regions.US_EAST_1

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

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                L.d("hcia", "authenticationChallenge")
            }

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
                L.d("hcia", "getMFACode")
            }

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                userToken = userSession!!.idToken.jwtToken
                L.d("hcia", "帳號密碼登入成功 請求token成功:" + userSession.idToken.jwtToken.substring(0, 5))
                nameChange?.onName(userPool.currentUser.userId)
                result.onSuccess(userSession.username)
            }

            override fun onFailure(exception: Exception) { // Sign-in failed, check exception for the cause
                L.d("hcia", "onFailure" + exception)
                result.onError(exception)
            }
        })
    }


    override fun oauthToken(): CHOauthToken {

        val user = userPool.currentUser
        L.d("hcia", "認證使用著" + user.userId)
        L.d("hcia", "user:" + user.userPoolId)
//        L.d("hcia", "user:" + user.s)
//
        if (user.userId == null) {
            L.d("hcia", "我沒找到使用著" + user.userId)
            return CHOauthToken(identityProviderCognito, "nouser")
        }
        user.getSession(object : AuthenticationHandler {

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                L.d("hcia", "authenticationChallenge")
            }

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
                L.d("hcia", "getMFACode")
            }

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                userToken = userSession!!.idToken.jwtToken
                L.d("hcia", "請求token成功:" + userSession.idToken.jwtToken.substring(0, 5))
            }

            override fun onFailure(exception: Exception) { // Sign-in failed, check exception for the cause
                L.d("hcia", "我請求token失敗了 ！！！！！onFailure!" + exception)//2020-02-03 14:30:45.089 21152-21152/com.candyhouse D/hcia: onFailurecom.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException: Failed to authenticate user  |OnFailure 主線成(AWSServiceClient.kt:217)
//                CHAccountManager.setupLoginSession(AWSCognitoOAuthService)
                L.d("hcia", "我請求token失敗了 exception.message:" + exception.message)
                exception is NullPointerException

                when (exception) {
                    is NullPointerException -> {
                        L.d("hcia", "我請求token失敗了 我找不到")
                    }

                }

            }
        })


        L.d("hcia", "我是返回的 token userToken:" + userToken?.substring(0, 5))
        return CHOauthToken(identityProviderCognito, userToken ?: "novalue")
    }
}