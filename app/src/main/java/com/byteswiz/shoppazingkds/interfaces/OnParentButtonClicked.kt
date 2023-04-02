package com.byteswiz.shoppazingkds.interfaces

interface OnParentButtonClicked {
    fun onPreparingClicked(receiptNo: String, todaysOrderNo: String)
    fun onCompletedClicked(receiptNo: String, todaysOrderNo: String, position: Int)

    fun onRecallClicked(receiptNo: String, todaysOrderNo: String)
    fun onChildItemClicked(orderNo: String, itemId: Long, flag: Boolean)
}