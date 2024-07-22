package com.byteswiz.shoppazingkds.interfaces

interface OnParentButtonClicked {
    fun onPreparingClicked(receiptNo: String, localUniqueId: String, position: Int, isLongClick:Boolean)
    fun onCompletedClicked(receiptNo: String, localUniqueId: String, position: Int, orderRefNo:String, textDuration:String)

    fun onRecallClicked(receiptNo: String, localUniqueId: String)
    fun onChildItemClicked(parentModelId: Int,  itemId: Long, flag: Boolean)
}