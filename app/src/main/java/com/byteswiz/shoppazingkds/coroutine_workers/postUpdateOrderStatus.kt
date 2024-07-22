package com.byteswiz.shoppazingkds.coroutine_workers


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byteswiz.shoppazingkds.api.ApiClient
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.data.UpdateOrderRequest
import com.byteswiz.shoppazingkds.data.UpdateOrderResponse
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_WALKIN
import com.byteswiz.shoppazingkds.utils.Constants.TEST_ONLINE_URL
import com.byteswiz.shoppazingkds.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class postUpdateOrderStatus(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    private var LOG_TAG="PostSalesCoroutineWorker"
    private var sessionManager: SessionManager
    private var apiClient: ApiClient
    val appContext = applicationContext


    init {

        apiClient = ApiClient()

        sessionManager = SessionManager(appContext)
        // call your Rest API in init method
    }

    override suspend fun doWork(): Result {


        return try {

            if(hasActiveInternetConnection(appContext)){
                PostStatusUpdate()
                Result.success()
            }
            else
                Result.failure()

        } catch (throwable: Throwable) {
            Log.d("ErrorPostingSales", throwable.localizedMessage)
            //Timber.e(throwable)
            // Result.failure()
            if (runAttemptCount <3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    private fun haveNetworkConnection(context: Context): Boolean {

        var haveConnectedWifi = false
        var haveConnectedMobile = false
        val cm =context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val netInfo = cm!!.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals("WIFI", ignoreCase = true))
                if (ni.isConnected )
                    haveConnectedWifi = true
            if (ni.typeName.equals("MOBILE", ignoreCase = true))
                if (ni.isConnected) haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
    }

    fun isNetworkConnected(context: Context): Boolean {
        var result = false
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                val capabilities =
                    cm.getNetworkCapabilities(cm.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = true
                    }
                }
            }
        } else {
            if (cm != null) {
                val activeNetwork = cm.activeNetworkInfo
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }
    fun hasActiveInternetConnection(context: Context): Boolean {
        if (isNetworkConnected(context)) {
            try {
                val urlc: HttpURLConnection =

                    URL(TEST_ONLINE_URL).openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.setConnectTimeout(1500)
                urlc.connect()
                return urlc.getResponseCode() === 200
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Error checking internet connection", e)
            }
        } else {
            Log.d(LOG_TAG, "No network available!")
        }
        return false
    }

    fun PostStatusUpdate(){

        var toUpdate = ShoppingCart.getOrders().filter { it-> it.orderStatusId!=2 && it.OrderTypeId!= ORDER_TYPES_WALKIN && !it.IsSynced && it.TodaysOrderNo !=null }

        for (s in toUpdate){
            PostConfirmOrderAsync(
                UpdateOrderRequest(
                    s.OrderRefNo,s.orderStatusId,s.receiptNo,s.TodaysOrderNo,false
                )
            )


        }
    }


    /*fun PostConfirmOrderAsync(updateOrderModel: UpdateOrderRequest) {

        apiClient.getApiService(appContext).postUpdateOrderStatus(updateOrderModel)
            .enqueue(object : Callback<UpdateOrderResponse> {

                override fun onFailure(call: Call<UpdateOrderResponse>, t: Throwable) {
                    Log.d("Error:", t.localizedMessage)

                   //Toast.makeText(appContext, "Update Failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<UpdateOrderResponse>, response: Response<UpdateOrderResponse>) {
                    //simpleD.hide()
                   // dialog.dismiss()
                    if (response.body() != null){

                    }


                }
            })

    }
*/

    fun PostConfirmOrderAsync(model: UpdateOrderRequest){
        try{

            var requestResponse: UpdateOrderResponse? =null
            val response: Response<UpdateOrderResponse> = apiClient.getApiService(appContext)
                    .postUpdateOrderStatus(
                            model
                    )
                    .execute()

            requestResponse = response.body()
            if(requestResponse!!.status==200)
                ShoppingCart.updateSyncStatus(true,model.TodaysOrderNo)

        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }

    }





}