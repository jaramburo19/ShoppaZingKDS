package com.byteswiz.shoppazingkds.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class UpdateOrderResponse(
    @SerializedName("status_code")
    var status: Int,
    @SerializedName("message")
    var message: String,

    @SerializedName("OrderRefNo")
    var OrderRefNo: String
)


{
}