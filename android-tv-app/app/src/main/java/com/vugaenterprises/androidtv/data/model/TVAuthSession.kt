package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class TVAuthSessionRequest(
    @SerializedName("tv_device_id")
    val tvDeviceId: String
)

data class TVAuthSessionResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TVAuthSessionData? = null
)

data class TVAuthSessionData(
    @SerializedName("session_token")
    val sessionToken: String,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("expires_at")
    val expiresAt: String,
    @SerializedName("expires_in_seconds")
    val expiresInSeconds: Int = 300 // Default to 5 minutes if not provided
)

data class TVAuthStatusRequest(
    @SerializedName("session_token")
    val sessionToken: String
)

data class TVAuthStatusResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TVAuthStatusData? = null
)

data class TVAuthStatusData(
    @SerializedName("session_status")
    val authStatus: String,
    @SerializedName("authenticated")
    val authenticated: Boolean,
    @SerializedName("user")
    val user: UserData? = null
)

data class TVAuthCompleteRequest(
    @SerializedName("session_token")
    val sessionToken: String,
    @SerializedName("tv_device_id")
    val tvDeviceId: String
)