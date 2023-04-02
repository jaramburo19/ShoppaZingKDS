package com.byteswiz.shoppazingkds.datafactories

import com.byteswiz.parentmodel.ParentModel
import java.util.*


class ParentDataFactory(private val parents: MutableList<ParentModel>) {

    private var _parents =parents
    private val random = Random()

    // private val titles =  arrayListOf( "Now Playing", "Classic", "Comedy", "Thriller", "Action", "Horror", "TV Series")


   /* private fun randomTitle(): String {
        val index = random.nextInt(parents.size)
        return parents[index]
    }*/

    fun getOrders():MutableList<ParentModel>{
        return _parents
    }

    fun setOrders(orders: MutableList<ParentModel>){
        _parents = orders
    }

/*
    private fun randomChildren(): List<CartItem> {
        return ChildDataFactory.getChildren()
    }
*/


   /* fun getParents(count: Int): ArrayList<ParentModel> {
        val parents = ArrayList<ParentModel>()
        repeat(count) {
            val parent = ParentModel(randomTitle(), randomChildren())
            parents.add(parent)
        }
        return parents
    }*/
}