package com.core.networks.auth

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@Keep
data class AuthData(
    @SerializedName("appid") val appid: String,
    @SerializedName("appvers") val appvers: Int,
    @SerializedName("user_id") val userId: String
)

@Keep
data class AuthResponse(
    @SerializedName("encrypted_response") val encryptedResponse: String
)

@Keep
data class RefreshTokenResonse(
    @SerializedName("access_token") val accessToken: String
)



@Keep
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun fromJson(json: String): TokenResponse {
            val jsonObject = JSONObject(json)
            return TokenResponse(
                accessToken = jsonObject.getString("access_token"),
                refreshToken = jsonObject.getString("refresh_token")
            )
        }
    }
}

@Keep
interface AuthApiService {

    @POST("/auth")
    fun authenticate(@Body authData: AuthData): Call<AuthResponse>

    @POST("/refresh")
    fun refreshToken(@Query("refresh_token") refreshToken: String): Call<RefreshTokenResonse>


}
