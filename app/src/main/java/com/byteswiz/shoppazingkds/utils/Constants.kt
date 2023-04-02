package com.byteswiz.shoppazingkds.utils

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri


object Constants {

    const val ORDER_STATUS_CONFIRMED = 2
    const val ORDER_STATUS_PREPARING = 3
    const val ORDER_STATUS_READY = 4
    const val TYPE_ITEM = 0
    const val TYPE_HEADER = 1


    const val ORDER_TYPES_WALKIN =3
    const val ORDER_TYPES_ONLINE_DELIVERY =1
    const val ORDER_TYPES_ONLINE_PICKUP =2

    // Endpoints
    /*const val BASE_URL = "http://jaramburo19-001-site6.ftempurl.com/api/token/"
    const val LOGIN_URL = "oauth/access_token"*/

    //API
    //LOCAL
    //const val BASE_URL = "http://10.0.2.2:44315/"
    //const val BACKEND_BASE_URL = "http://10.0.2.2:44315/"

    //const val BASE_URL = "http://10.0.2.2:44315/"

    //QA
    const val BASE_URL = "http://jaramburo19-001-site29.ftempurl.com/api/"
    const val BACKEND_BASE_URL = "http://jaramburo19-001-site29.ftempurl.com/"

    //LIVE
     /*const val BASE_URL = "http://sellercenter.shoppazing.com/api/"
     const val BACKEND_BASE_URL = "http://sellercenter.shoppazing.com/"*/

    //const val BASE_URL = "http://jaramburo19-001-site28.ftempurl.com/api/"
    //const val BASE_URL = "http://sellercenter.shoppazing.com/api/"
    /* const val BASE_URL = "http://jaramburo19-001-site28.ftempurl.com/api/"
     const val BACKEND_BASE_URL = "http://jaramburo19-001-site28.ftempurl.com/"
     */

    const val selected_position = 0

    const val TEST_ONLINE_URL ="https://www.google.com/"

    const val LOGIN_URL = "token"

    const val POSTS_URL = "posts"

    const val GETPRODUCTS_URL = "items/get//IsPrinterConnected/"

    const val GETITEMS_URL = "items/getitems/"

    const val UDPATE_ORDER_STATUS_URL = "shop/updateorderstatus/"


    //NOTIFICATION CONSTANTS
    const val VERBOSE_NOTIFICATION_CHANNEL_NAME =
            "BPOS WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
            "Shows notifications whenever work starts"

    const val NEWORDER_NOTIFICATION_TITLE = "Online Order Arrived"

    const val CHANNEL_ID = "BPOS_NOTIFICATION"
    const val NOTIFICATION_ID = 1




}