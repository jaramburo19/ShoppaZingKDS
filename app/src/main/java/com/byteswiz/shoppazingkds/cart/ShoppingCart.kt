package com.byteswiz.shoppazingkds.cart


import android.content.Context
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.roomdb.AppDatabase
import com.byteswiz.shoppazingkds.utils.Constants
import io.paperdb.Paper
import java.util.UUID

class ShoppingCart() {

    companion object {

        //<editor-fold desc="Customers">
        fun addOrder(order: ParentModel,_context:Context):Long{
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

           //val _tempchildren = order.children
            //order.children =null

           /*val orders = ShoppingCart.getOrders()

           var targetItem = orders.filter { it->it.TodaysOrderNo==order.TodaysOrderNo }

            var uuid: String =""
            uuid = UUID.randomUUID().toString() + orders.count() + 1

            order.localUniqueId = uuid
            removeItem(order)
            orders.add(order)

            saveOrders(orders)*/


            val orderCount = ordersDao.ordersCount()

            var uuid: String =""
            uuid = UUID.randomUUID().toString() + orderCount + 1
            order.localUniqueId =uuid

            var newId = ordersDao.insertOrder(order)

            for(kitem in order.children!!.iterator()){
                kitem.ParentModelId = newId
            }
            ordersDao.insertKDSCartItem(order.children!!)
            return  newId
        }

        fun getOrders(_context:Context): MutableList<ParentModel> {
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            return  ordersDao.getNotReadyOrders()
            //return Paper.book().read("orders", mutableListOf())
        }

        fun getReadyOrders(_context:Context): MutableList<ParentModel> {
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            return  ordersDao.getReadyOrders()
            //return Paper.book().read("orders", mutableListOf())
        }

        fun getOrderById(_context:Context, Id:Int): ParentModel {
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            return  ordersDao.getOrderById(Id)
            //return Paper.book().read("orders", mutableListOf())
        }


        fun updateStatus(orderStatusId: Int, localUniqueId: String,_context: Context){
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            ordersDao.updateOrderStatusIdByLocalUID(localUniqueId,orderStatusId)

            /*val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.localUniqueId==localUniqueId }
            for(t in targetItem.listIterator()){
                t.orderStatusId =orderStatusId
            }

            saveOrders(orders)*/

        }

        fun getOrderByLocalUID(localUniqueId: String,_context: Context):ParentModel?{
           /* val orders = getOrders().filter { it->it.orderStatusId== Constants.ORDER_STATUS_READY }
            val targetItem = orders.find{ it.qrcode==qrCode}*/
            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            /*val orders = ShoppingCart.getOrders()
            val targetItem = orders.singleOrNull { it.localUniqueId==localUniqueId }*/

            val targetItem = ordersDao.getOrderByLocalUID(localUniqueId)

            return targetItem
        }

        fun updateChildItemStatus(parentModelId: Int, itemId: Long, flag: Boolean,_context: Context){
           /* val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.localUniqueId==localUniqueId }
            for(t in targetItem.listIterator()){
                for(c in t.children){
                    if(c.itemId==itemId)
                        c.IsDone=flag
                }
            }
            saveOrders(orders)*/

            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            ordersDao.updateChildStatus(flag,itemId,parentModelId)

            var getStat = ordersDao.getChildrens(parentModelId)
            var analyze = getStat


        }

        fun updateSyncStatus(isSynced: Boolean, localUniqueId: String,_context:Context){
          /*  val orders = ShoppingCart.getOrders()
            val targetItem = orders.singleOrNull { it.localUniqueId==localUniqueId }
            targetItem!!.IsSynced =isSynced
            saveOrders(orders)*/


            val db = AppDatabase.getAppDataBase(_context)!!
            val ordersDao = db.ordersDao()

            ordersDao.updateSyncStatus(localUniqueId,isSynced)

        }


        fun saveOrders(orders: MutableList<ParentModel>) {
            Paper.book().write("orders", orders)
        }

       /* fun clearCustomer(){
            val customers = ShoppingCart.getOrders()
            customers.clear()
            saveOrders(customers)
        }
*/
       /* fun removeItem(cartItem: ParentModel) {

            val cart = getOrders()
            val targetItem = cart.singleOrNull { it.qrcode == cartItem.qrcode }

            if (targetItem != null) {
                cart.remove(targetItem)
                saveOrders(cart)
            }

        }*/



    }

}