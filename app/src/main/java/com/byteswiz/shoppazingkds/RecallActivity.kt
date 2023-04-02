package com.byteswiz.shoppazingkds

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.dataadapters.ParentAdapter
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_PREPARING
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_READY

class RecallActivity : AppCompatActivity() {
    lateinit var _adapter: ParentAdapter
    lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recall)
        setupToolBar("Recall order")

        _adapter = ParentAdapter(ShoppingCart.getOrders(), object : OnParentButtonClicked {
            override fun onPreparingClicked(receiptNo: String, todaysOrderNo: String) {

            }

            override fun onCompletedClicked(receiptNo: String, todaysOrderNo: String, position: Int) {

            }

            override fun onRecallClicked(receiptNo: String, todaysOrderNo: String) {
                ShoppingCart.updateStatus(ORDER_STATUS_PREPARING,todaysOrderNo)
                finish()
                //Toast.makeText(this@RecallActivity,"Recall Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
            }

            override fun onChildItemClicked(orderNo: String, itemId: Long, flag:Boolean) {
                ShoppingCart.updateChildItemStatus(orderNo,itemId,flag)
            }

        })

        initRecycler()
    }

    private fun setupToolBar(title: String) {

        val toolbar: Toolbar = findViewById(R.id.htab_toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) supportActionBar!!.setTitle(title)
       // supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_cancel__white_24)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecycler(){
        _adapter.setOrders(ShoppingCart.getOrders()
            .filter { it->it.TodaysOrderNo!=null }
            .sortedByDescending { it.TodaysOrderNo }
            .filter { it->it.orderStatusId==ORDER_STATUS_READY }.toMutableList())
        recyclerView = findViewById(R.id.rv_parent)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecallActivity, LinearLayoutManager.HORIZONTAL,false)
            //adapter = _adapter
        }
        recyclerView.adapter = _adapter
        _adapter.notifyDataSetChanged()
    }
}