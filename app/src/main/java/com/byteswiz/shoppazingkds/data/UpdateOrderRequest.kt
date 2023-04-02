package com.byteswiz.shoppazingkds.data

import com.google.gson.annotations.SerializedName

class UpdateOrderRequest(

    @SerializedName("OrderNo")
    var OrderNo: String,


    @SerializedName("OrderStatusId")
    var OrderStatusId: Int,

    @SerializedName("ReceiptNo")
    var ReceiptNo: String,

    @SerializedName("TodaysOrderNo")
    var TodaysOrderNo: String

)

{
}