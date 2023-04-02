package com.byteswiz.shoppazingkds.cart


import com.byteswiz.parentmodel.ParentModel
import io.paperdb.Paper

class ShoppingCart {

    companion object {

        //<editor-fold desc="Customers">
        fun addOrder(order: ParentModel){
            val orders = ShoppingCart.getOrders()
            orders.add(order)
            ShoppingCart.saveOrders(orders)
        }

        fun getOrders(): MutableList<ParentModel> {
            return Paper.book().read("orders", mutableListOf())
        }

        fun updateStatus(orderStatusId: Int, todaysOrderNo: String){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.TodaysOrderNo==todaysOrderNo }
            for(t in targetItem.listIterator()){
                t.orderStatusId =orderStatusId
            }
            saveOrders(orders)
        }

        fun updateChildItemStatus(todaysOrderNo: String, itemId: Long, flag: Boolean){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.filter { it.TodaysOrderNo==todaysOrderNo }
            for(t in targetItem.listIterator()){
                for(c in t.children){
                    if(c.itemId==itemId)
                        c.IsDone=flag
                }
            }
            saveOrders(orders)
        }

        fun updateSyncStatus(isSynced: Boolean, todaysOrderNo: String){
            val orders = ShoppingCart.getOrders()
            val targetItem = orders.singleOrNull { it.TodaysOrderNo==todaysOrderNo }
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