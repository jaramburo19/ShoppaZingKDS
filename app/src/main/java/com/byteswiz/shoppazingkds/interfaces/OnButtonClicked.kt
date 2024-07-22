package com.byteswiz.shoppazingkds.interfaces

interface OnButtonClicked {
    fun onPreparingClicked()
    fun onCompletedClicked()
    fun onRecallClicked()
    fun onChildItemClicked(parentModelId: Int, itemId: Long, flag: Boolean)
}