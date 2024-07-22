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
import com.byteswiz.shoppazingkds.databinding.ActivityRecallBinding
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_PREPARING
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_READY

class RecallActivity : AppCompatActivity() {
    lateinit var _adapter: ParentAdapter
    //lateinit var recyclerView: RecyclerView
    lateinit var binding: ActivityRecallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityRecallBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_recall)
        setContentView(binding.root)
        setupToolBar("Recall order")

        _adapter = ParentAdapter(this, ShoppingCart.getOrders(), object : OnParentButtonClicked {
            override fun onPreparingClicked(receiptNo: String, localUniqueId: String) {

            }

            override fun onCompletedClicked(receiptNo: String, localUniqueId: String, position: Int, orderRefNo:String, textDuration:String) {

            }

            override fun onRecallClicked(receiptNo: String, localUniqueId: String) {
                ShoppingCart.updateStatus(ORDER_STATUS_PREPARING,localUniqueId)
                finish()
                //Toast.makeText(this@RecallActivity,"Recall Clicked: " + receiptNo + " " + qrcode, Toast.LENGTH_SHORT).show()
            }

            override fun onChildItemClicked(localUniqueId: String, itemId: Long, flag:Boolean) {
                ShoppingCart.updateChildItemStatus(localUniqueId,itemId,flag)
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



        var doneOrders = ShoppingCart.getOrders()
            .filter { it->it.TodaysOrderNo!=null && it.orderStatusId== ORDER_STATUS_READY }
        if(doneOrders.isNotEmpty()){
            binding.recyclerView.visibility=View.VISIBLE
            binding.nodata.root.visibility=View.GONE


            _adapter.setOrders(doneOrders
                .sortedByDescending { it.TodaysOrderNo }
                .toMutableList())
            //recyclerView = findViewById(R.id.rv_parent)

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(this@RecallActivity, LinearLayoutManager.HORIZONTAL,false)
                //adapter = _adapter
            }
            binding.recyclerView.adapter = _adapter
            _adapter.notifyDataSetChanged()
        }
        else{
            binding.recyclerView.visibility=View.GONE
            binding.nodata.txtNoDataDesc.text="There is no orders to recall \n yet  right now."
            binding.nodata.root.visibility=View.VISIBLE
        }

    }
}