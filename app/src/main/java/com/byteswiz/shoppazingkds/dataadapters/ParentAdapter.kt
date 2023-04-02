package com.byteswiz.shoppazingkds.dataadapters

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.R
import com.byteswiz.shoppazingkds.cart.ShoppingCart
import com.byteswiz.shoppazingkds.interfaces.OnButtonClicked
import com.byteswiz.shoppazingkds.interfaces.OnParentButtonClicked
import com.byteswiz.shoppazingkds.kds_timers.CountUpTimer
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_ONLINE_DELIVERY
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_ONLINE_PICKUP
import com.byteswiz.shoppazingkds.utils.Constants.ORDER_TYPES_WALKIN
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ParentAdapter(private var parents: MutableList<ParentModel>, listener: OnParentButtonClicked) :    RecyclerView.Adapter<ParentAdapter.ViewHolder>(){
    private val viewPool = RecyclerView.RecycledViewPool()

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

    fun addOrder(order: ParentModel){
        parents.add(order)
        notifyDataSetChanged()

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
                mListener!!.onPreparingClicked( parent.receiptNo, parent.TodaysOrderNo)

            }
            override fun onCompletedClicked() {
                mListener!!.onCompletedClicked(parent.receiptNo, parent.TodaysOrderNo, position)
            }

            override fun onRecallClicked() {
                if(parent.TodaysOrderNo!=null)
                    mListener!!.onRecallClicked(parent.receiptNo,parent.TodaysOrderNo)
               /* else
                    mListener!!.onRecallClicked(parent.receiptNo,"")*/
            }

            override fun onChildItemClicked(orderNo: String, itemId: Long, flag: Boolean) {
                mListener!!.onChildItemClicked(orderNo,itemId, flag)
            }

        })
        adapter.data = parent.children
        holder.recyclerView.apply {
            layoutManager = childLayoutManager
            //adapter = ChildAdapter(parent.children)
            //adapter = adapter
            setRecycledViewPool(viewPool)
        }

        holder.recyclerView.adapter = adapter
        //adapter.data = parent.children
        adapter.notifyDataSetChanged()

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



        var timer: CountDownTimer? = null
    }
}