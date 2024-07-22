package com.byteswiz.shoppazingkds.cart


import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.utils.Constants
import io.paperdb.Paper
import java.util.UUID

class ShoppingCart {

    companion object {

        //<editor-fold desc="Customers">
        fun addOrder(order: ParentModel){
            val orders = ShoppingCart.getOrders()

           var targetItem = orders.filter { it->it.TodaysOrderNo==order.TodaysOrderNo }

            var uuid: String =""
            uuid = UUID.randomUUID().toString() + orders.count() + 1

            order.localUniqueId = uuid
            removeItem(order)
            orders.add(order)

            saveOrders(orders)
        }

        fun getOrders(): MutableList<ParentModel> {
            return Paper.book().read("orders", mutableListOf())
        }

        fun updateStatus(orderStatusId: Int, localUniqueId: String){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.localUniqueId==localUniqueId }
            for(t in targetItem.listIterator()){
                t.orderStatusId =orderStatusId
            }
            saveOrders(orders)
        }

        fun getOrderByOrderRefNo(localUniqueId: String, orderRefNo: String):ParentModel?{
           /* val orders = getOrders().filter { it->it.orderStatusId== Constants.ORDER_STATUS_READY }
            val targetItem = orders.find{ it.qrcode==qrCode}*/

            val orders = ShoppingCart.getOrders()
            val targetItem = orders.singleOrNull { it.localUniqueId==localUniqueId }

            return targetItem
        }

        fun updateChildItemStatus(localUniqueId: String, itemId: Long, flag: Boolean){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.localUniqueId==localUniqueId }
            for(t in targetItem.listIterator()){
                for(c in t.children){
                    if(c.itemId==itemId)
                        c.IsDone=flag
                }
            }
            saveOrders(orders)
        }

        fun updateSyncStatus(isSynced: Boolean, localUniqueId: String){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.singleOrNull { it.localUniqueId==localUniqueId }
            targetItem!!.IsSynced =isSynced
            saveOrders(orders)
        }


        fun saveOrders(orders: MutableList<ParentModel>) {
            Paper.book().write("orders", orders)
        }

        fun clearCustomer(){
            val customers = ShoppingCart.getOrders()
            customers.clear()
            saveOrders(customers)
        }

        fun removeItem(cartItem: ParentModel) {

            val cart = getOrders()
            val targetItem = cart.singleOrNull { it.qrcode == cartItem.qrcode }

            if (targetItem != null) {
                cart.remove(targetItem)
                saveOrders(cart)
            }

        }



    }

}