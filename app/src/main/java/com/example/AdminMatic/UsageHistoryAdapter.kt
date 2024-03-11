package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.time.LocalDateTime


class UsageHistoryAdapter(list: MutableList<Usage>, private val context: Context, private val unit: String) : RecyclerView.Adapter<UsageHistoryViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Usage> = emptyList<Usage>().toMutableList()

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UsageHistoryViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: UsageHistoryViewHolder, position: Int) {
        val usage: Usage = filterList[position]

        if (usage.type == "1") {
            holder.laborLayout!!.visibility = View.VISIBLE
            holder.materialLayout!!.visibility = View.INVISIBLE

            holder.laborNameView!!.text = usage.empName
            holder.laborDateView!!.text = LocalDateTime.parse(usage.start, GlobalVars.dateFormatterPHP).format(GlobalVars.dateFormatterMonthDay)
            holder.laborQtyView!!.text = context.getString(R.string.x_units, usage.qty, unit)
        }
        else {
            holder.laborLayout!!.visibility = View.INVISIBLE
            holder.materialLayout!!.visibility = View.VISIBLE

            holder.materialDateView!!.text = LocalDateTime.parse(usage.start, GlobalVars.dateFormatterPHP).format(GlobalVars.dateFormatterMonthDay)
            holder.materialQtyView!!.text = context.getString(R.string.x_units, usage.qty, unit)
            holder.materialCostView!!.text = context.getString(R.string.dollar_sign, usage.unitCost)
            holder.materialTotalView!!.text = context.getString(R.string.dollar_sign, usage.totalCost)

            println("usage.hasReceipt: ${usage.hasReceipt}")
            if (usage.hasReceipt != null) {
                Picasso.with(context).load(R.drawable.ic_check_enabled).into(holder.materialReceiptView)
            }
            else {
                Picasso.with(context).load(R.drawable.ic_check_disabled).into(holder.materialReceiptView)
            }

        }


    }

    override fun getItemCount(): Int{
        return filterList.size
    }

}

class UsageHistoryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.usage_history_list_item, parent, false)) {

    var laborLayout:ConstraintLayout? = null
    var materialLayout:ConstraintLayout? = null

    var laborNameView:TextView? = null
    var laborDateView:TextView? = null
    var laborQtyView:TextView? = null

    var materialDateView:TextView? = null
    var materialQtyView:TextView? = null
    var materialCostView:TextView? = null
    var materialTotalView:TextView? = null
    var materialReceiptView:ImageView? = null

    init {
        laborLayout = itemView.findViewById(R.id.labor_cl)
        materialLayout = itemView.findViewById(R.id.material_cl)

        laborNameView = itemView.findViewById(R.id.labor_name_tv)
        laborDateView = itemView.findViewById(R.id.labor_date_tv)
        laborQtyView = itemView.findViewById(R.id.labor_qty_tv)

        materialDateView = itemView.findViewById(R.id.material_date_tv)
        materialQtyView = itemView.findViewById(R.id.material_qty_tv)
        materialCostView = itemView.findViewById(R.id.material_cost_tv)
        materialTotalView = itemView.findViewById(R.id.material_total_tv)
        materialReceiptView = itemView.findViewById(R.id.material_receipt_iv)
    }


}