package com.byteswiz.shoppazingkds.api

import com.byteswiz.shoppazingkds.data.UpdateOrderRequest
import com.byteswiz.shoppazingkds.data.UpdateOrderResponse
import com.byteswiz.shoppazingkds.utils.Constants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiService {

    @Headers("Content-Type: application/json")
    @POST(Constants.UDPATE_ORDER_STATUS_URL)
    fun postUpdateOrderStatus(@Body request: UpdateOrderRequest): Call<UpdateOrderResponse>

}