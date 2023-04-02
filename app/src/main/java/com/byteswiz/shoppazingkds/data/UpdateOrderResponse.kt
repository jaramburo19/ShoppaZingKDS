package com.byteswiz.shoppazingkds.data

import com.google.gson.annotations.SerializedName

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