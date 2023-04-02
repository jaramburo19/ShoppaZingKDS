package com.byteswiz.shoppazingkds.dataadapters


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byteswiz.parentmodel.KDSCartItem

import com.byteswiz.shoppazingkds.R
import com.byteswiz.shoppazingkds.interfaces.OnButtonClicked
import com.mikhaellopez.hfrecyclerviewkotlin.HFRecyclerView


//this class uses //Header Footer//    implementation 'com.mikhaellopez:hfrecyclerview-kotlin:1.1.1'
class ExampleAdapter(var diningOptionName: String,
                     var qNo: String?,
                     var orderStatusId: Int,
                     var todaysOrderNo: String,
                     listener: OnButtonClicked) : HFRecyclerView<KDSCartItem>(true, true) { // With Header & With Footer

    private var mListener: OnButtonClicked? = null
    private var _todaysOrderNo: String = todaysOrderNo

    init {
        mListener = listener

    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ItemViewHolder -> holder.bind(getItem(position),mListener,_todaysOrderNo)
            is ViewHolder.HeaderViewHolder -> {
                holder.bind(diningOptionName,qNo)

                when(orderStatusId){
                    2->{
                        holder.btnRecall.visibility=View.GONE
                        holder.btnPreparing.visibility =View.VISIBLE
                    }
                    3->{
                        holder.btnPreparing.visibility=View.GONE
                        holder.btnRecall.visibility=View.GONE
                    }
                    4->{
                        //holder.btnCompleted.visibility=View.GONE
                        holder.btnPreparing.visibility=View.GONE
                        holder.btnRecall.visibility=View.VISIBLE
                    }
                }
                holder.btnPreparing.setOnClickListener{
                    mListener!!.onPreparingClicked()
                    holder.btnPreparing.visibility=View.GONE
                }

                /*holder.btnCompleted.setOnClickListener{
                    mListener!!.onCompletedClicked()
                }*/
                holder.btnRecall.setOnClickListener{
                    mListener!!.onRecallClicked()
                }

            }
            is ViewHolder.FooterViewHolder -> {
                when(orderStatusId){
                    2->{
                        holder.btnRecall.visibility=View.GONE
                        holder.btnPreparing.visibility =View.VISIBLE
                    }
                    3->{
                        holder.btnPreparing.visibility=View.GONE
                        holder.btnRecall.visibility=View.GONE
                    }
                    4->{
                        holder.btnCompleted.visibility=View.GONE
                        holder.btnPreparing.visibility=View.GONE
                        holder.btnRecall.visibility=View.VISIBLE
                    }
                }
                holder.btnPreparing.setOnClickListener{
                    mListener!!.onPreparingClicked()
                    holder.btnPreparing.visibility=View.GONE
                }

                holder.btnCompleted.setOnClickListener{
                    mListener!!.onCompletedClicked()
                }
                holder.btnRecall.setOnClickListener{
                    mListener!!.onRecallClicked()
                }


            }
        }
    }

    //region Override Get ViewHolder
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder =
            ViewHolder.ItemViewHolder(inflater.inflate(R.layout.child_recycler, parent, false))

    override fun getHeaderView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder =
            ViewHolder.HeaderViewHolder(inflater.inflate(R.layout.child_header, parent, false))

    override fun getFooterView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder =
            ViewHolder.FooterViewHolder(inflater.inflate(R.layout.child_footer, parent, false))
    //endregion

    //region ViewHolder Header and Footer
    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var text: TextView = itemView.findViewById<View>(R.id.product_name) as TextView
            var txtQty: TextView = itemView.findViewById<View>(R.id.product_quantity) as TextView

            var modifiers: TextView = itemView.findViewById<View>(R.id.modifiers) as TextView
            var sp: TextView = itemView.findViewById<View>(R.id.txtSpecialInstructions) as TextView
            var imgCheck:ImageView =itemView.findViewById(R.id.imgCheck)

            var IsTicked: Boolean =false
            fun bind(item: KDSCartItem, listener: OnButtonClicked?, orderNo: String) {
                text.text = item.CombiName

                IsTicked = if(item.IsDone==null)
                                false
                            else item.IsDone==true

                if(IsTicked)
                {
                    text.setTextColor(Color.RED)
                    imgCheck.visibility=View.VISIBLE

                }
                else
                {
                    text.setTextColor(Color.BLACK)
                    imgCheck.visibility=View.GONE

                }

                itemView.setOnClickListener{
                   if(!IsTicked){
                       IsTicked=true
                       text.setTextColor(Color.RED)
                       imgCheck.visibility=View.VISIBLE

                   }
                    else{
                       IsTicked=false
                       item.IsDone=IsTicked
                       text.setTextColor(Color.BLACK)
                       imgCheck.visibility=View.GONE
                    }

                    listener!!.onChildItemClicked(orderNo, item.itemId!!,IsTicked)
                }

                if(!item.SpecialInstructions.isNullOrEmpty()){
                    sp.visibility=View.VISIBLE
                    sp.text = item.SpecialInstructions
                }
                else
                    sp.visibility=View.GONE


                var qtyText: String =""
                if (item.quantity.rem(1).equals(0.0))
                    qtyText = item.quantity.toInt().toString()
                else
                    qtyText = item.quantity.toString()

                txtQty.text = "x " + qtyText

                if(!item.modifiers.isNullOrEmpty())
                {
                    val stringBuilder = StringBuilder()
                    for(m in item.modifiers!!.iterator()){

                        stringBuilder.append(m.name + " x " + qtyText)
                        stringBuilder.append("\n")
                        //stringBuilder.append("\n")
                    }
                    modifiers.text = stringBuilder.toString().trim { it <= ',' }

                }
                else
                {
                    modifiers.visibility = View.GONE
                    /*  itemView.modifiers.text = "x" + cartItem.quantity.toString()*/
                }
            }

        }

        internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var btnPreparing: Button = itemView.findViewById<View>(R.id.btnPreparing) as Button
            //var btnCompleted: Button = itemView.findViewById<View>(R.id.btnCompleted) as Button
            var btnRecall: Button = itemView.findViewById<View>(R.id.btnRecall) as Button

            var txtDiningOption: TextView = itemView.findViewById<View>(R.id.txtDiningOption) as TextView
            var txtQNo: TextView = itemView.findViewById<View>(R.id.txtQNo) as TextView
            fun bind(diningOptionName: String, qNo: String?){
                txtDiningOption.text = diningOptionName
                if(!qNo.isNullOrEmpty())
                {
                    txtQNo.visibility=View.VISIBLE
                    txtQNo.text ="QUEUE: " + qNo
                }
                else
                    txtQNo.visibility=View.GONE

            }
        }

        internal class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var btnPreparing: Button = itemView.findViewById<View>(R.id.btnPreparing) as Button
            var btnCompleted: Button = itemView.findViewById<View>(R.id.btnCompleted) as Button
            var btnRecall: Button = itemView.findViewById<View>(R.id.btnRecall) as Button

        }

    }
    //endregion

}