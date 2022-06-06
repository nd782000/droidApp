package com.example.AdminMatic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AdminMatic.R
import com.squareup.picasso.Picasso
import java.time.LocalDateTime


class EmployeeUsageAdapter(list: MutableList<Usage>, private val context: Context, private val cellClickListener: EmployeeUsageCellClickListener) : RecyclerView.Adapter<EmployeeUsageViewHolder>() {

    //var onItemClick: ((Customer) -> Unit)? = null

    var filterList:MutableList<Usage> = emptyList<Usage>().toMutableList()
    lateinit  var globalVars:GlobalVars

    var queryText = ""

    init {
        filterList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeUsageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EmployeeUsageViewHolder(inflater, parent)
    }

    //private var listener: LogOut? = null





    override fun onBindViewHolder(holder: EmployeeUsageViewHolder, position: Int) {


        globalVars = GlobalVars()

        val usage: Usage = filterList[position]
        holder.bind(usage)
        println("queryText = $queryText")


        val mIconView:ImageView = holder.itemView.findViewById(R.id.list_usage_status_icon_iv)
        when (usage.woStatus) {
            "0"-> Picasso.with(context)
                .load(R.drawable.ic_canceled)
                .into(mIconView)
            "1"-> Picasso.with(context)
                .load(R.drawable.ic_not_started)
                .into(mIconView)
            "2"-> Picasso.with(context)
                .load(R.drawable.ic_in_progress)
                .into(mIconView)
            "3"-> Picasso.with(context)
                .load(R.drawable.ic_done)
                .into(mIconView)

        }

        val data = filterList[position]
        holder.itemView.setOnClickListener {
            cellClickListener.onWoItemCellClickListener(data)
        }

    }

    override fun getItemCount(): Int{

        print("getItemCount = ${filterList.size}")
        return filterList.size

    }





}

class EmployeeUsageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.employee_usage_list_item, parent, false)) {

    private var mNameView: TextView? = null
    private var mDateView: TextView? = null
    private var mQtyView: TextView? = null

    init {
        mNameView = itemView.findViewById(R.id.list_usage_name_tv)
        mDateView = itemView.findViewById(R.id.list_usage_date_tv)
        mQtyView = itemView.findViewById(R.id.list_usage_qty_tv)
    }

    fun bind(usage: Usage) {
        mNameView?.text = usage.custName
        mDateView?.text = LocalDateTime.parse(usage.start, GlobalVars.dateFormatterPHP).format(GlobalVars.dateFormatterMonthDay)
        mQtyView?.text = usage.qty
    }

}