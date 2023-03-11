package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class ItemVendorsAdapter(list: MutableList<Vendor>, private val context: Context, private val unit: String, private val cellClickListener: VendorCellClickListener) : RecyclerView.Adapter<ItemVendorViewHolder>() {

    var filterList:MutableList<Vendor> = emptyList<Vendor>().toMutableList()

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVendorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemVendorViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ItemVendorViewHolder, position: Int) {


        val vendor: Vendor = filterList[position]
        holder.bind(vendor, context, unit)


        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onVendorCellClickListener(data)
        }



    }

    override fun getItemCount(): Int{

        //print("getItemCount = ${filterList.size}")
        return filterList.size

    }

}

class ItemVendorViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_vendor_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mCostView: TextView? = null
    private var mCl: ConstraintLayout? = null


    init {
        mNameView = itemView.findViewById(R.id.list_name)
        mCostView = itemView.findViewById(R.id.list_cost_tv)
        mCl = itemView.findViewById(R.id.item_vendor_list_item)
    }

    fun bind(vendor: Vendor, context: Context, unit: String) {
        mNameView?.text = vendor.name

        // Only show value if items money permission is available
        if (GlobalVars.permissions!!.itemsMoney == "1") {
            mCostView!!.visibility = View.VISIBLE
            mCostView?.text = context.getString(R.string.item_price_each, vendor.cost, unit)
        }
        else {
            mCostView!!.visibility = View.INVISIBLE
        }


        if (vendor.preferred == "1") {
            mCl!!.background = context.getColor(R.color.backgroundHighlight).toDrawable()
        }

    }



}