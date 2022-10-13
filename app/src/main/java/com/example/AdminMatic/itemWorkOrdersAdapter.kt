package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso


class ItemWorkOrdersAdapter(list: MutableList<WorkOrder>, private val context: Context, private val unit: String, private val cellClickListener: WorkOrderCellClickListener) : RecyclerView.Adapter<ItemWorkOrderViewHolder>() {

    var filterList:MutableList<WorkOrder> = emptyList<WorkOrder>().toMutableList()

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemWorkOrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemWorkOrderViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ItemWorkOrderViewHolder, position: Int) {

        val workOrder: WorkOrder = filterList[position]
        holder.bind(workOrder, context, unit)

        val serviceStatusImageView: ImageView = holder.itemView.findViewById(R.id.list_icon_iv)

        when (workOrder.status) {
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(serviceStatusImageView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(serviceStatusImageView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(serviceStatusImageView)
        }


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onWorkOrderCellClickListener(data, position)
        }


    }

    override fun getItemCount(): Int{
        //print("getItemCount = ${filterList.size}")
        return filterList.size
    }


}

class ItemWorkOrderViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_wo_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mQtyView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mQtyView = itemView.findViewById(R.id.list_qty)
    }

    fun bind(workOrder: WorkOrder, context: Context, unit: String) {
        mNameView?.text = context.getString(R.string.item_wo_title, workOrder.title, workOrder.custName)
        mQtyView?.text = context.getString(R.string.item_remaining_qty, workOrder.remQty, unit)
    }

}