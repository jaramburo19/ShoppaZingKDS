package com.byteswiz.shoppazingkds.dataadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
/*import com.byteswiz.shoppazingkds.ChildModel*/
import com.byteswiz.shoppazingkds.R


/*
class ChildAdapter(private val children: List<ChildModel>)
    : RecyclerView.Adapter<ChildAdapter.ViewHolder>(){

    private object VIEW_TYPES {
        const val Header = 1
        const val Normal = 2
        const val Footer = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

       */
/* val v =  LayoutInflater.from(parent.context)
            .inflate(R.layout.child_recycler, parent, false)
        return ViewHolder(v)
*//*


        val v: View

        if (viewType === VIEW_TYPES.Footer) {
            v = LayoutInflater.from(parent.context).inflate(R.layout.child_footer, parent, false)
            return ViewHolder(v)
        }

        v = LayoutInflater.from(parent.context).inflate(R.layout.child_recycler, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return children.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val child = children[position]
        //holder.imageView.setImageResource(child.image)
        holder.textView.text = child.title


    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val textView : TextView = itemView.findViewById(R.id.product_name)
       // val imageView: ImageView = itemView.findViewById(R.id.child_imageView)

    }

    // Define a ViewHolder for Footer view
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                // Do whatever you want on clicking the item
            }
        }
    }
}*/
