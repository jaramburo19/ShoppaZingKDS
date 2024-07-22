package com.byteswiz.shoppazingkds.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.byteswiz.parentmodel.KDSCartItem
import com.byteswiz.parentmodel.ParentModel

@Dao
interface OrdersDao {


    @Query("SELECT * FROM Orders where Id = :Id")
    fun getOrderById(Id:Int):ParentModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(item: ParentModel):Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKDSCartItem(item: List<KDSCartItem>)


    @Query("SELECT Count(localUniqueId) FROM Orders")
    fun ordersCount():Int

    @Query("SELECT * FROM Orders where orderStatusId <> 4")
    fun getNotReadyOrders():MutableList<ParentModel>

    @Query("SELECT * FROM Orders where orderStatusId == 4")
    fun getReadyOrders():MutableList<ParentModel>

    @Query("SELECT * FROM Orders where localUniqueId =:uid")
    fun getOrderByLocalUID(uid: String):ParentModel

    @Query("UPDATE Orders  SET orderStatusId =:statusId where localUniqueId =:uid")
    fun updateOrderStatusIdByLocalUID(uid:String, statusId:Int)


    @Query("UPDATE Orders  SET IsSynced =:isSynced where localUniqueId =:uid")
    fun updateSyncStatus(uid:String, isSynced:Boolean)

    @Query("UPDATE cartitems  SET IsDone =:flag where itemId =:itemId and ParentModelId =:parentModelId")
    fun updateChildStatus(flag: Boolean, itemId:Long, parentModelId: Int)


    @Query("SELECT * FROM cartitems where ParentModelId =:parentModelId")
    fun getChildrens(parentModelId: Int):List<KDSCartItem>

}