package com.byteswiz.shoppazingkds.livedata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byteswiz.parentmodel.ParentModel

class CartViewModel: ViewModel() {

    private var _itemsLiveData: MutableLiveData<MutableList<ParentModel>?>
    val ItemsMutableLiveData: MutableLiveData<MutableList<ParentModel>?>
        get() = _itemsLiveData


    init {
        _itemsLiveData = MutableLiveData<MutableList<ParentModel>?>()


    }


}