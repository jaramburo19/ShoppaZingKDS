package com.byteswiz.shoppazingkds.interfaces

interface OnParentButtonClicked {
    fun onPreparingClicked(receiptNo: String, localUniqueId: String)
    fun onCompletedClicked(receiptNo: String, localUniqueId: String, position: Int, orderRefNo:String, textDuration:String)

    fun onRecallClicked(receiptNo: String, localUniqueId: String)
    fun onChildItemClicked(orderNo: String, itemId: Long, flag: Boolean)
}