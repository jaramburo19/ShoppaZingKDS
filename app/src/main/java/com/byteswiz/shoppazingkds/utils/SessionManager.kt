package com.byteswiz.shoppazingkds.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.byteswiz.shoppazingkds.R

class SessionManager (context: Context) {
    private var pref: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    var editor: SharedPreferences.Editor = pref.edit()

    // Context
    var _context: Context = context

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_EMAIL ="user_email"
        const val IS_LOGGEDIN = "isLoggedIn"
        const val USER_FULLNAME ="user_fullname"
        const val USER_MERCHANT_ID ="user_merchant_id"
        const val USER_BUSINESS_NAME ="user_business_name"
        const val USER_POS_ID ="user_pos_id"
        const val USER_POS_NAME = "userPosName"
        const val USER_STORE_ID = "user_store_id"
        const val USER_STORE_NAME = "user_store_NAME"
        const val IS_PIN_ENTERED ="isPinEntered"
        const val CURRENT_POS_ISONLINE_DEVICE = "isOnlineDevice"

        const val MERCHANT_LOYALTY_ISENABLED= "merchant_loyalty_isenabled"
        const val MERCHANT_LOYALTY_CASHBACKRATE= "merchant_loyalty_cashbackrate"
        const val MERCHANT_LOYALTY_REDEEMLIMIT= "merchant_loyalty_redeemlimit"
        const val MERCHANT_LOYALTY_MERCHANTID= "merchant_loyalty_MERCHANTID"


        const val MERCHANT__OPENTICKET_ID= "merchant_openticket_id"
        const val MERCHANT_OPENTICKET_USERPREDEFTICKETS= "merchant_openticket_usepredefticket"
        const val MERCHANT__OPENTICKET_MERCHANTID= "merchant_openticket_merchantid"



        const val MERCHANTSETTINGS_SHIFTS= "merchant_settings_shifts"
        const val MERCHANTSETTINGS_TIMECLOCK= "merchant_settings_timeclock"
        const val MERCHANTSETTINGS_OPENTICKETS= "merchant_settings_opentickets"
        const val MERCHANTSETTINGS_KITCHENPRINTERS= "merchant_settings_kitchenprinters"
        const val MERCHANTSETTINGS_CUSTOMERDISPLAYS= "merchant_settings_customerdisplays"
        const val MERCHANTSETTINGS_QUEUINGDISPLAY= "merchant_settings_queuingdisplay"
        const val MERCHANTSETTINGS_DININGOPTIONS= "merchant_settings_diningoptions"
        const val MERCHANTSETTINGS_LOWSTOCKNOTIFICATIONS= "merchant_settings_lowstocknotifications"
        const val MERCHANTSETTINGS_NEGATIVESTOCKALERTS=  "merchant_settings_negativestockalerts"
        const val MERCHANTSETTINGS_WEIGHTEMBEDEDBARCODES= "merchant_settings_weightembededbarcodes"


        const val CURRENT_MERCHANT_ID ="currentMerchantId"
        const val CURRENT_USER_USERID = "currentUserId"
        const val CURRENT_USER_PIN = "currentUserPIN"
        const val CURRENT_USER_FULL_NAME="currentUserFullName"
        const val CURRENT_USER_PERMISSION_ID ="currentUserPermissionId"
        const val CURRENT_USER_ROLE ="currentUserRole"

        //SHIFT REPORT
        const val CURRENT_SHIFT_ID = "currentShiftId"
        const val CURRENT_SHIFT_OPENEDBY = "currentShiftOpenedBy"
        const val CURRENT_SHIFT_DATETIMEOPENED = "currentShiftDateTimeOpened"
        const val CURRENT_SHIFT_STARTING_CASH= "currentShiftStartingCash"
        const val CURRENT_SHIFT_CASH_PAYMENTS= "currentShiftCashPayments"
        const val CURRENT_SHIFT_CASH_REFUNDS= "currentShiftCashRefunds"
        const val CURRENT_SHIFT_CASH_PAYOUTS= "currentShiftCashPayouts"
        const val CURRENT_SHIFT_CASH_PAYINS= "currentShiftCashPayins"
        const val CURRENT_SHIFT_EXPECTED_CASH= "currentShiftExpectedCash"
        const val CURRENT_SHIFT_DISCOUNTS= "currentShiftDiscounts"
        const val CURRENT_SHIFT_TAXEXCLUDED= "currentShiftTaxExcluded"
        const val CURRENT_SHIFT_GROSSALES= "currentShiftGrossSales"



        //Open Tickets
        const val CURRENT_CART_TICKET_NAME= "current_cart_ticket_name"


        const val SELECTED_DINING_OPTION_NAME= "selectedDiningOptionName"
        const val SELECTED_DINING_OPTION_ID= "selectedDiningOptionId"

        const val IS_SHIFT_OPEN ="IsShiftOpen"

        const val IS_SCREEN_WIDE ="IsScreenWide"

        const val DEFAULT_PRINTER_NAME ="DefaultPrinterName"

        const val DEFAULT_PRINTER_INTERFACE = "DefaultPrinterInterface"

        const val DEFAULT_PRINTER_PAPERSIZE = "DefaultPrinterPaperSize"

        const val MERCHANT_USER_COUNT ="merchant_user_count"

        const val DEFAULT_CUSTOMERDISPLAY_NAME ="DefaultCustomerDisplayName"

        const val DEFAULT_CUSTOMERDISPLAY_IPADDRESS = "DefaultCustomerDispalyIPAddress"

        const val SCANNED_ORDER_NO = "scanned_order_no"

        const val CUSTOMER_TYPE = "customerType"

        const val CURRENT_ONLINE_ORDER_NO = "currentOnlineOrderNo"
        const val CURRENT_ORDER_TYPE_ID = "currentOrderTypeId"

        private const val FIRST_TIME = "isFirstRun"

    }

    fun isFirstRun() = pref.getBoolean(FIRST_TIME, true)

    fun setFirstRun() {
        editor.putBoolean(FIRST_TIME, false).commit()
        editor.commit()
    }

    data class CurrentCustomerTypeModel(
        var CurrentCustomerType: Int,// 1-Walk-In, 2-Online
        var CurrentOrderNo: String?,
        var CurrentOrderTypeId: Int
    )

    fun setCurrentCustomerType(customerType: CurrentCustomerTypeModel){
        editor.putInt(CUSTOMER_TYPE, customerType.CurrentCustomerType)
        editor.putString(CURRENT_ONLINE_ORDER_NO, customerType.CurrentOrderNo)
        editor.putInt(CURRENT_ORDER_TYPE_ID, customerType.CurrentOrderTypeId)

        editor.apply()
        editor.commit()
    }
    fun getCurrentCustomerType(): CurrentCustomerTypeModel{

        return CurrentCustomerTypeModel( pref.getInt(CUSTOMER_TYPE, 0),
            pref.getString(CURRENT_ONLINE_ORDER_NO, ""),
            pref.getInt(CUSTOMER_TYPE, 1)    )

    }

    /* fun setScannedOrderNo(orderNo: String){
         editor.putString(SCANNED_ORDER_NO, orderNo)

         editor.apply()
         editor.commit()
     }

     fun getScannedOrderNo(): String? {
         val retToken = pref.getString(SCANNED_ORDER_NO, "")
         return retToken
     }*/


    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String, email: String, username: String, merchantId: Int, businessname: String) {
        //val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_FULLNAME, username)

        editor.putInt(USER_MERCHANT_ID, merchantId)
        editor.putString(USER_BUSINESS_NAME, businessname)
        editor.apply()
        editor.commit()
    }



    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        val retToken = pref.getString(USER_TOKEN, null)
        return retToken
    }




}