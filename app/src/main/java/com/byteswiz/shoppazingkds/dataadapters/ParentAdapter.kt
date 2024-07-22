package com.byteswiz.shoppazingkds.dataadapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.R
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.interfaces.OnButtonClicked
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.kds_timers.CountUpTimer
import com.byteswiz.shoppazingkds.roomdb.AppDatabase
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_STATUS_PREPARING
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_ONLINE_DELIVERY
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_ONLINE_PICKUP
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_WALKIN
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ParentAdapter(var context: Context, private var activity:Activity, private var parents: MutableList<ParentModel>, listener: OnParentButtonClicked) :    RecyclerView.Adapter<ParentAdapter.ViewHolder>(){
    private val viewPool = RecyclerView.RecycledViewPool()
    private val _activity = activity
    private val _context = context

    private var mListener: OnParentButtonClicked? = null
    private lateinit var mRecentlyDeletedItem: ParentModel
    var mRecentlyDeletedItemPosition: Int =0
    init {
        mListener = listener

    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_recycler, parent, false)
        return ViewHolder(v)
    }

    fun setOrders(order: MutableList<ParentModel>){
        parents =order
        notifyDataSetChanged()
    }
    fun notifyChanged(position: Int, orderStatusId: Int){
        parents[position].orderStatusId = orderStatusId
        notifyItemChanged(position)
    }

    fun addOrder(order: ParentModel){
        parents.add(order)
        //notifyDataSetChanged()
        notifyItemInserted(parents.size - 1)
    }
    fun removeAndAdd(order:ParentModel){

    }


    fun removeAt(position: Int) {
        mRecentlyDeletedItem  = parents.removeAt(position)
        mRecentlyDeletedItemPosition = position
        notifyItemRemoved(position)


    }

    override fun getItemCount(): Int {
        return parents.size
    }


    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val parent = parents[position]

        if(parent.OrderTypeId== ORDER_TYPES_WALKIN)
            holder.txtTransType.text ="WALK-IN"
        else if(parent.OrderTypeId== ORDER_TYPES_ONLINE_DELIVERY)
            holder.txtTransType.text ="DELIVERY"
        else
            holder.txtTransType.text ="PICKUP"

        if(parent.OrderTypeId== ORDER_TYPES_ONLINE_DELIVERY || parent.OrderTypeId== ORDER_TYPES_ONLINE_PICKUP)
            holder.txtOrderNo.text ="OrderNo: " + parent.TodaysOrderNo  + ", Online#" + parent.OrderRefNo
        else
            holder.txtOrderNo.text = "OrderNo: " + parent.TodaysOrderNo

        if(parent.receiptNo.isNullOrEmpty() || parent.receiptNo=="")
            holder.txtReceiptNo.visibility=View.GONE
        else
        {
            holder.txtReceiptNo.text ="#"+  parent.receiptNo
            holder.txtReceiptNo.visibility=View.VISIBLE
        }

        if(parent.ticketName !="" || !parent.ticketName.isNullOrEmpty()){
            holder.txtTicketName.text =parent.ticketName
            holder.txtTicketName.visibility=View.VISIBLE
        }
        else
            holder.txtTicketName.visibility-=View.GONE


        when(parent.orderStatusId){
            2->{
                holder.card_view.setBackgroundResource(R.drawable.card_view_border);
            }
            3 ->{
                holder.card_view.setBackgroundResource(R.drawable.card_view_border_preparing);
            }
        }

        holder.constHeader.setOnLongClickListener(){it->
            //holder.btnPreparing.visibility=View.GONE
            holder.card_view.setBackgroundResource(R.drawable.card_view_border_preparing);
            mListener!!.onPreparingClicked( parent.receiptNo, parent.localUniqueId,holder.layoutPosition,true)

            return@setOnLongClickListener true
        }


        /* if(parent.orderStatusId== ORDER_STATUS_PREPARING)
             return*/

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            /* val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
             val currentDateandTime = sdf.format(Date())
             val event_date: Date = dateFormat.parse(parent.KDSOrderDate)
             var  current_date = dateFormat.parse(currentDateandTime)
             val diff: Long = current_date.getTime() - event_date.getTime()*/

            val startDateTime = dateFormat.parse(parent.KDSOrderDate)
            val currentDateandTime = dateFormat.format(Date())
            //val event_date: Date = dateFormat.parse(parent.KDSOrderDate)
            var  current_date = dateFormat.parse(currentDateandTime)

            var different = current_date.getTime() - startDateTime.getTime()
            val secondsInMilli:Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val elapsedDays = different / daysInMilli
            different %= daysInMilli
            val elapsedHours = different / hoursInMilli
            different %= hoursInMilli
            val elapsedMinutes = different / minutesInMilli
            different %= minutesInMilli
            val elapsedSeconds = different / secondsInMilli
            holder.txtDays.text = (if (elapsedDays > 1) "$elapsedDays Days" else "$elapsedDays Day")
            holder.txtDays.visibility = if (elapsedDays > 0) View.VISIBLE else View.GONE
            val elapsedTimeMilliseconds = ((elapsedHours * 60 * 60 * 1000
                    + elapsedMinutes * 60 * 1000
                    + elapsedSeconds * 1000)).toInt()

            /*  holder.timeText.base =diff
              holder.timeText.start()*/

            holder.timeText.setBase(SystemClock.elapsedRealtime() - elapsedTimeMilliseconds);
            holder.timeText.start();



        } catch (e: Exception) {
            e.printStackTrace()
        }


        val childLayoutManager = LinearLayoutManager(
            holder.recyclerView.context,
            LinearLayout.VERTICAL,
            false
        )
        childLayoutManager.initialPrefetchItemCount = 4


        var adapter = ExampleAdapter(parent.diningOptionName, parent.qNo, parent.orderStatusId,parent.TodaysOrderNo, object : OnButtonClicked{
            override fun onPreparingClicked() {
                holder.card_view.setBackgroundResource(R.drawable.card_view_border_preparing);
                mListener!!.onPreparingClicked( parent.receiptNo, parent.localUniqueId,holder.layoutPosition,false)

            }
            override fun onCompletedClicked() {

                mListener!!.onCompletedClicked(parent.receiptNo,
                    parent.localUniqueId, holder.layoutPosition, parent.TodaysOrderNo,holder.timeText.text.toString())
            }

            override fun onRecallClicked() {
                mListener!!.onRecallClicked(parent.receiptNo, parent.localUniqueId)

            }

            override fun onChildItemClicked(parentModelId: Int, itemId: Long, flag: Boolean) {
                mListener!!.onChildItemClicked(parentModelId,itemId, flag)
            }

        })



        holder.recyclerView.adapter = adapter

        getChildrens(_context,parent.Id, adapter)

        holder.recyclerView.apply {
            layoutManager = childLayoutManager
            //adapter = ChildAdapter(parent.children)
            //adapter = adapter
            setRecycledViewPool(viewPool)
        }

        if (holder.recyclerView.itemDecorationCount == 0) {
            val itemDecorator = DividerItemDecoration(_activity, VERTICAL)
            holder.recyclerView.addItemDecoration(itemDecorator)
        }


        //adapter.notifyDataSetChanged()

    }

    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    fun getChildrens(_context: Context, parentModelId:Int, adapter:ExampleAdapter) {
        val db = AppDatabase.getAppDataBase(_context)!!
        val ordersDao = db.ordersDao()
        executor.execute {
            var chiliders = ordersDao.getChildrens(parentModelId)
            handler.post {
                adapter.data = chiliders
                adapter.notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recyclerView : RecyclerView =  itemView.findViewById(R.id.rv_child)
        val txtOrderNo: TextView = itemView.findViewById(R.id.txtOrderNo)
        val txtTransType: TextView = itemView.findViewById(R.id.txtTransType)
        val timeText: Chronometer = itemView.findViewById(R.id.timeText)
        val txtDays: TextView = itemView.findViewById(R.id.txtDays)
        val txtTicketName: TextView = itemView.findViewById(R.id.txtTicketName)
        val txtReceiptNo: TextView = itemView.findViewById(R.id.txtReceiptNo)
        val card_view: CardView =itemView.findViewById(R.id.card_view)
        val constHeader: ConstraintLayout =itemView.findViewById(R.id.constHeader)



        var timer: CountDownTimer? = null
    }
}