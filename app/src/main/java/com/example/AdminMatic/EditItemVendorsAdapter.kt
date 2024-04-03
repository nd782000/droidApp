package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R


class EditItemVendorsAdapter(
    private val list: MutableList<Vendor>,
    private val context: Context,
    private val cellClickListener: EditItemVendorCellClickListener
)

    : RecyclerView.Adapter<EditVendorViewHolder>() {


    var filterList:MutableList<Vendor> = emptyList<Vendor>().toMutableList()

    private var initialBindOccurred = false

    var queryText = ""

    init {
        filterList = list
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditVendorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EditVendorViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EditVendorViewHolder, position: Int) {
        val vendor: Vendor = filterList[position]
        holder.bind(vendor, context)

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onEditItemVendorCellClickListener(data)
        }

        // Delete Button
        val deleteBtn:Button = holder.itemView.findViewById(R.id.delete_btn)
        deleteBtn.setOnClickListener {
            println("Delete button pressed")
        }

        // Preferred Switch
        val prefSwitch:SwitchCompat = holder.itemView.findViewById(R.id.preferred_switch)
        prefSwitch.isFocusable = false
        prefSwitch.isFocusableInTouchMode = false

        if (vendor.preferred == "1") {
            if (!prefSwitch.isChecked) {
                prefSwitch.isChecked = true
            }
        }
        else {
            prefSwitch.isChecked = false
        }


        prefSwitch.setOnClickListener {
            println("onclick")
            prefSwitch.isChecked = !prefSwitch.isChecked
            if (!prefSwitch.isChecked) {
                cellClickListener.onEditItemVendorCheckListener(vendor)
            }
            else {

            }
        }

        /*
        prefSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                println("Checked On")
            }
            else {
                println("Checked Off")
            }
        }

         */

    }

    override fun getItemCount(): Int{
        return filterList.size
    }

    // Clean all elements of the recycler
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: MutableList<Vendor>) {
        list.addAll(list)
        notifyDataSetChanged()
    }
}

class EditVendorViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.edit_item_vendor_list_item, parent, false)) {
    private var mNameView: TextView? = null
    private var mCostView: TextView? = null
    private var mPriceView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.name_tv)
        mCostView = itemView.findViewById(R.id.cost_tv)
        mPriceView = itemView.findViewById(R.id.suggested_price_tv)
    }

    fun bind(vendor: Vendor, context: Context) {
        mNameView?.text = vendor.name
        mCostView?.text = context.getString(R.string.cost_x, vendor.cost)
        mPriceView?.text = context.getString(R.string.suggested_price_x, vendor.price)
    }



}