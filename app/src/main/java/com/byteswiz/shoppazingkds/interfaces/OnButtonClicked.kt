package com.byteswiz.shoppazingkds.interfaces

interface OnButtonClicked {
    fun onPreparingClicked()
    fun onCompletedClicked()
    fun onRecallClicked()
    fun onChildItemClicked(orderNo: String, itemId: Long, flag: Boolean)
}