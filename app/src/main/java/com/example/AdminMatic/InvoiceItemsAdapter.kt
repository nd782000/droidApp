package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R




class InvoiceItemsAdapter(list: MutableList<InvoiceItem>, private val context: Context) : RecyclerView.Adapter<InvoiceItemViewHolder>() {

    var filterList:MutableList<InvoiceItem> = emptyList<InvoiceItem>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return InvoiceItemViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: InvoiceItemViewHolder, position: Int) {

        val invoiceItem: InvoiceItem = filterList[position]
        holder.bind(invoiceItem, context)



    }

    override fun getItemCount(): Int{
        print("getItemCount = ${filterList.size}")
        return filterList.size
    }


}

class InvoiceItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.invoice_item_list_item, parent, false)) {

    private var mNameView: TextView? = null
    private var mPriceView: TextView? = null
    private var mDescriptionView: TextView? = null


    init {
        mNameView = itemView.findViewById(R.id.invoice_item_name_tv)
        mPriceView = itemView.findViewById(R.id.invoice_item_price_tv)
        mDescriptionView = itemView.findViewById(R.id.invoice_item_description_tv)
    }

    fun bind(invoiceItem: InvoiceItem, context: Context) {
        mNameView?.text = invoiceItem.item
        mPriceView?.text = context.getString(R.string.contract_item_total_price, invoiceItem.act, invoiceItem.price, invoiceItem.total)

        /*
        var descriptionString = ""
        val iterator = contractItem.tasks!!.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            descriptionString = descriptionString + "- " + item.taskDescription
            if (iterator.hasNext()) {
                descriptionString += "\n"
            }
        }
        */

        mDescriptionView?.text = invoiceItem.custDesc

    }
}