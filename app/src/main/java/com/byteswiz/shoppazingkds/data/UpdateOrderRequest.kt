package com.byteswiz.shoppazingkds.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class UpdateOrderRequest(

    @SerializedName("OrderNo")
    var OrderNo: String,

    @SerializedName("OrderStatusId")
    var OrderStatusId: Int,

    @SerializedName("ReceiptNo")
    var ReceiptNo: String,

    @SerializedName("TodaysOrderNo")
    var TodaysOrderNo: String,

    @SerializedName("IsManualDelivered")
    var IsManualDelivered: Boolean,

    @SerializedName("LocalUniqueId")
    var LocalUniqueId: String,


)

{
}